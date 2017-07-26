package viewfactor;

import com.google.inject.Inject;
import org.j3d.loaders.stl.STLFileReader;
import viewfactor.events.ConsoleLogger;
import viewfactor.events.EventManager;
import viewfactor.gpu.IntersectionKernel;

public class ViewFactorCalculator {

  private IntersectionKernel.Builder kernelBuilder;
  private ThreadedAdder adder;

  @Inject
  public ViewFactorCalculator(
      IntersectionKernel.Builder kernelBuilder,
      ThreadedAdder adder,
      EventManager eventManager,
      ConsoleLogger consoleLogger) {
    this.kernelBuilder = kernelBuilder;
    this.adder = adder;
    // By default include a console logger.
    eventManager.addSubscriber(consoleLogger);
  }

  public void run(STLFileReader emitterFile, STLFileReader receiverFile, STLFileReader interconnectFile) {
    new Thread(() -> kernelBuilder
        .setEmitterReader(emitterFile)
        .setInterconnectReader(interconnectFile)
        .setReceiverReader(receiverFile)
        .build()
        // TODO(Matthew Barry): we get the right result without dividing by area after summing. Why is this?
        .calculate(adder::add, adder::finishAndGet)).run();
  }
}
