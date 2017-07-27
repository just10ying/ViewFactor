package viewfactor.events;

import com.google.common.base.Stopwatch;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class Event {

  private static final String TYPE_JSON_KEY = "type";
  private static final String MESSAGE_JSON_KEY = "message";
  private static final String TIMESTAMP_JSON_KEY = "timestamp";

  private final long timestamp;
  Event() {
    this.timestamp = System.currentTimeMillis();
  }

  abstract String getMessage();

  public long getTimestamp() {
    return timestamp;
  }

  String getDateTime() {
    return new SimpleDateFormat("[MMM dd, yyyy HH:mm:ss]").format(new Date(timestamp));
  }

  public JSONObject toJson() {
    JSONObject thisJson = new JSONObject();
    thisJson.put(TYPE_JSON_KEY, this.getClass().getSimpleName());
    thisJson.put(MESSAGE_JSON_KEY, getMessage());
    thisJson.put(TIMESTAMP_JSON_KEY, timestamp);
    return thisJson;
  }

  private static String getReasonableTimeUnit(Stopwatch stopwatch) {
    for (TimeUnit unit : TimeUnit.values()) {
      long value = stopwatch.elapsed(unit);
      if (value > 9999)  {
        continue;
      }
      return value + " " + unit.name().toLowerCase() + ".";
    }
    return "Error: time unit too large.";
  }

  static class JobStart extends Event {
    @Override
    String getMessage() {
      return "Initializing...";
    }
  }

  static class JobFinish extends Event {
    private final Stopwatch allStopWatch;

    JobFinish(Stopwatch allStopWatch) {
      this.allStopWatch = allStopWatch;
    }

    @Override
    String getMessage() {
      return "All operations completed in: " + getReasonableTimeUnit(allStopWatch);
    }
  }

  static class ParseStlStart extends Event {
    @Override
    String getMessage() {
      return "Reading STL files and precomputing geometry...";
    }
  }

  static class ParseStlFinish extends Event {
    private final Stopwatch stlStopwatch;

    ParseStlFinish(Stopwatch stlStopwatch) {
      this.stlStopwatch = stlStopwatch;
    }

    @Override
    String getMessage() {
      return "STLs parsed and geometry precomputed in: " + getReasonableTimeUnit(stlStopwatch);
    }
  }

  static class BufferTransferStart extends Event {
    @Override
    String getMessage() {
      return "Transferring initial buffers to GPU...";
    }
  }

  static class BufferTransferFinish extends Event {
    private final Stopwatch bufferTransferStopwatch;

    BufferTransferFinish(Stopwatch bufferTransferStopwatch) {
      this.bufferTransferStopwatch = bufferTransferStopwatch;
    }

    @Override
    String getMessage() {
      return "Initial buffer transfer complete in: " + getReasonableTimeUnit(bufferTransferStopwatch);
    }
  }

  static class ComputationStart extends Event {
    @Override
    String getMessage() {
      return "Beginning GPU computation of view factors...";
    }
  }

  public static class ComputationProgress extends Event {
    private static final DecimalFormat PERCENTAGE_FORMATTER = new DecimalFormat("##.##%");
    private static final String CURRENT_JOB_JSON_KEY = "current_job";
    private static final String NUM_JOBS_JSON_KEY = "num_jobs";

    private final int currentJob;
    private final int numJobs;

    ComputationProgress(int currentJob, int numJobs) {
      this.currentJob = currentJob;
      this.numJobs = numJobs;
    }

    @Override
    public JSONObject toJson() {
      return super.toJson().put(CURRENT_JOB_JSON_KEY, currentJob).put(NUM_JOBS_JSON_KEY, numJobs);
    }

    @Override
    String getMessage() {
      double percent = (double) currentJob / (double) numJobs;
      String fraction = "(" + currentJob + "/" + numJobs + ").";
      return "GPU computation is " + PERCENTAGE_FORMATTER.format(percent) + " complete " + fraction;
    }
  }

  static class ComputationFinish extends Event {
    private final Stopwatch computationStopwatch;
    private final double result;

    ComputationFinish(Stopwatch computationStopwatch, double result) {
      this.computationStopwatch = computationStopwatch;
      this.result = result;
    }

    @Override
    String getMessage() {
      return "GPU computation finished in: " + getReasonableTimeUnit(computationStopwatch) + "\n"
          + "Result: " + result;
    }
  }

  static class Info extends Event {
    private final String info;

    Info(String info) {
      this.info = info;
    }

    @Override
    String getMessage() {
      return info;
    }
  }

  static class ExceptionThrown extends Event {
    private final Exception exception;

    ExceptionThrown(Exception exception) {
      this.exception = exception;
    }

    @Override
    String getMessage() {
      return exception.getMessage();
    }
  }
}
