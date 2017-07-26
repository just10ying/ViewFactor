import com.google.inject.Guice;
import com.google.inject.Injector;
import org.j3d.loaders.stl.STLFileReader;
import viewfactor.ViewFactorCalculator;
import viewfactor.ViewFactorModule;

import java.io.File;
import java.io.IOException;

public class Main {

  public static void main(String[] args) {
    run();
  }

  public static void run() {
    try {
      STLFileReader emitterReader = new STLFileReader(new File("stl/bottom.stl"));
      STLFileReader receiverReader = new STLFileReader(new File("stl/top.stl"));
      STLFileReader interconnectReader = null;
      interconnectReader = new STLFileReader(new File("stl/top.stl"));

      Injector injector = Guice.createInjector(new ViewFactorModule());
      injector.getInstance(ViewFactorCalculator.class).run(emitterReader, receiverReader, interconnectReader);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
