import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import gpu.IntersectionKernel;

import java.io.File;

public class ViewFactorCalculator {

  private IntersectionKernel.Builder kernelBuilder;
  private ThreadedAdder adder;

  @Inject
  public ViewFactorCalculator(
      IntersectionKernel.Builder kernelBuilder,
      ThreadedAdder adder) {
    this.kernelBuilder = kernelBuilder;
    this.adder = adder;
  }

  public static void main(String[] args) {
    File emitterFile = new File("stl/bottom.stl");
    File receiverFile = new File("stl/top.stl");

    Injector injector = Guice.createInjector();
    injector.getInstance(ViewFactorCalculator.class).run(emitterFile, null, receiverFile);

    // TODO(Matthew Barry): we get the right output without dividing by area after summing. Why?
  }

  public void run(File emitterFile, File interconnectFile, File receiverFile) {
    kernelBuilder.setEmitterFile(emitterFile).setReceiverFile(receiverFile);
    if (interconnectFile != null) {
      kernelBuilder.setInterconnectFile(interconnectFile);
    }
    kernelBuilder.build().calculate(adder::add, this::onComplete);
  }

  private void onComplete() {
    System.out.println(adder.finishAndGet());
  }
}
