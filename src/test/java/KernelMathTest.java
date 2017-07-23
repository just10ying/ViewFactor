import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class KernelMathTest {

  private static final double TOLERANCE = 0.0000000000000005d;

  private IntersectionKernel kernelMath;

  @Before
  public void setup() {
    kernelMath = new IntersectionKernel.Builder().buildMathOnly();
  }

  @Test
  public void centerOf_shouldBeMathematicallyCorrect() {
    double[][] triangle = new double[3][];
    triangle[0] = new double[] {1, 2, 3};
    triangle[1] = new double[] {4, 2, 3.2};
    triangle[2] = new double[] {-2, 9.32, 8};

    double[] expected = new double[] {1, 4.44, 4.733333333333333};

    assertThat(kernelMath.centerOf(triangle)).usingExactEquality().containsExactly(expected);
  }

  @Test
  public void edgeFrom_shouldBeMathematicallyCorrect() {
    double[] a = new double[] {2.3, 9, 4};
    double[] b = new double[] {4.3, 2, -33};

    double[] expected = new double[] {-2, 7, 37};

    assertThat(kernelMath.edgeFrom(a, b)).usingExactEquality().containsExactly(expected);
  }

  @Test
  public void magnitude_shouldBeMathematicallyCorrect() {
    double[] input = new double[] {3, -3.2, 4.12};

    assertThat(kernelMath.magnitude(input)).isEqualTo(6.017840144104859d);
  }

  @Test
  public void crossProduct_shouldBeMathematicallyCorrect() {
    double[] a = new double[] {3, 2, -1};
    double[] b = new double[] {4, -7, -2.2};

    double[] expected = new double[] {-11.4, 2.6, -29};

    assertThat(kernelMath.crossProduct(a, b)).usingTolerance(TOLERANCE).containsExactly(expected);
  }

  @Test
  public void dotProduct_shouldBeMathematicallyCorrect()  {
    double[] a = new double[] {3, 2, -1};
    double[] b = new double[] {4, -7, -2.2};

    assertThat(kernelMath.dotProduct(a, b)).isWithin(TOLERANCE).of(0.2d);
  }

  @Test
  public void angleBetween_shouldBeMathematicallyCorrect() {
    double[] a = new double[] {3, 2, -1};
    double[] b = new double[] {4, -7, -2.2};

    assertThat(kernelMath.angleBetween(a, b)).isWithin(TOLERANCE).of(1.5644002035465558);
  }

  @Test
  public void areaOfTriangle_shouldBeMathematicallyCorrect() {
    double[][] triangle = new double[3][];
    triangle[0] = new double[] {3, 2, -1};
    triangle[1] = new double[] {4, -7, -2.2};
    triangle[2] = new double[] {1, 2, 9};

    assertThat(kernelMath.areaOf(triangle)).isEqualTo(46.04823557966146d);
  }
}
