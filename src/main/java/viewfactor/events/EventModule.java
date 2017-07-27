package viewfactor.events;

import com.google.common.base.Stopwatch;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import logger.ConsoleLogger;

public class EventModule extends AbstractModule {
  @Override
  protected void configure() {

  }

  @Provides @Singleton
  EventManager provideStateManager(Provider<Stopwatch> timerProvider, ConsoleLogger consoleLogger) {
    return new EventManager(timerProvider, consoleLogger);
  }
}
