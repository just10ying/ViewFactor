import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.istack.internal.Nullable;
import viewfactor.ViewFactorCalculator;
import viewfactor.state.MapLogger;
import viewfactor.state.StateManager;

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
  private final MapLogger mapLogger;
  private URL url;
  private Provider<ViewFactorCalculator> calculatorProvider;

  @Inject
  public ServerConnection(
      StateManager stateManager,
      MapLogger mapLogger,
      Provider<ViewFactorCalculator> calculator) {
    this.mapLogger = mapLogger;

    stateManager.registerSubscriber(mapLogger);
  }

  public void connect(@Nullable String urlString) {
    try {
      if (urlString == null) {
        url = new URL(DEFAULT_SERVER_URL);
      } else {
        url = new URL(urlString);
      }
      new Thread(this::loop).start();
    } catch (MalformedURLException e) {
      e.printStackTrace();
      System.out.println("Not connected.");
    }
  }

  private void loop() {
    while (true) {
      String response = sendParams(mapLogger.get());
      // Parse response to start a job.
      try {
        Thread.sleep(10000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

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
      e.printStackTrace();
      return null;
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

  private void startJob(String emitterUrl, String receiverUrl, String interconnectUrl) {
//    calculatorProvider.get().run();
  }
}
