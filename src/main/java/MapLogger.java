package viewfactor.state;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

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

  private enum State {
    RUNNING, IDLE;
  }

  private static final DecimalFormat PERCENTAGE_FORMATTER = new DecimalFormat("##.##%");

  private State state;
  private List<String> messages;
  private String percentage;
  private boolean failure;
  private double result;

  @Inject
  public MapLogger() {
    state = State.RUNNING;
    percentage = "0";
    failure = false;
    result = -1;
    messages = new ArrayList<>();
  }

  public Map<String, String> get() {
    Map<String, String> state = new HashMap<>();
    state.put(MESSAGES_KEY, Joiner.on(',').join(messages));
    state.put(STATE_KEY, state.toString());
    state.put(PERCENTAGE_KEY, percentage);
    state.put(FAILURE, String.valueOf(failure));
    state.put(RESULT, String.valueOf(result));
    messages.clear();
    return state;
  }

  @Override
  public void onStart() {
    state = State.RUNNING;
    percentage = "0";
    failure = false;
    result = -1;
    messages.add("Initializing...");
  }

  @Override
  public void onStop(StateManager.ElapsedTime timer) {
    this.state = State.IDLE;
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
    e.printStackTrace();
    state = State.IDLE;
    failure = true;
  }
}
