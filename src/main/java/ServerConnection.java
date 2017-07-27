import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.istack.internal.Nullable;
import org.j3d.loaders.stl.STLFileReader;
import viewfactor.ViewFactorCalculator;
import viewfactor.events.EventManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class ServerConnection {

  private static final String DEFAULT_SERVER_URL = "https://just10ying.com/view-factor";
  private static final String EMITTER_URL_JSON_KEY = "emitter";
  private static final String RECEIVER_URL_JSON_KEY = "receiver";
  private static final String INTERCONNECT_URL_JSON_KEY = "interconnect";
  private static final int REQUEST_TIMEOUT = 30000;

  private final Provider<ViewFactorCalculator> calculatorProvider;
  private final WebsocketLogger websocketLogger;
  private final EventManager eventManager;

  private URL url;

  @Inject
  public ServerConnection(
      EventManager eventManager,
      WebsocketLogger websocketLogger,
      Provider<ViewFactorCalculator> calculatorProvider) {
    this.websocketLogger = websocketLogger;
    this.eventManager = eventManager;
    this.calculatorProvider = calculatorProvider;
    eventManager.registerSubscriber(websocketLogger);
  }

  void connect(@Nullable String urlString) {
    try {
      if (urlString == null) {
        url = new URL(DEFAULT_SERVER_URL);
      } else {
        url = new URL(urlString);
      }
//      new Thread(this::loop).start();
    } catch (MalformedURLException e) {
      e.printStackTrace();
      System.out.println("Shutting down.");
    }
  }
//
//  private void loop() {
//    while (true) {
//      try {
//        JSONObject jsonResponse = new JSONObject(sendParams(websocketLogger.get()));
//        if (eventManager.getState() == EventManager.State.IDLE
//            || eventManager.getState() == EventManager.State.EXCEPTION
//            && !jsonResponse.toString().isEmpty()) {
//          startJob(
//              jsonResponse.getString(EMITTER_URL_JSON_KEY),
//              jsonResponse.getString(RECEIVER_URL_JSON_KEY),
//              jsonResponse.optString(INTERCONNECT_URL_JSON_KEY)
//          );
//        }
//        Thread.sleep(REQUEST_TIMEOUT);
//      } catch (Exception e) {
//        eventManager.exception(e);
//      }
//    }
//  }

  private String sendParams(Map<String, String> params) {
    try {
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setDoOutput(true);
      DataOutputStream out = new DataOutputStream(con.getOutputStream());
      out.writeBytes(getParamsString(params));
      out.flush();
      out.close();

      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuilder content = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
      in.close();
      con.disconnect();
      return content.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
    StringBuilder result = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
      result.append("=");
      result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
      result.append("&");
    }
    String resultString = result.toString();
    return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
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
