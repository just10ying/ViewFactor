import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import events.EventManager;
import handlers.ConsoleLogger;
import handlers.FileLogger;
import org.j3d.loaders.stl.STLFileReader;
import viewfactor.ViewFactorCalculator;

import java.io.File;
import java.net.URISyntaxException;

public class Application {
  public interface Factory {
    Application create(String[] args);
  }

  @AssistedInject
  public Application(
      ViewFactorCalculator viewFactorCalculator,
      ServerConnection.Factory serverConnectionFactory,
      EventManager eventManager,
      ConsoleLogger consoleLogger,
      FileLogger fileLogger,
      @Assisted String[] args
  ) {
    eventManager.registerSubscriber(consoleLogger);
    eventManager.registerSubscriber(fileLogger);

    if (args.length >= 2) {
      // Manual run.
      File emitterFile = new File(args[0]);
      File receiverFile = new File(args[1]);
      File interconnectFile = args.length == 3 ? new File(args[2]) : null;

      try {
        STLFileReader emitterReader = new STLFileReader(emitterFile);
        STLFileReader receiverReader = new STLFileReader(receiverFile);
        STLFileReader interconnectReader = interconnectFile == null ? null : new STLFileReader(interconnectFile);
        viewFactorCalculator.run(emitterReader, receiverReader, interconnectReader);

        eventManager.shutdown();
        System.exit(0);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      // Remote run.
      serverConnectionFactory.create(args.length == 0 ? null : args[0]);
    }
  }

  // Bootstrap application.
  public static void main(String[] args) throws URISyntaxException {
    Injector injector = Guice.createInjector(new ApplicationModule());
    injector.getInstance(Application.Factory.class).create(args);
  }
}
