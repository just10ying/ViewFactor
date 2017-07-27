import com.google.inject.AbstractModule;
import logger.LoggerModule;
import viewfactor.events.EventModule;

public class ApplicationModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new LoggerModule());
    install(new EventModule());
  }
}
