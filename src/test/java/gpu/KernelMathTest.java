package gpu;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class KernelMathTest {

  private static final double TOLERANCE = 0.0000000000000005d;

  private IntersectionKernel kernelMath;

  @Before
  public void setup() {
    kernelMath = IntersectionKernel.forMathOnly();
  }

  @Test
  public void magnitude_shouldReturnMathematicallyCorrectValue() {
    double result = kernelMath.magnitude(0, -2, 9.3);

    assertThat(result).isEqualTo(9.51262319236918);
  }
}
