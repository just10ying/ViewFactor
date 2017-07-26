package viewfactor.events;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventManager implements Events {

  public interface Subscriber {
    void onParseStlStart();
    void onParseStlFinish(ElapsedTime stopwatch);
    void onBufferTransferStart();
    void onBufferTransferFinish(ElapsedTime stopwatch);
    void onComputationStart();
    void onComputationProgress(ElapsedTime stopwatch, int current, int max);
    void onComputationFinish(ElapsedTime stopwatch, double result);
    void onInfo(String info);
    void onException(Exception e);
  }

  private Stopwatch stlStopwatch;
  private Stopwatch bufferTransferStopwatch;
  private Stopwatch computationStopwatch;
  private List<Subscriber> subscribers;

  @Inject
  public EventManager(Provider<Stopwatch> stopwatchProvider) {
    stlStopwatch = stopwatchProvider.get();
    bufferTransferStopwatch = stopwatchProvider.get();
    computationStopwatch = stopwatchProvider.get();

    subscribers = new ArrayList<>();
  }

  public void addSubscriber(Subscriber subscriber) {
    subscribers.add(subscriber);
  }

  @Override
  public void startParseStl() {
    stlStopwatch.start();
    subscribers.forEach(Subscriber::onParseStlStart);
  }

  @Override
  public void finishParseStl() {
    stlStopwatch.stop();
    subscribers.forEach(subscriber -> subscriber.onParseStlFinish(new ElapsedTime(stlStopwatch)));
  }

  @Override
  public void startBufferTransfer() {
    bufferTransferStopwatch.start();
    subscribers.forEach(Subscriber::onBufferTransferStart);
  }

  @Override
  public void finishBufferTransfer() {
    bufferTransferStopwatch.stop();
    subscribers.forEach(subscriber -> subscriber.onBufferTransferFinish(new ElapsedTime(bufferTransferStopwatch)));
  }

  @Override
  public void startComputation() {
    computationStopwatch.start();
    subscribers.forEach(Subscriber::onComputationStart);
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


