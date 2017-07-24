package gpu;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.File;
import java.util.function.BiConsumer;

public class IntersectionKernel extends Kernel {

  public static class Builder {
    private Provider<GpuGeometry> geometryProvider;
    private GpuGeometry emitters;
    private GpuGeometry interconnects;
    private GpuGeometry receivers;

    @VisibleForTesting
    Builder() {}

    @Inject
    public Builder(Provider<GpuGeometry> geometryProvider) {
      this.geometryProvider = geometryProvider;
    }

    public Builder setEmitterFile(File emitterFile) {
      emitters = geometryProvider.get().from(emitterFile);
      return this;
    }

    public Builder setInterconnectFile(File interconnectFile) {
      interconnects = geometryProvider.get().from(interconnectFile);
      return this;
    }

    public Builder setReceiverFile(File receiverFile) {
      receivers = geometryProvider.get().from(receiverFile);
      return this;
    }

    public IntersectionKernel build() {
      if (interconnects == null) {
        interconnects = geometryProvider.get().empty();
      }

      return new IntersectionKernel(
          emitters.getNormalX(),
          emitters.getNormalY(),
          emitters.getNormalZ(),
          emitters.getVertexAX(),
          emitters.getVertexAY(),
          emitters.getVertexAZ(),
          emitters.getCenterX(),
          emitters.getCenterY(),
          emitters.getCenterZ(),
          emitters.getArea(),
          interconnects.getNormalX(),
          interconnects.getNormalY(),
          interconnects.getNormalZ(),
          interconnects.getVertexAX(),
          interconnects.getVertexAY(),
          interconnects.getVertexAZ(),
          interconnects.getEdgeBAX(),
          interconnects.getEdgeBAY(),
          interconnects.getEdgeBAZ(),
          interconnects.getEdgeCAX(),
          interconnects.getEdgeCAY(),
          interconnects.getEdgeCAZ(),
          receivers.getNormalX(),
          receivers.getNormalY(),
          receivers.getNormalZ(),
          receivers.getVertexAX(),
          receivers.getVertexAY(),
          receivers.getVertexAZ(),
          receivers.getCenterX(),
          receivers.getCenterY(),
          receivers.getCenterZ(),
          receivers.getArea()
      );
    }
  }

  private static final class MathOnlyKernelException extends RuntimeException {
    private MathOnlyKernelException() {
      super("Error: this kernel is missing geometry data and can only be used for its math methods.");
    }
  }

  public interface KernelComplete {
    void onComplete();
  }

  @Constant private static final double PI = 3.141592653589793238462643383279502884197169399375105820974944592307816406286d;
  @Constant private static final int A = 0;
  @Constant private static final int B = 1;
  @Constant private static final int C = 2;
  @Constant private static final int X = 0;
  @Constant private static final int Y = 1;
  @Constant private static final int Z = 2;

  @Constant private final double[] emitterNormalX;
  @Constant private final double[] emitterNormalY;
  @Constant private final double[] emitterNormalZ;
  @Constant private final double[] emitterVertexAX;
  @Constant private final double[] emitterVertexAY;
  @Constant private final double[] emitterVertexAZ;
  @Constant private final double[] emitterCenterX;
  @Constant private final double[] emitterCenterY;
  @Constant private final double[] emitterCenterZ;
  @Constant private final double[] emitterAreas;

  @Constant private final double[] interconnectNormalX;
  @Constant private final double[] interconnectNormalY;
  @Constant private final double[] interconnectNormalZ;
  @Constant private final double[] interconnectVertexAX;
  @Constant private final double[] interconnectVertexAY;
  @Constant private final double[] interconnectVertexAZ;
  @Constant private final double[] interconnectEdgeBAX;
  @Constant private final double[] interconnectEdgeBAY;
  @Constant private final double[] interconnectEdgeBAZ;
  @Constant private final double[] interconnectEdgeCAX;
  @Constant private final double[] interconnectEdgeCAY;
  @Constant private final double[] interconnectEdgeCAZ;

  @Constant private final double[] receiverNormalX;
  @Constant private final double[] receiverNormalY;
  @Constant private final double[] receiverNormalZ;
  @Constant private final double[] receiverVertexAX;
  @Constant private final double[] receiverVertexAY;
  @Constant private final double[] receiverVertexAZ;
  @Constant private final double[] receiverCenterX;
  @Constant private final double[] receiverCenterY;
  @Constant private final double[] receiverCenterZ;
  @Constant private final double[] receiverAreas;

  private double[] result;
  private int emitterIndex;

  /**
   * Constructor, used only by the Builder class. The builder exists so the above fields can be final, allowing Aparapi
   * to put them in faster memory.
   */
  private IntersectionKernel(
      double[] emitterNormalX,
      double[] emitterNormalY,
      double[] emitterNormalZ,
      double[] emitterVertexAX,
      double[] emitterVertexAY,
      double[] emitterVertexAZ,
      double[] emitterCenterX,
      double[] emitterCenterY,
      double[] emitterCenterZ,
      double[] emitterAreas,
      double[] interconnectNormalX,
      double[] interconnectNormalY,
      double[] interconnectNormalZ,
      double[] interconnectVertexAX,
      double[] interconnectVertexAY,
      double[] interconnectVertexAZ,
      double[] interconnectEdgeBAX,
      double[] interconnectEdgeBAY,
      double[] interconnectEdgeBAZ,
      double[] interconnectEdgeCAX,
      double[] interconnectEdgeCAY,
      double[] interconnectEdgeCAZ,
      double[] receiverNormalX,
      double[] receiverNormalY,
      double[] receiverNormalZ,
      double[] receiverVertexAX,
      double[] receiverVertexAY,
      double[] receiverVertexAZ,
      double[] receiverCenterX,
      double[] receiverCenterY,
      double[] receiverCenterZ,
      double[] receiverAreas) {
    this.emitterNormalX = emitterNormalX;
    this.emitterNormalY = emitterNormalY;
    this.emitterNormalZ = emitterNormalZ;
    this.emitterVertexAX = emitterVertexAX;
    this.emitterVertexAY = emitterVertexAY;
    this.emitterVertexAZ = emitterVertexAZ;
    this.emitterCenterX = emitterCenterX;
    this.emitterCenterY = emitterCenterY;
    this.emitterCenterZ = emitterCenterZ;
    this.emitterAreas = emitterAreas;

    this.interconnectNormalX = interconnectNormalX;
    this.interconnectNormalY = interconnectNormalY;
    this.interconnectNormalZ = interconnectNormalZ;
    this.interconnectVertexAX = interconnectVertexAX;
    this.interconnectVertexAY = interconnectVertexAY;
    this.interconnectVertexAZ = interconnectVertexAZ;
    this.interconnectEdgeBAX = interconnectEdgeBAX;
    this.interconnectEdgeBAY = interconnectEdgeBAY;
    this.interconnectEdgeBAZ = interconnectEdgeBAZ;
    this.interconnectEdgeCAX = interconnectEdgeCAX;
    this.interconnectEdgeCAY = interconnectEdgeCAY;
    this.interconnectEdgeCAZ = interconnectEdgeCAZ;

    this.receiverNormalX = receiverNormalX;
    this.receiverNormalY = receiverNormalY;
    this.receiverNormalZ = receiverNormalZ;
    this.receiverVertexAX = receiverVertexAX;
    this.receiverVertexAY = receiverVertexAY;
    this.receiverVertexAZ = receiverVertexAZ;
    this.receiverCenterX = receiverCenterX;
    this.receiverCenterY = receiverCenterY;
    this.receiverCenterZ = receiverCenterZ;
    this.receiverAreas = receiverAreas;
  }

  // Used to eliminate circular dependency injection for GpuGeometry and IntersectionKernel.Builder.
  static IntersectionKernel forMathOnly() {
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
        null,
        null);
  }

  /**
   * Runs the kernel for each emitter triangle, passing the view factor result of that triangle's dA incrementally back
   * to resultConsumer. Calls completionHandler onComplete when the task is finished.
   */
  public void calculate(BiConsumer<double[], Double> resultConsumer, KernelComplete completionHandler) {
    if (isMathOnly()) {
      throw new MathOnlyKernelException();
    }
    // Run in parallel.
    new Thread(() -> {
      setExplicit(true);
      put(emitterNormalX).put(emitterNormalY).put(emitterNormalZ);
      put(emitterVertexAX).put(emitterVertexAY).put(emitterVertexAZ);
      put(emitterCenterX).put(emitterCenterY).put(emitterCenterZ);
      put(emitterAreas);

//      put(interconnectNormalX).put(interconnectNormalY).put(interconnectNormalZ);
//      put(interconnectVertexAX).put(interconnectVertexAY).put(interconnectVertexAZ);
//      put(interconnectEdgeBAX).put(interconnectEdgeBAY).put(interconnectEdgeBAZ);
//      put(interconnectEdgeCAX).put(interconnectEdgeCAY).put(interconnectEdgeCAZ);

      put(receiverNormalX).put(receiverNormalY).put(receiverNormalZ);
      put(receiverVertexAX).put(receiverVertexAY).put(receiverVertexAZ);
      put(receiverCenterX).put(receiverCenterY).put(receiverCenterZ);
      put(receiverAreas);

      result = new double[receiverAreas.length];

      for (emitterIndex = 0; emitterIndex < receiverAreas.length; emitterIndex++) {
        super.execute(Range.create(receiverAreas.length));
        get(result);
        resultConsumer.accept(result, emitterAreas[emitterIndex]);
      }
      completionHandler.onComplete();
    }).run();
  }

  /**
   * Kernel code goes here. This is public, but don't call this!
   */
  @Override
  public void run() {
    int receiverIndex = getGlobalId();

    // Calculate the ray from the emitter to the destination triangle.
    double rayX = receiverCenterX[receiverIndex] - emitterCenterX[emitterIndex];
    double rayY = receiverCenterY[receiverIndex] - emitterCenterY[emitterIndex];
    double rayZ = receiverCenterZ[receiverIndex] - emitterCenterZ[emitterIndex];

    double rayMagnitude = magnitude(rayX, rayY, rayZ);

    double emitterDenominator =
        magnitude(
            emitterNormalX[emitterIndex],
            emitterNormalY[emitterIndex],
            emitterNormalZ[emitterIndex]) * rayMagnitude;
    double receiverDenominator =
        magnitude(
            receiverNormalX[receiverIndex],
            receiverNormalY[receiverIndex],
            receiverNormalZ[receiverIndex]) * rayMagnitude;

    double emitterNormalDotRay =
        emitterNormalX[emitterIndex] * rayX
        + emitterNormalY[emitterIndex] * rayY
        + emitterNormalZ[emitterIndex] * rayZ;
    double receiverNormalDotRay =
        receiverNormalX[receiverIndex] * rayX
            + receiverNormalY[receiverIndex] * rayY
            + receiverNormalZ[receiverIndex] * rayZ;

    double cosThetaOne = emitterNormalDotRay / emitterDenominator;
    double cosThetaTwo = receiverNormalDotRay / receiverDenominator;

    // TODO(Matthew Barry): please verify that cosThetaOne and cosThetaTwo are correct. Dot product with opposite ray
    // should just cause the angle to be negative, I believe.
    if (cosThetaOne < 0) {
      cosThetaOne = -cosThetaOne;
    }
    if (cosThetaTwo < 0) {
      cosThetaTwo = -cosThetaTwo;
    }

    result[receiverIndex] = cosThetaOne * cosThetaTwo * emitterAreas[emitterIndex] * receiverAreas[receiverIndex]
        / (PI * rayMagnitude * rayMagnitude);
  }

  private boolean isMathOnly() {
    return emitterNormalX == null
        || emitterNormalY == null
        || emitterNormalZ == null
        || emitterVertexAX == null
        || emitterVertexAY == null
        || emitterVertexAZ == null
        || emitterCenterX == null
        || emitterCenterY == null
        || emitterCenterZ == null
        || emitterAreas == null
        || interconnectNormalX == null
        || interconnectNormalY == null
        || interconnectNormalZ == null
        || interconnectVertexAX == null
        || interconnectVertexAY == null
        || interconnectVertexAZ == null
        || interconnectEdgeBAX == null
        || interconnectEdgeBAY == null
        || interconnectEdgeBAZ == null
        || interconnectEdgeCAX == null
        || interconnectEdgeCAY == null
        || interconnectEdgeCAZ == null
        || receiverNormalX == null
        || receiverNormalY == null
        || receiverNormalZ == null
        || receiverVertexAX == null
        || receiverVertexAY == null
        || receiverVertexAZ == null
        || receiverCenterX == null
        || receiverCenterY == null
        || receiverCenterZ == null
        || receiverAreas == null;
  }
  @VisibleForTesting
  double magnitude(double a, double b, double c) {
    return Math.sqrt(a * a + b * b + c * c);
  }
}
