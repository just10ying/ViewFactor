package handlers;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LoggerModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(RemoteLogger.Factory.class));
  }

  @Provides @Singleton
  public FileLogger provideFileLogger() {
    return new FileLogger();
  }

  @Provides @Singleton
  public ConsoleLogger provideConsoleLogger() {
    return new ConsoleLogger();
  }
}
