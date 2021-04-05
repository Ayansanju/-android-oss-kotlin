package com.kickstarter.libs.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.appevents.AppEventsLogger;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ErrorEnvelope;

import java.util.Objects;

import javax.inject.Inject;

public final class ApplicationLifecycleUtil implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
  protected @Inject ApiClientType client;
  protected @Inject CurrentConfigType config;
  protected @Inject CurrentUserType currentUser;
  protected @Inject Logout logout;

  private final KSApplication application;
  private boolean isInBackground = true;

  private Config currentConfig;

  public ApplicationLifecycleUtil(final @NonNull KSApplication application) {
    this.application = application;
    application.component().inject(this);
  }

  @Override
  public void onActivityCreated(final @NonNull Activity activity, final @Nullable Bundle bundle) {
  }

  @Override
  public void onActivityStarted(final @NonNull Activity activity) {
  }

  @Override
  public void onActivityResumed(final @NonNull Activity activity) {
    if(this.isInBackground) {
      // Facebook: logs 'install' and 'app activate' App Events.
      AppEventsLogger.activateApp(activity.getApplication());

      //to get the current config for segment feature when app resume from background
      this.config.observable().subscribe(c -> {
        this.currentConfig = c;
      });

      // Refresh the config file
      this.client.config()
              .compose(Transformers.pipeApiErrorsTo(this::handleConfigApiError))
              .compose(Transformers.neverError())
              .subscribe(c -> {
                if (Objects.requireNonNull(c.features()).containsKey(ConfigFeatureName.SEGMENT_ENABLED.getConfigFeatureName())) {
                  final boolean isChecked = Objects.requireNonNull(this.currentConfig.features()).get(ConfigFeatureName.SEGMENT_ENABLED.getConfigFeatureName());
                  Objects.requireNonNull(c.features()).put(ConfigFeatureName.SEGMENT_ENABLED.getConfigFeatureName(), isChecked);
                }

                this.config.config(c);
              });

      // Refresh the user
      final String accessToken = this.currentUser.getAccessToken();
      if (ObjectUtils.isNotNull(accessToken)) {
        this.client.fetchCurrentUser()
                .compose(Transformers.neverError())
                .subscribe(u -> this.currentUser.refresh(u));
      }

      this.isInBackground = false;
    }
  }

  /**
   * Handles a config API error by logging the user out in the case of a 401. We will interpret
   * 401's on the config request as meaning the user's current access token is no longer valid,
   * as that endpoint should never 401 othewise.
   */
  private void handleConfigApiError(final @NonNull ErrorEnvelope error) {
    if (error.httpCode() == 401) {
      this.logout.execute();
      ApplicationUtils.startNewDiscoveryActivity(this.application);
    }
  }

  @Override
  public void onActivityPaused(final @NonNull Activity activity) {
    // Facebook: logs 'app deactivate' App Event.
    AppEventsLogger.deactivateApp(activity);
  }

  @Override
  public void onActivityStopped(final @NonNull Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(final @NonNull Activity activity, final @Nullable Bundle bundle) {
  }

  @Override
  public void onActivityDestroyed(final @NonNull Activity activity) {
  }

  @Override
  public void onConfigurationChanged(final @NonNull Configuration configuration) {
  }

  @Override
  public void onLowMemory() {
  }

  /**
   * Memory availability callback. TRIM_MEMORY_UI_HIDDEN means the app's UI is no longer visible.
   * This is triggered when the user navigates out of the app and primarily used to free resources used by the UI.
   * http://developer.android.com/training/articles/memory.html
   */
  @Override
  public void onTrimMemory(final int i) {
    if(i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
      this.isInBackground = true;
    }
  }
}
