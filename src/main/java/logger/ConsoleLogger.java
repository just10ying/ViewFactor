package logger;

import com.google.inject.Inject;
import viewfactor.events.Event;
import viewfactor.events.EventManager;

public class ConsoleLogger implements EventManager.Subscriber {

  @Inject
  public ConsoleLogger() {}

  @Override
  public void onEvent(Event event) {
    System.out.println(event.getDateTime() + ": " + event.getMessage());
  }
}
