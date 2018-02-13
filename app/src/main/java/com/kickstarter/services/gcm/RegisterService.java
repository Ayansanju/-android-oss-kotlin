package com.kickstarter.services.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.ApiClientType;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

public class RegisterService extends IntentService {
  protected @Inject ApiClientType apiClient;
  protected @Inject CurrentUserType currentUser;

  public RegisterService() {
    super("RegisterService");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ((KSApplication) getApplicationContext()).component().inject(this);
  }

  @Override
  protected void onHandleIntent(final @Nullable Intent intent) {
    Timber.d("onHandleIntent");

    try {
      // This initially hits the network to retrieve the token, subsequent calls are local
      final InstanceID instanceID = InstanceID.getInstance(this);

      // R.string.gcm_defaultSenderId is derived from google-services.json
      final String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
      Timber.d("Token: %s", token);

      sendTokenToApi(token);
      subscribeToGlobalTopic(token);
    } catch (final Exception e) {
      Timber.e("Failed to complete token refresh: %s", e);
    }
  }

  /**
   * Persist token to app servers.
   *
   * @param token The new token.
   */
  private void sendTokenToApi(final @NonNull String token) {
    this.currentUser.observable()
      .take(1)
      .filter(ObjectUtils::isNotNull)
      .subscribe(__ ->
        this.apiClient.registerPushToken(token)
          .compose(Transformers.neverError())
          .toList().toBlocking().single()
      );
  }

  /**
   * Subscribe to generic global topic - not using more specific topics.
   *
   * @throws IOException if unable to reach the GCM PubSub service
   */
  private void subscribeToGlobalTopic(final @NonNull String token) throws IOException {
    GcmPubSub.getInstance(this).subscribe(token, "/topics/global", null);
  }
}

