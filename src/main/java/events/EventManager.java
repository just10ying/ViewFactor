package events;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EventManager implements KernelEvents {

  public interface Subscriber {
    void onEvent(Event event);
  }

  private enum State {
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

  @SuppressWarnings("unchecked cast")
  private static final Map<State, State> VALID_STATES = (HashMap<State, State>) new HashMap() {{
    put(State.IDLE, State.INIT);
    put(State.EXCEPTION, State.INIT);
    put(State.INIT, State.PARSE_STL);
    put(State.PARSE_STL, State.POST_PARSE_STL);
    put(State.POST_PARSE_STL, State.BUFFER_TRANSFER);
    put(State.BUFFER_TRANSFER, State.POST_BUFFER_TRANSFER);
    put(State.POST_BUFFER_TRANSFER, State.COMPUTATION);
    put(State.COMPUTATION, State.POST_COMPUTATION);
    put(State.POST_COMPUTATION, State.IDLE);
  }};

  private final Provider<Stopwatch> stopwatchProvider;
  private final List<Event> events;
  private final List<Subscriber> subscribers;
  private final ExecutorService executors;

  private Stopwatch allStopWatch;
  private Stopwatch stlStopwatch;
  private Stopwatch bufferTransferStopwatch;
  private Stopwatch computationStopwatch;

  private State state;

  @Inject
  public EventManager(Provider<Stopwatch> stopwatchProvider) {
    this.stopwatchProvider = stopwatchProvider;
    subscribers = new ArrayList<>();
    events = new ArrayList<>();
    state = State.IDLE;
    executors = Executors.newFixedThreadPool(1);
  }

  public void registerSubscriber(Subscriber subscriber) {
    subscribers.add(subscriber);
  }

  @Override
  public void start() {
    changeState(State.INIT);

    allStopWatch = stopwatchProvider.get();
    stlStopwatch = stopwatchProvider.get();
    bufferTransferStopwatch = stopwatchProvider.get();
    computationStopwatch = stopwatchProvider.get();

    recordAndAlertSubscribers(new Event.JobStart());

    allStopWatch.start();
  }

  @Override
  public void startParseStl() {
    changeState(State.PARSE_STL);
    recordAndAlertSubscribers(new Event.ParseStlStart());
    stlStopwatch.start();
  }

  @Override
  public void finishParseStl() {
    changeState(State.POST_PARSE_STL);
    stlStopwatch.stop();
    recordAndAlertSubscribers(new Event.ParseStlFinish(stlStopwatch));
  }

  @Override
  public void startBufferTransfer() {
    changeState(State.BUFFER_TRANSFER);
    recordAndAlertSubscribers(new Event.BufferTransferStart());
    bufferTransferStopwatch.start();
  }

  @Override
  public void finishBufferTransfer() {
    changeState(State.POST_BUFFER_TRANSFER);
    bufferTransferStopwatch.stop();
    recordAndAlertSubscribers(new Event.BufferTransferFinish(bufferTransferStopwatch));
  }

  @Override
  public void startComputation() {
    changeState(State.COMPUTATION);
    recordAndAlertSubscribers(new Event.ComputationStart());
    computationStopwatch.start();
  }

  @Override
  public void updateComputationProgress(int current, int max) {
    recordAndAlertSubscribers(new Event.ComputationProgress(current, max));
  }

  @Override
  public void finishComputation(double result) {
    changeState(State.POST_COMPUTATION);
    computationStopwatch.stop();
    recordAndAlertSubscribers(new Event.ComputationFinish(computationStopwatch, result));
  }

  @Override
  public void finish() {
    changeState(State.IDLE);
    allStopWatch.stop();
    recordAndAlertSubscribers(new Event.JobFinish(allStopWatch));
  }

  @Override
  public void info(String info) {
    recordAndAlertSubscribers(new Event.Info(info));
  }

  @Override
  public void exception(Exception e) {
    changeState(State.EXCEPTION);
    recordAndAlertSubscribers(new Event.ExceptionThrown(e));
  }

  public void shutdown() {
    executors.shutdown();
    try {
      executors.awaitTermination(5, TimeUnit.HOURS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void changeState(State newState) {
    Preconditions.checkState(VALID_STATES.get(state) == newState || newState == State.EXCEPTION);
    state = newState;
  }

  private void recordAndAlertSubscribers(Event event) {
    events.add(event);
    executors.execute(() -> subscribers.forEach(subscriber -> subscriber.onEvent(event)));
  }
}


