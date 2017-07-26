import com.google.common.base.Joiner;
import com.google.inject.Inject;
import viewfactor.state.StateManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapLogger implements StateManager.Subscriber {
  private static final String MESSAGES_KEY = "messages";
  private static final String STATE_KEY = "state";
  private static final String PERCENTAGE_KEY = "percentage";
  private static final String RESULT = "result";
  private static final String FAILURE = "failure";

  private static final DecimalFormat PERCENTAGE_FORMATTER = new DecimalFormat("##.##%");

  private List<String> messages;
  private String percentage;
  private StateManager.State state;
  private boolean failure;
  private double result;

  @Inject
  public MapLogger() {
    percentage = PERCENTAGE_FORMATTER.format(0);
    state = StateManager.State.INIT;
    failure = false;
    result = -1;
    messages = new ArrayList<>();
  }

  public Map<String, String> get() {
    Map<String, String> returnValue = new HashMap<>();
    returnValue.put(MESSAGES_KEY, Joiner.on(',').join(messages));
    returnValue.put(STATE_KEY, state.toString());
    returnValue.put(PERCENTAGE_KEY, percentage);
    returnValue.put(FAILURE, String.valueOf(failure));
    returnValue.put(RESULT, String.valueOf(result));
    messages.clear();
    return returnValue;
  }

  @Override
  public void onStart() {
    percentage = "0";
    failure = false;
    result = -1;
    messages.add("Initializing...");
  }

  @Override
  public void onStop(StateManager.ElapsedTime timer) {
    messages.add("All operations completed in " + timer);
  }

  @Override
  public void onParseStlStart() {
    messages.add("Reading STL files and precomputing geometry...");
  }

  @Override
  public void onParseStlFinish(StateManager.ElapsedTime elapsedTime) {
    messages.add("STLs parsed and geometry precomputed in: " + elapsedTime);
  }

  @Override
  public void onBufferTransferStart() {
    messages.add("Beginning initial buffer transfer to GPU.");
  }

  @Override
  public void onBufferTransferFinish(StateManager.ElapsedTime elapsedTime) {
    messages.add("Initial buffer transfer complete in: " + elapsedTime);
  }

  @Override
  public void onComputationStart() {
    messages.add("Beginning GPU computation of view factors.");
  }

  @Override
  public void onComputationProgress(StateManager.ElapsedTime elapsedTime, int current, int max) {
    double percent = (double) current / (double) max;
    String fraction = "(" + current + "/" + max + ").";
    messages.add("GPU computation is " + PERCENTAGE_FORMATTER.format(percent) + " complete " + fraction);
    this.percentage = PERCENTAGE_FORMATTER.format(percent);
  }

  @Override
  public void onComputationFinish(StateManager.ElapsedTime elapsedTime, double result) {
    messages.add("GPU computation finished in: " + elapsedTime);
    messages.add("Result: " + result);
    percentage = "100%";
    this.result = result;
  }

  @Override
  public void onInfo(String info) {
    messages.add("[INFO]: " + info);
  }

  @Override
  public void onException(Exception e) {
    messages.add("[ERROR]: " + e.getMessage());
    failure = true;
  }

  @Override
  public void onStateChange(StateManager.State state) {
    this.state = state;
  }
}
