import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import events.EventModule;
import handlers.LoggerModule;

public class ApplicationModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new LoggerModule());
    install(new EventModule());
    install(new FactoryModuleBuilder().build(Application.Factory.class));
  }
}
