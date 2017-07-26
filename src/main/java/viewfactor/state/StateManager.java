package viewfactor.state;

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
  }

  private final Provider<Stopwatch> stopwatchProvider;
  private final List<Subscriber> subscribers;

  private Stopwatch allStopWatch;
  private Stopwatch stlStopwatch;
  private Stopwatch bufferTransferStopwatch;
  private Stopwatch computationStopwatch;

  @Inject
  public StateManager(Provider<Stopwatch> stopwatchProvider, ConsoleLogger consoleLogger) {
    this.stopwatchProvider = stopwatchProvider;
    subscribers = new ArrayList<>();
    subscribers.add(consoleLogger); // By default, include a console logger.
  }

  public void registerSubscriber(Subscriber subscriber) {
    subscribers.add(subscriber);
  }

  @Override
  public void start() {
    allStopWatch = stopwatchProvider.get();
    stlStopwatch = stopwatchProvider.get();
    bufferTransferStopwatch = stopwatchProvider.get();
    computationStopwatch = stopwatchProvider.get();
    subscribers.forEach(Subscriber::onStart);
    allStopWatch.start();
  }

  @Override
  public void finish() {
    allStopWatch.stop();
    subscribers.forEach(subscriber -> subscriber.onStop(new ElapsedTime(allStopWatch)));
  }

  @Override
  public void startParseStl() {
    subscribers.forEach(Subscriber::onParseStlStart);
    stlStopwatch.start();
  }

  @Override
  public void finishParseStl() {
    stlStopwatch.stop();
    subscribers.forEach(subscriber -> subscriber.onParseStlFinish(new ElapsedTime(stlStopwatch)));
  }

  @Override
  public void startBufferTransfer() {
    subscribers.forEach(Subscriber::onBufferTransferStart);
    bufferTransferStopwatch.start();
  }

  @Override
  public void finishBufferTransfer() {
    bufferTransferStopwatch.stop();
    subscribers.forEach(subscriber -> subscriber.onBufferTransferFinish(new ElapsedTime(bufferTransferStopwatch)));
  }

  @Override
  public void startComputation() {
    subscribers.forEach(Subscriber::onComputationStart);
    computationStopwatch.start();
  }

  @Override
  public void updateComputationProgress(int current, int max) {
    subscribers.forEach(subscriber -> subscriber.onComputationProgress(new ElapsedTime(computationStopwatch), current, max));
  }

  @Override
  public void finishComputation(double result) {
    computationStopwatch.stop();
    subscribers.forEach(subscriber -> subscriber.onComputationFinish(new ElapsedTime(computationStopwatch), result));
  }

  @Override
  public void info(String info) {
    subscribers.forEach(subscriber -> subscriber.onInfo(info));
  }

  @Override
  public void exception(Exception e) {
    subscribers.forEach(subscriber -> subscriber.onException(e));
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


