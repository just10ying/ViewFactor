import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.istack.internal.Nullable;
import logger.RemoteLogger;
import org.j3d.loaders.stl.STLFileReader;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import viewfactor.ViewFactorCalculator;
import viewfactor.events.EventManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ServerConnection {

  private static final String DEFAULT_SERVER_URL = "ws://158.69.215.240:8124";
  private static final String EMITTER_URL_JSON_KEY = "emitter";
  private static final String RECEIVER_URL_JSON_KEY = "receiver";
  private static final String INTERCONNECT_URL_JSON_KEY = "interconnect";

  private final Provider<ViewFactorCalculator> calculatorProvider;
  private final RemoteLogger.Factory remoteLogger;
  private final EventManager eventManager;

  private WebSocketClient websocketClient;
  private URI url;

  @Inject
  public ServerConnection(
      EventManager eventManager,
      RemoteLogger.Factory remoteLogger,
      Provider<ViewFactorCalculator> calculatorProvider) {
    this.eventManager = eventManager;
    this.remoteLogger = remoteLogger;
    this.calculatorProvider = calculatorProvider;
  }

  void connect(@Nullable String urlString) {
    try {
      if (urlString == null) {
        url = new URI(DEFAULT_SERVER_URL);
      } else {
        url = new URI(urlString);
      }
      websocketClient = new WebSocketClient(url, new Draft_17()) {
        @Override
        public void onOpen(ServerHandshake handshakedata) {
          System.out.println("Success!");
          websocketClient.send("Hello world!");
        }

        @Override
        public void onMessage(String message) {
          System.out.println(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
          System.out.println(reason);
        }

        @Override
        public void onError(Exception ex) {
          System.out.println(ex.toString());
        }
      };
      // TODO(justinying): web socket reconnect
      websocketClient.connect();
      eventManager.registerSubscriber(remoteLogger.create(websocketClient::send));
    } catch (URISyntaxException e) {
      e.printStackTrace();
      System.out.println("Shutting down.");
    }
  }

  private void startJob(String emitterUrl, String receiverUrl, String interconnectUrl) throws Exception {
    STLFileReader emitterReader = new STLFileReader(new URL(emitterUrl));
    STLFileReader receiverReader = new STLFileReader(new URL(receiverUrl));
    STLFileReader interconnectReader = interconnectUrl.isEmpty() ? null : new STLFileReader(new URL(interconnectUrl));
    Thread calculatorThread = new Thread(() -> calculatorProvider.get().run(emitterReader, receiverReader, interconnectReader));
    calculatorThread.setUncaughtExceptionHandler((t, e) -> eventManager.exception(new Exception(e)));
    calculatorThread.start();
  }
}