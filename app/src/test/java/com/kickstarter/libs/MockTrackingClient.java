package com.kickstarter.libs;

import com.kickstarter.libs.utils.ConfigUtils;
import com.kickstarter.models.User;

import org.joda.time.DateTime;
import org.json.JSONArray;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;
import rx.subjects.PublishSubject;

public final class MockTrackingClient extends TrackingClientType {
  private static final long DEFAULT_TIME = DateTime.parse("2018-11-02T18:42:05Z").getMillis() / 1000;
  private final boolean cleanPropertiesOnly;
  @Nullable private User loggedInUser;
  @Nullable private Config config;

  public MockTrackingClient(final @NonNull CurrentUserType currentUser, final @NonNull CurrentConfigType currentConfig, final boolean cleanPropertiesOnly) {
    this.cleanPropertiesOnly = cleanPropertiesOnly;
    currentUser.observable().subscribe(u -> this.loggedInUser = u);
    currentConfig.observable().subscribe(c -> this.config = c);
  }

  public static class Event {
    private final String name;
    private final Map<String, Object> properties;
    Event(final String name, final Map<String, Object> properties) {
      this.name = name;
      this.properties = properties;
    }
  }

  private final @NonNull PublishSubject<Event> events = PublishSubject.create();
  public final @NonNull Observable<String> eventNames = this.events.map(e -> e.name);
  public final @NonNull Observable<Map<String, Object>> eventProperties = this.events.map(e -> e.properties);

  @Override
  protected boolean cleanPropertiesOnly() {
    return this.cleanPropertiesOnly;
  }

  @Override
  public void track(final @NonNull String eventName, final @NonNull Map<String, Object> additionalProperties) {
    this.events.onNext(new Event(eventName, combinedProperties(additionalProperties)));
  }

  //Default property values
  @Override
  protected String brand() {
    return "Google";
  }

  @Override
  protected int buildNumber() {
    return 9999;
  }

  @Override
  protected JSONArray currentVariants() {
    return ConfigUtils.INSTANCE.currentVariants(this.config);
  }

  @Override
  protected String deviceDistinctId() {
    return "uuid";
  }

  @Override
  protected String deviceFormat() {
    return "phone";
  }

  @Override
  protected String deviceOrientation() {
    return "Portrait";
  }

  @Override
  protected JSONArray enabledFeatureFlags() {
    return ConfigUtils.INSTANCE.enabledFeatureFlags(this.config);
  }

  @Override
  protected boolean isGooglePlayServicesAvailable() {
    return false;
  }

  @Override
  protected boolean isTalkBackOn() {
    return false;
  }

  @Override
  protected String manufacturer() {
    return "Google";
  }

  @Override
  protected String model() {
    return "Pixel 3";
  }

  @Override
  protected String OSVersion() {
    return "9";
  }

  @Override
  protected long time() {
    return DEFAULT_TIME;
  }

  @Override
  protected User loggedInUser() {
    return this.loggedInUser;
  }

  @Override
  protected String userAgent() {
    return "agent";
  }

  @Override
  protected String versionName() {
    return "9.9.9";
  }

  @Override
  protected boolean wifiConnection() {
    return false;
  }
}
