package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.User;

import java.util.Objects;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class AccessTokenEnvelope implements Parcelable {
  public abstract String accessToken();
  public abstract User user();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder accessToken(String __);
    public abstract Builder user(User __);
    public abstract AccessTokenEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_AccessTokenEnvelope.Builder();
  }

  public abstract Builder toBuilder();

  @Override
  public boolean equals(final @Nullable Object obj) {
    boolean equals = super.equals(obj);

    if (obj instanceof AccessTokenEnvelope) {
      final AccessTokenEnvelope otherEnvelope = (AccessTokenEnvelope) obj;
      equals = Objects.equals(this.accessToken(), otherEnvelope.accessToken()) &&
            Objects.equals(this.user(), otherEnvelope.user());
    }

    return equals;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
