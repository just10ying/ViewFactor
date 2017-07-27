package viewfactor.events;

import com.google.inject.Inject;

public class ConsoleLogger implements EventManager.Subscriber{

  @Inject
  public ConsoleLogger() {}

  @Override
  public void onEvent(Event event) {
    System.out.println(event.getDateTime() + ": " + event.getMessage());
  }
}
