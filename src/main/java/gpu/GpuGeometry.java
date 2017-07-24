package gpu;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.j3d.loaders.stl.STLFileReader;

import java.io.File;
import java.util.stream.IntStream;

public class GpuGeometry {

  private static final int A = 0;
  private static final int B = 1;
  private static final int C = 2;

  private static final int X = 0;
  private static final int Y = 1;
  private static final int Z = 2;

  // Loaded from specified file.
  private int numTriangles;

  // Aparapi only supports single-dimension arrays.
  private double[] normalX;
  private double[] normalY;
  private double[] normalZ;

  private double[] vertexAX;
  private double[] vertexAY;
  private double[] vertexAZ;

  private double[] edgeBAX;
  private double[] edgeBAY;
  private double[] edgeBAZ;

  private double[] edgeCAX;
  private double[] edgeCAY;
  private double[] edgeCAZ;

  // Below arrays are calculated when get() is called, as they're not always used.
  private double[] centerX;
  private double[] centerY;
  private double[] centerZ;

  private double[] area;

  @Inject
  public GpuGeometry() {}

  private void initWithSize(int size) {
    numTriangles = size;

    normalX = new double[size];
    normalY = new double[size];
    normalZ = new double[size];

    vertexAX = new double[size];
    vertexAY = new double[size];
    vertexAZ = new double[size];

    edgeBAX = new double[size];
    edgeBAY = new double[size];
    edgeBAZ = new double[size];

    edgeCAX = new double[size];
    edgeCAY = new double[size];
    edgeCAZ = new double[size];

    centerX = new double[size];
    centerY = new double[size];
    centerZ = new double[size];

    area = new double[size];
  }

  GpuGeometry empty() {
    initWithSize(0);
    return this;
  }

  GpuGeometry from(File file) {
    try {
      STLFileReader reader = new STLFileReader(file);
      initWithSize(IntStream.of(reader.getNumOfFacets()).sum());

      for (int index = 0; index < numTriangles; index++) {
        double[] normal = new double[3];
        double[][] vertices = new double[3][3];

        reader.getNextFacet(normal, vertices);

        normalX[index] = normal[X];
        normalY[index] = normal[Y];
        normalZ[index] = normal[Z];

        vertexAX[index] = vertices[A][X];
        vertexAY[index] = vertices[A][Y];
        vertexAZ[index] = vertices[A][Z];

        edgeBAX[index] = vertices[B][X] - vertices[A][X];
        edgeBAY[index] = vertices[B][Y] - vertices[A][Y];
        edgeBAZ[index] = vertices[B][Z] - vertices[A][Z];

        edgeCAX[index] = vertices[C][X] - vertices[A][X];
        edgeCAY[index] = vertices[C][Y] - vertices[A][Y];
        edgeCAZ[index] = vertices[C][Z] - vertices[A][Z];

        centerX[index] = (vertices[A][X] + vertices[B][X] + vertices[C][X]) / 3;
        centerY[index] = (vertices[A][Y] + vertices[B][Y] + vertices[C][Y]) / 3;
        centerZ[index] = (vertices[A][Z] + vertices[B][Z] + vertices[C][Z]) / 3;

        area[index] = areaOf(vertices);
      }
      reader.close();
      return this;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // From: https://math.stackexchange.com/questions/128991/how-to-calculate-area-of-3d-triangle
  @VisibleForTesting
  static double areaOf(double[][] triangle) {
    double x1 = triangle[1][0] - triangle[0][0];
    double x2 = triangle[1][1] - triangle[0][1];
    double x3 = triangle[1][2] - triangle[0][2];

    double y1 = triangle[2][0] - triangle[0][0];
    double y2 = triangle[2][1] - triangle[0][1];
    double y3 = triangle[2][2] - triangle[0][2];

    return .5 * Math.sqrt(
        (x2 * y3 - x3 * y2) * (x2 * y3 - x3 * y2)
            + (x3 * y1 - x1 * y3) * (x3 * y1 - x1 * y3)
            + (x1 * y2 - x2 * y1) * (x1 * y2 - x2 * y1));
  }


  public double[] getNormalX() {
    return normalX;
  }

  public double[] getNormalY() {
    return normalY;
  }

  public double[] getNormalZ() {
    return normalZ;
  }

  public double[] getVertexAX() {
    return vertexAX;
  }

  public double[] getVertexAY() {
    return vertexAY;
  }

  public double[] getVertexAZ() {
    return vertexAZ;
  }

  public double[] getEdgeBAX() {
    return edgeBAX;
  }

  public double[] getEdgeBAY() {
    return edgeBAY;
  }

  public double[] getEdgeBAZ() {
    return edgeBAZ;
  }

  public double[] getEdgeCAX() {
    return edgeCAX;
  }

  public double[] getEdgeCAY() {
    return edgeCAY;
  }

  public double[] getEdgeCAZ() {
    return edgeCAZ;
  }

  public double[] getCenterX() {
    return centerX;
  }

  public double[] getCenterY() {
    return centerY;
  }

  public double[] getCenterZ() {
    return centerZ;
  }

  public double[] getArea() {
    return area;
  }
}
