package viewfactor;

import com.google.common.base.Stopwatch;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import viewfactor.events.ConsoleLogger;
import viewfactor.events.EventManager;

public class ViewFactorModule extends AbstractModule {
  @Override
  protected void configure() {

  }

  @Provides @Singleton
  EventManager provideStateManager(Provider<Stopwatch> timerProvider, ConsoleLogger consoleLogger) {
    return new EventManager(timerProvider, consoleLogger);
  }
}
