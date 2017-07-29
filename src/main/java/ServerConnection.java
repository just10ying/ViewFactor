import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import events.Event;
import events.EventManager;
import org.j3d.loaders.stl.STLFileReader;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import viewfactor.ViewFactorCalculator;

import javax.annotation.Nullable;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnection extends WebSocketClient implements EventManager.Subscriber {

  private static final String DEFAULT_SERVER_URI = "ws://158.69.215.240:8124";
  private static final String REGISTER_JSON_KEY = "register";
  private static final String EMITTER_URL_JSON_KEY = "emitter";
  private static final String RECEIVER_URL_JSON_KEY = "receiver";
  private static final String INTERCONNECT_URL_JSON_KEY = "interconnect";

  private final Provider<ViewFactorCalculator> calculatorProvider;
  private final ExecutorService executorService;

  interface Factory {
    ServerConnection create(String serverUri);
  }

  @AssistedInject
  public ServerConnection(
      EventManager eventManager,
      Provider<ViewFactorCalculator> calculatorProvider,
      @Assisted @Nullable String serverUri) throws URISyntaxException {
    super(serverUri == null ? new URI(DEFAULT_SERVER_URI) : new URI(serverUri), new Draft_6455());

    this.calculatorProvider = calculatorProvider;
    executorService = Executors.newFixedThreadPool(1);
    connect();
    eventManager.registerSubscriber(this);
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    try {
      send(new JSONObject().put(REGISTER_JSON_KEY, InetAddress.getLocalHost().getHostName()).toString());
    } catch (UnknownHostException e) {
      System.out.println("Fatal error: cannot register with server.");
    }
  }

  @Override
  public void onMessage(String message) {
    JSONObject job = new JSONObject(message);
    try {
      startJob(
          job.get(EMITTER_URL_JSON_KEY).toString(),
          job.get(RECEIVER_URL_JSON_KEY).toString(),
          job.get(INTERCONNECT_URL_JSON_KEY).toString()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onEvent(Event event) {
    send(event.toJson().toString());
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    System.out.println("Server closed:" + reason);
  }

  @Override
  public void onError(Exception ex) {
    System.out.println(ex.toString());
  }

  private void startJob(String emitterUrl, String receiverUrl, String interconnectUrl) throws Exception {
    STLFileReader emitterReader = new STLFileReader(new URL(emitterUrl));
    STLFileReader receiverReader = new STLFileReader(new URL(receiverUrl));
    STLFileReader interconnectReader = interconnectUrl.isEmpty() ? null : new STLFileReader(new URL(interconnectUrl));
    executorService.execute(() -> calculatorProvider.get().run(emitterReader, receiverReader, interconnectReader));
  }
}