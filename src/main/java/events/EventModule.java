package events;

import com.google.common.base.Stopwatch;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class EventModule extends AbstractModule {
  @Override
  protected void configure() {

  }

  @Provides @Singleton
  EventManager provideStateManager(Provider<Stopwatch> timerProvider) {
    return new EventManager(timerProvider);
  }
}
