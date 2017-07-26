package viewfactor;

import com.google.inject.Inject;
import org.j3d.loaders.stl.STLFileReader;
import viewfactor.gpu.IntersectionKernel;
import viewfactor.state.StateManager;

public class ViewFactorCalculator {

  private IntersectionKernel.Builder kernelBuilder;
  private StateManager stateManager;
  private ThreadedAdder adder;

  @Inject
  public ViewFactorCalculator(
      IntersectionKernel.Builder kernelBuilder,
      ThreadedAdder adder,
      StateManager stateManager) {
    this.kernelBuilder = kernelBuilder;
    this.adder = adder;
    this.stateManager = stateManager;
  }

  public void run(STLFileReader emitterFile, STLFileReader receiverFile, STLFileReader interconnectFile) {
    stateManager.start();
    kernelBuilder
        .setEmitterReader(emitterFile)
        .setInterconnectReader(interconnectFile)
        .setReceiverReader(receiverFile)
        .build()
        // TODO(Matthew Barry): we get the right result without dividing by area after summing. Why is this?
        .calculate(adder::add, adder::finishAndGet);
    stateManager.finish();
  }
}
