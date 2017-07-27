package logger;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LoggerModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(RemoteLogger.Factory.class));
  }
}
