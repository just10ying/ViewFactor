import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import gpu.IntersectionKernel;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.DoubleStream;

public class ViewFactorCalculator {

  private static final int NUM_THREADS = 3;

  private IntersectionKernel.Builder kernelBuilder;
  private ExecutorService adderPool;
  private double sum;

  @Inject
  public ViewFactorCalculator(IntersectionKernel.Builder kernelBuilder) {
    this.kernelBuilder = kernelBuilder;

    sum = 0;
    adderPool = Executors.newFixedThreadPool(NUM_THREADS);
  }

  public void run(File emitterFile, File interconnectFile, File receiverFile) {
    kernelBuilder.setEmitterFile(emitterFile).setReceiverFile(receiverFile);
    if (interconnectFile != null) {
      kernelBuilder.setInterconnectFile(interconnectFile);
    }
    kernelBuilder.build().calculate(this::computePartialViewFactor, this::onComplete);
  }

  private void computePartialViewFactor(double[] result, double dA) {
    sum += DoubleStream.of(result).sum();
  }

  private void onComplete() {
    System.out.println(sum);
  }

  public static void main(String[] args) {
    File emitterFile = new File("stl/bottom.stl");
    File receiverFile = new File("stl/top.stl");

    Injector injector = Guice.createInjector();
    injector.getInstance(ViewFactorCalculator.class).run(emitterFile, null, receiverFile);
  }
}
