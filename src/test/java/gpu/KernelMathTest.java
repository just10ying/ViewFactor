package gpu;

import org.junit.Before;

public class KernelMathTest {

  private static final double TOLERANCE = 0.0000000000000005d;

  private IntersectionKernel kernelMath;

  @Before
  public void setup() {
    kernelMath = IntersectionKernel.forMathOnly();
  }
}
