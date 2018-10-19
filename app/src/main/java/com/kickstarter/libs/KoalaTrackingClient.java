package com.kickstarter.libs;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.util.Base64Utils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kickstarter.BuildConfig;
import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.KoalaUtils;
import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.KoalaBackgroundService;
import com.kickstarter.services.firebase.DispatcherKt;
import com.kickstarter.ui.IntentKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public final class KoalaTrackingClient extends TrackingClientType {
  private static final String TAG = KoalaTrackingClient.class.getSimpleName();
  @Inject CurrentUserType currentUser;
  @Inject AndroidPayCapability androidPayCapability;
  @Inject Build build;
  @Nullable private User loggedInUser;
  private final @NonNull Context context;

  // Cached values
  private @Nullable Boolean isGooglePlayServicesAvailable;

  public KoalaTrackingClient(
    final @ApplicationContext @NonNull Context context,
    final @NonNull CurrentUserType currentUser,
    final @NonNull AndroidPayCapability androidPayCapability,
    final @NonNull Build build) {

    this.context = context;
    this.currentUser = currentUser;
    this.androidPayCapability = androidPayCapability;
    this.build = build;

    // Cache the most recent logged in user for default Koala properties.
    this.currentUser.observable().subscribe(u -> this.loggedInUser = u);
  }

  @Override
  public void track(final @NonNull String eventName, final @NonNull Map<String, Object> properties) {
    final Map<String, Object> newProperties = new HashMap<>(properties);
    newProperties.putAll(defaultProperties());

    try {
      final String trackingDataString = getTrackingDataString(eventName, newProperties);
      final Bundle bundle = new Bundle();
      bundle.putString(IntentKey.KOALA_EVENT_NAME, eventName);
      bundle.putString(IntentKey.KOALA_EVENT, trackingDataString);

      final String uniqueJobName = KoalaBackgroundService.BASE_JOB_NAME + System.currentTimeMillis();
      DispatcherKt.dispatchJob(this.context, KoalaBackgroundService.class, uniqueJobName, bundle);
    } catch (JSONException e) {
      if (this.build.isDebug()) {
        Timber.e("Failed to encode event: " + eventName);
      }
      Fabric.getLogger().e(KoalaTrackingClient.TAG, "Failed to encode event: " + eventName);
    }
  }

  private String getTrackingDataString(final @NonNull String eventName, final @NonNull Map<String, Object> newProperties) throws JSONException {
    final JSONObject trackingEvent = new JSONObject();
    trackingEvent.put("event", eventName);

    final Map<String, Object> compactProperties = MapUtils.compact(newProperties);
    final JSONObject propertiesJSON = new JSONObject();
    for (Map.Entry<String, Object> entry : compactProperties.entrySet()) {
      propertiesJSON.put(entry.getKey(), entry.getValue());
    }
    trackingEvent.put("properties", propertiesJSON);
    final JSONArray trackingArray = new JSONArray();
    trackingArray.put(trackingEvent);

    return Base64Utils.encodeUrlSafe(trackingArray.toString().getBytes());
  }

  @NonNull
  @Override
  public Map<String, Object> defaultProperties() {
    final Map<String, Object> hashMap = new HashMap<>();

    if (this.loggedInUser != null) {
      hashMap.putAll(KoalaUtils.userProperties(this.loggedInUser));
    }

    hashMap.put("android_pay_capable", this.androidPayCapability.isCapable());
    hashMap.put("android_uuid", FirebaseInstanceId.getInstance().getId());
    hashMap.put("app_version", BuildConfig.VERSION_NAME);
    hashMap.put("brand", android.os.Build.BRAND);
    hashMap.put("client_platform", "android");
    hashMap.put("client_type", "native");
    hashMap.put("device_fingerprint", FirebaseInstanceId.getInstance().getId());
    hashMap.put("device_format", deviceFormat());
    hashMap.put("device_orientation", deviceOrientation());
    hashMap.put("distinct_id", FirebaseInstanceId.getInstance().getId());
    hashMap.put("google_play_services", isGooglePlayServicesAvailable() ? "available" : "unavailable");
    hashMap.put("koala_lib", "kickstarter_android");
    hashMap.put("manufacturer", android.os.Build.MANUFACTURER);
    hashMap.put("model", android.os.Build.MODEL);
    hashMap.put("mp_lib", "android");
    hashMap.put("os", "Android");
    hashMap.put("os_version", android.os.Build.VERSION.RELEASE);
    hashMap.put("time", System.currentTimeMillis());
    hashMap.put("user_logged_in", this.loggedInUser != null);

    return hashMap;
  }

  /**
   * Derives the device's orientation (portrait/landscape) from the `context`.
   */
  private @NonNull String deviceOrientation() {
    if (this.context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      return "landscape";
    }
    return "portrait";
  }

  private @NonNull String deviceFormat() {
    return this.context.getResources().getBoolean(R.bool.isTablet) ? "tablet" : "phone";
  }

  /**
   * Derives the availability of google play services from the `context`.
   */
  private boolean isGooglePlayServicesAvailable() {
    if (this.isGooglePlayServicesAvailable == null) {
      this.isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.context.getApplicationContext()) != ConnectionResult.SUCCESS;
    }
    return this.isGooglePlayServicesAvailable;
  }
}
