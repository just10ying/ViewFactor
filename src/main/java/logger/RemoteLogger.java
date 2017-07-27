package logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import viewfactor.events.Event;
import viewfactor.events.EventManager;

public class RemoteLogger implements EventManager.Subscriber {

  public interface RemoteConnection {
    void send(String message);
  }

  public interface Factory {
    RemoteLogger create(RemoteConnection connection);
  }

  private final RemoteConnection connection;

  @AssistedInject
  public RemoteLogger(@Assisted RemoteConnection connection) {
    this.connection = connection;
  }

  @Override
  public void onEvent(Event event) {
    connection.send(event.toJson().toString());
  }
}
