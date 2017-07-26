import com.google.inject.Guice;
import com.google.inject.Injector;
import org.j3d.loaders.stl.STLFileReader;
import viewfactor.ViewFactorCalculator;
import viewfactor.ViewFactorModule;

import java.io.File;
import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new ViewFactorModule());
    if (args.length >= 2) {
      File emitterFile = new File(args[0]);
      File receiverFile = new File(args[1]);
      File interconnectFile = args.length == 3 ? new File(args[2]) : null;

      try {
        STLFileReader emitterReader = new STLFileReader(emitterFile);
        STLFileReader receiverReader = new STLFileReader(receiverFile);
        STLFileReader interconnectReader = interconnectFile == null ? null : new STLFileReader(interconnectFile);
        injector.getInstance(ViewFactorCalculator.class).run(emitterReader, receiverReader, interconnectReader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      ServerConnection serverConnection = injector.getInstance(ServerConnection.class);
      serverConnection.connect(args.length == 0 ? null : args[0]);
    }
  }
}
