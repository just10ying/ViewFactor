package viewfactor.state;

import com.google.inject.Inject;

import java.text.DecimalFormat;

public class ConsoleLogger implements StateManager.Subscriber{

  private static final DecimalFormat PERCENTAGE_FORMATTER = new DecimalFormat("##.##%");

  @Inject
  public ConsoleLogger() {}

  @Override
  public void onStart() {
    System.out.println("Initializing...");
  }

  @Override
  public void onStop(StateManager.ElapsedTime timer) {
    System.out.println("All operations completed in " + timer);
  }

  @Override
  public void onParseStlStart() {
    System.out.println("Reading STL files and precomputing geometry...");
  }

  @Override
  public void onParseStlFinish(StateManager.ElapsedTime elapsedTime) {
    System.out.println("STLs parsed and geometry precomputed in: " + elapsedTime);
  }

  @Override
  public void onBufferTransferStart() {
    System.out.println("Beginning initial buffer transfer to GPU.");
  }

  @Override
  public void onBufferTransferFinish(StateManager.ElapsedTime elapsedTime) {
    System.out.println("Initial buffer transfer complete in: " + elapsedTime);
  }

  @Override
  public void onComputationStart() {
    System.out.println("Beginning GPU computation of view factors.");
  }

  @Override
  public void onComputationProgress(StateManager.ElapsedTime elapsedTime, int current, int max) {
    double percent = (double) current / (double) max;
    String fraction = "(" + current + "/" + max + ").";
    System.out.println("GPU computation is " + PERCENTAGE_FORMATTER.format(percent) + " complete " + fraction);
  }

  @Override
  public void onComputationFinish(StateManager.ElapsedTime elapsedTime, double result) {
    System.out.println("GPU computation finished in: " + elapsedTime);
    System.out.println("Result: " + result);
  }

  @Override
  public void onInfo(String info) {
    System.out.println("[INFO]: " + info);
  }

  @Override
  public void onException(Exception e) {
    System.out.println("[ERROR]: " + e.getMessage());
    e.printStackTrace();
  }
}
