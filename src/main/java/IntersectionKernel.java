import com.aparapi.Kernel;
import com.aparapi.Range;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.j3d.loaders.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

class IntersectionKernel extends Kernel {

  static class Builder {

    private Provider<Geometry> geometryProvider;
    private Geometry emitters;
    private Geometry interconnects;
    private Geometry receivers;

    @VisibleForTesting
    Builder() {}

    @Inject
    Builder(Provider<Geometry> geometryProvider) {
      this.geometryProvider = geometryProvider;
    }

    Builder setEmitterFile(File emitterFile) throws InvalidFormatException, IOException {
      emitters = geometryProvider.get().from(emitterFile);
      return this;
    }

    Builder setInterconnectFile(File interconnectFile) throws InvalidFormatException, IOException {
      interconnects = geometryProvider.get().from(interconnectFile);
      return this;
    }

    Builder setReceiverFile(File receiverFile) throws InvalidFormatException, IOException {
      receivers = geometryProvider.get().from(receiverFile);
      return this;
    }

    IntersectionKernel build() {
      return new IntersectionKernel(
          emitters.getNormals(),
          emitters.getTriangles(),
          emitters.getCenters(),
          emitters.getAreas(),
          interconnects.getNormals(),
          interconnects.getTriangles(),
          interconnects.getEdges(),
          receivers.getNormals(),
          receivers.getTriangles(),
          receivers.getCenters(),
          receivers.getAreas()
      );
    }

    IntersectionKernel buildMathOnly() {
      return new IntersectionKernel(
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);
    }
  }

  private static final class MathOnlyKernelException extends RuntimeException {
    private MathOnlyKernelException() {
      super("Error: this kernel is missing geometry data and can only be used for its math methods.");
    }
  }

  interface KernelComplete {
    void onComplete();
  }

  @Constant private final double[][] emitterNormals;
  @Constant private final double[][][] emitterTriangles;
  @Constant private final double[][] emitterCenters;
  @Constant private final double[] emitterAreas;

  @Constant private final double[][] interconnectNormals;
  @Constant private final double[][][] interconnectTriangles;
  @Constant private final double[][][] interconnectEdges;

  @Constant private final double[][] receiverNormals;
  @Constant private final double[][][] receiverTriangles;
  @Constant private final double[][] receiverCenters;
  @Constant private final double[] receiverAreas;

  private double[] result;
  private int emitterIndex;

  /**
   * Constructor, used only by the Builder class. The builder exists so the above fields can be final, allowing Aparapi
   * to put them in faster memory.
   */
  private IntersectionKernel(
      double[][] emitterNormals,
      double[][][] emitterTriangles,
      double[][] emitterCenters,
      double[] emitterAreas,
      double[][] interconnectNormals,
      double[][][] interconnectTriangles,
      double[][][] interconnectEdges,
      double[][] receiverNormals,
      double[][][] receiverTriangles,
      double[][] receiverCenters,
      double[] receiverAreas) {
    this.emitterNormals = emitterNormals;
    this.emitterTriangles = emitterTriangles;
    this.emitterCenters = emitterCenters;
    this.emitterAreas = emitterAreas;
    this.interconnectNormals = interconnectNormals;
    this.interconnectTriangles = interconnectTriangles;
    this.interconnectEdges = interconnectEdges;
    this.receiverNormals = receiverNormals;
    this.receiverTriangles = receiverTriangles;
    this.receiverCenters = receiverCenters;
    this.receiverAreas = receiverAreas;

    setExplicit(true);
    put(emitterNormals).put(emitterTriangles).put(emitterCenters).put(emitterAreas);
    put(interconnectNormals).put(interconnectTriangles).put(interconnectEdges);
    put(receiverNormals).put(receiverTriangles).put(receiverCenters).put(receiverAreas);
  }

  /**
   * Runs the kernel for each emitter triangle, passing the view factor result of that triangle's dA incrementally back
   * to resultConsumer. Calls completionHandler onComplete when the task is finished.
   */
  public void calculateAll(Consumer<double[]> resultConsumer, KernelComplete completionHandler) {
    if (isMathOnly()) {
      throw new MathOnlyKernelException();
    }
    // Run in parallel.
    new Thread(() -> {
      for (emitterIndex = 0; emitterIndex < receiverTriangles.length; emitterIndex++) {
        super.execute(Range.create(receiverTriangles.length));
        get(result);
        resultConsumer.accept(result);
      }
      completionHandler.onComplete();
    }).run();
  }

  /**
   * Kernel code goes here.
   */
  @Override
  public void run() {
    int receiverIndex = getGlobalId();
    // Calculate the ray from the emitter to the destination triangle.
    double[] ray = edgeFrom(receiverCenters[receiverIndex], emitterCenters[emitterIndex]);
  }

  private boolean isMathOnly() {
    return emitterNormals == null
        || emitterTriangles == null
        || emitterCenters == null
        || emitterAreas == null
        || interconnectNormals == null
        || interconnectTriangles == null
        || interconnectEdges == null
        || receiverNormals == null
        || receiverTriangles == null
        || receiverCenters == null
        || receiverAreas == null;
  }

  /**
   * Moller-Trumbore intersection for use on GPU. Taken from: https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
   * TODO: tests.
   * @param orig 3D coordinates of the ray's origin.
   * @param dir 3D vector components of the ray.
   * @param triangle Triangle given by a 3x3 array; three points in 3d.
   * @return whether or not the ray intersects the given triangle.
   */
  boolean doesIntersect(double[] orig, double[] dir, double[][] triangle, double[][] edges) {
    double[] v0 = triangle[0];
    double[] v1 = triangle[1];
    double[] v2 = triangle[2];

    double[] e1 = edges[0];
    double[] e2 = edges[1];

    double[] pvec = crossProduct(dir, e2);
    double det = dotProduct(e1, pvec);

    // Ray is parallel to plane
    if (det < 1e-8 && det > -1e-8) {
      return false;
    }

    double inv_det = 1 / det;
    double[] tvec = edgeFrom(orig, v0);
    double u = dotProduct(tvec, pvec) * inv_det;
    if (u < 0 || u > 1) {
      return false;
    }

    double[] qvec = crossProduct(tvec, e1);
    double v = dotProduct(dir, qvec) * inv_det;
    return !(v < 0) && !(u + v > 1);
  }

  @VisibleForTesting
  double areaOf(double[][] triangle) {
    double[] ab = edgeFrom(triangle[1], triangle[0]);
    double[] ac = edgeFrom(triangle[2], triangle[0]);

    return .5 * magnitude(ab) * magnitude(ac) * Math.sin(angleBetween(ab, ac));
  }

  @VisibleForTesting
  // Computes the angle between the two vectors in radians.
  double angleBetween(double[] a, double[] b) {
    return Math.acos(dotProduct(a, b) / (magnitude(a) * magnitude(b)));
  }

  @VisibleForTesting
  double dotProduct(double[] a, double[] b) {
    return ((a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2]));
  }

  @VisibleForTesting
  double[] crossProduct(double[] a, double[] b) {
    double[] result = new double[3];
    result[0] = ((a[1] * b[2]) - (a[2] * b[1]));
    result[1] = ((a[2] * b[0]) - (a[0] * b[2]));
    result[2] = ((a[0] * b[1]) - (a[1] * b[0]));
    return result;
  }

  @VisibleForTesting
  double magnitude(double[] input) {
    return Math.sqrt(input[0] * input[0] + input[1] * input[1] + input[2] * input[2]);
  }

  @VisibleForTesting
  double[] edgeFrom(double[] a, double[] b) {
    double[] result = new double[3];

    result[0] = a[0] - b[0];
    result[1] = a[1] - b[1];
    result[2] = a[2] - b[2];

    return result;
  }

  @VisibleForTesting
  double[] centerOf(double[][] triangle) {
    double[] result = new double[3];

    result[0] = (triangle[0][0] + triangle[1][0] + triangle[2][0]) / 3;
    result[1] = (triangle[0][1] + triangle[1][1] + triangle[2][1]) / 3;
    result[2] = (triangle[0][2] + triangle[1][2] + triangle[2][2]) / 3;

    return result;
  }

}
