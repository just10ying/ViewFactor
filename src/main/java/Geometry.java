import com.google.inject.Inject;
import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.stl.STLFileReader;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

public class Geometry {

  // Loaded from specified file.
  private int numTriangles;
  private double[][] allNormals;
  private double[][][] allTriangles;

  // Calculated on get.
  private double[][] centers;
  private double[] areas;
  private double[][][] edges;

  private IntersectionKernel kernelMath;

  @Inject
  public Geometry(IntersectionKernel.Builder kernelBuilder) {
    this.kernelMath = kernelBuilder.buildMathOnly();
  }

  Geometry from(File file) throws InvalidFormatException, IOException {
    STLFileReader reader = new STLFileReader(file);
    numTriangles = IntStream.of(reader.getNumOfFacets()).sum();
    allNormals = new double[numTriangles][3];
    allTriangles = new double[numTriangles][3][3];
    for (int index = 0; index < numTriangles; index++) {
      double[] normal = new double[3];
      double[][] vertices = new double[3][3];

      reader.getNextFacet(normal, vertices);

      allNormals[index] = normal;
      allTriangles[index] = vertices;
    }
    reader.close();
    return this;
  }

  double[][] getNormals() {
    return allNormals;
  }

  double[][][] getTriangles() {
    return allTriangles;
  }

  double[][] getCenters() {
    if (centers == null) {
      centers = new double[numTriangles][];
      for (int index = 0; index < numTriangles; index++) {
        centers[index] = kernelMath.centerOf(allTriangles[index]);
      }
    }
    return centers;
  }

  double[] getAreas() {
    if (areas == null) {
      areas = new double[numTriangles];
      for (int index = 0; index < numTriangles; index++) {
        areas[index] = kernelMath.areaOf(allTriangles[index]);
      }
    }
    return areas;
  }

  double[][][] getEdges() {
    if (edges == null) {
      edges = new double[numTriangles][2][];
      for (int index = 0; index < numTriangles; index++) {
        double[][] triangle = allTriangles[index];
        edges[index][0] = kernelMath.edgeFrom(triangle[1], triangle[0]);
        edges[index][1] = kernelMath.edgeFrom(triangle[2], triangle[0]);
      }
    }
    return edges;
  }
}
