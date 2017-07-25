import com.google.inject.Inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.DoubleStream;

class ThreadedAdder {

  private static final int NUM_THREADS = 4;

  private ExecutorService threadPool;
  private DoubleAdder sum;

  @Inject
  ThreadedAdder(DoubleAdder sum) {
    threadPool = Executors.newFixedThreadPool(NUM_THREADS);
    this.sum = sum;
  }

  void add(double[] toAdd) {
    // Copy the array, as it may be overwritten by the time it is added.
    double[] localCopy = toAdd.clone();
    threadPool.submit(() -> sum.add(DoubleStream.of(localCopy).sum()));
  }

  double finishAndGet() {
    threadPool.shutdown();
    try {
      threadPool.awaitTermination(5, TimeUnit.HOURS);
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
    return sum.doubleValue();
  }
}
