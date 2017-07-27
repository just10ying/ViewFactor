import com.google.inject.Inject;
import viewfactor.events.Event;
import viewfactor.events.EventManager;

public class WebsocketLogger implements EventManager.Subscriber {
  @Inject
  public WebsocketLogger() {}

  @Override
  public void onEvent(Event event) {

  }
}
