package handlers;

import com.google.inject.Inject;
import events.Event;
import events.EventManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger implements EventManager.Subscriber {

  private static final String LOG_FILENAME = "output.log";

  private FileWriter outputFileWriter;

  @Inject
  public FileLogger() {
    try {
      outputFileWriter = new FileWriter(new File(LOG_FILENAME), true);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error: cannot log to file. Calculations will continue.");
    }
  }

  @Override
  public void onEvent(Event event) {
    if (outputFileWriter != null) {
      try {
        outputFileWriter.append(event.getDateTime()).append(": ").append(event.getMessage()).append("\r\n");
        outputFileWriter.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
