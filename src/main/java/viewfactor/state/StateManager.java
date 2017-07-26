package viewfactor.state;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StateManager implements Events {

  public interface Subscriber {
    void onStart();
    void onStop(ElapsedTime timer);
    void onParseStlStart();
    void onParseStlFinish(ElapsedTime timer);
    void onBufferTransferStart();
    void onBufferTransferFinish(ElapsedTime timer);
    void onComputationStart();
    void onComputationProgress(ElapsedTime timer, int current, int max);
    void onComputationFinish(ElapsedTime timer, double result);
    void onInfo(String info);
    void onException(Exception e);
    void onStateChange(State state);
  }

  public enum State {
    IDLE,
    INIT,
    PARSE_STL,
    POST_PARSE_STL,
    BUFFER_TRANSFER,
    POST_BUFFER_TRANSFER,
    COMPUTATION,
    POST_COMPUTATION,
    EXCEPTION
  }

  private final Provider<Stopwatch> stopwatchProvider;
  private final List<Subscriber> subscribers;

  private Stopwatch allStopWatch;
  private Stopwatch stlStopwatch;
  private Stopwatch bufferTransferStopwatch;
  private Stopwatch computationStopwatch;

  public State getState() {
    return state;
  }

  private State state;

  @Inject
  public StateManager(Provider<Stopwatch> stopwatchProvider, ConsoleLogger consoleLogger) {
    this.stopwatchProvider = stopwatchProvider;
    subscribers = new ArrayList<>();

    // By default, log to console.
    subscribers.add(consoleLogger);
    changeState(State.IDLE);
  }

  public void registerSubscriber(Subscriber subscriber) {
    subscribers.add(subscriber);
  }

  @Override
  public void start() {
    Preconditions.checkState(state == State.IDLE || state == State.EXCEPTION);
    changeState(State.INIT);

    allStopWatch = stopwatchProvider.get();
    stlStopwatch = stopwatchProvider.get();
    bufferTransferStopwatch = stopwatchProvider.get();
    computationStopwatch = stopwatchProvider.get();
    subscribers.forEach(Subscriber::onStart);
    allStopWatch.start();
  }

  @Override
  public void startParseStl() {
    Preconditions.checkState(state == State.INIT);
    changeState(State.PARSE_STL);

    subscribers.forEach(Subscriber::onParseStlStart);
    stlStopwatch.start();
  }

  @Override
  public void finishParseStl() {
    Preconditions.checkState(state == State.PARSE_STL);
    changeState(State.POST_PARSE_STL);

    stlStopwatch.stop();
    subscribers.forEach(subscriber -> subscriber.onParseStlFinish(new ElapsedTime(stlStopwatch)));
  }

  @Override
  public void startBufferTransfer() {
    Preconditions.checkState(state == State.POST_PARSE_STL);
    changeState(State.BUFFER_TRANSFER);

    subscribers.forEach(Subscriber::onBufferTransferStart);
    bufferTransferStopwatch.start();
  }

  @Override
  public void finishBufferTransfer() {
    Preconditions.checkState(state == State.BUFFER_TRANSFER);
    changeState(State.POST_BUFFER_TRANSFER);

    bufferTransferStopwatch.stop();
    subscribers.forEach(subscriber -> subscriber.onBufferTransferFinish(new ElapsedTime(bufferTransferStopwatch)));
  }

  @Override
  public void startComputation() {
    Preconditions.checkState(state == State.POST_BUFFER_TRANSFER);
    changeState(State.COMPUTATION);

    subscribers.forEach(Subscriber::onComputationStart);
    computationStopwatch.start();
  }

  @Override
  public void updateComputationProgress(int current, int max) {
    Preconditions.checkState(state == State.COMPUTATION);

    subscribers.forEach(subscriber -> subscriber.onComputationProgress(new ElapsedTime(computationStopwatch), current, max));
  }

  @Override
  public void finishComputation(double result) {
    Preconditions.checkState(state == State.COMPUTATION);
    changeState(State.POST_COMPUTATION);

    computationStopwatch.stop();
    subscribers.forEach(subscriber -> subscriber.onComputationFinish(new ElapsedTime(computationStopwatch), result));
  }

  @Override
  public void finish() {
    Preconditions.checkState(state == State.POST_COMPUTATION);
    changeState(State.IDLE);

    allStopWatch.stop();
    subscribers.forEach(subscriber -> subscriber.onStop(new ElapsedTime(allStopWatch)));
  }

  @Override
  public void info(String info) {
    subscribers.forEach(subscriber -> subscriber.onInfo(info));
  }

  @Override
  public void exception(Exception e) {
    changeState(State.EXCEPTION);
    subscribers.forEach(subscriber -> subscriber.onException(e));
  }

  private void changeState(State newState) {
    state = newState;
    subscribers.forEach(subscriber -> subscriber.onStateChange(state));
  }

  public static class ElapsedTime {
    private Stopwatch stopwatch;

    private ElapsedTime(Stopwatch stopwatch) {
      this.stopwatch = stopwatch;
    }

    public long elapsed(TimeUnit desiredUnit) {
      return stopwatch.elapsed(desiredUnit);
    }

    @Override
    public String toString() {
      for (TimeUnit unit : TimeUnit.values()) {
        long value = stopwatch.elapsed(unit);
        if (value > 9999)  {
          continue;
        }
        return value + " " + unit.name().toLowerCase() + ".";
      }
      return "Unable to find reasonable time unit for seconds: " + stopwatch.elapsed(TimeUnit.SECONDS);
    }
  }
}


