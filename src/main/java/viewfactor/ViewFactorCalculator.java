package viewfactor;

import com.google.inject.Inject;
import events.EventManager;
import org.j3d.loaders.stl.STLFileReader;

public class ViewFactorCalculator {

  private IntersectionKernel.Builder kernelBuilder;
  private EventManager eventManager;
  private ThreadedAdder adder;

  @Inject
  public ViewFactorCalculator(
      IntersectionKernel.Builder kernelBuilder,
      ThreadedAdder adder,
      EventManager eventManager) {
    this.kernelBuilder = kernelBuilder;
    this.adder = adder;
    this.eventManager = eventManager;
  }

  public void run(STLFileReader emitterFile, STLFileReader receiverFile, STLFileReader interconnectFile) {
    eventManager.start();
    kernelBuilder
        .setEmitterReader(emitterFile)
        .setInterconnectReader(interconnectFile)
        .setReceiverReader(receiverFile)
        .build()
        // TODO(Matthew Barry): we get the right result without dividing by area after summing. Why is this?
        .calculate(adder::add, adder::finishAndGet);
    eventManager.finish();
  }
}
