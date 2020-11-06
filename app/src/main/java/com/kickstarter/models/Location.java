package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import androidx.annotation.Nullable;

import java.util.Objects;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Location implements Parcelable, Relay {
  public abstract long id();
  public abstract @Nullable String city();
  public abstract String country();
  public abstract String displayableName();
  public abstract @Nullable String expandedCountry();
  public abstract String name();
  public abstract @Nullable Integer projectsCount();
  public abstract @Nullable String state();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder displayableName(String __);
    public abstract Builder id(long __);
    public abstract Builder city(String __);
    public abstract Builder country(String __);
    public abstract Builder expandedCountry(String __);
    public abstract Builder name(String __);
    public abstract Builder state(String __);
    public abstract Builder projectsCount(Integer __);
    public abstract Location build();
  }

  public static Builder builder() {
    return new AutoParcel_Location.Builder();
  }

  public abstract Builder toBuilder();

  @Override
  public boolean equals(final @Nullable Object obj) {
    boolean equals = super.equals(obj);

    if (obj instanceof Location) {
      final Location otherLoc = (Location) obj;
      equals = Objects.equals(this.id(), otherLoc.id()) &&
            Objects.equals(this.city(), otherLoc.city()) &&
            Objects.equals(this.country(), otherLoc.country()) &&
            Objects.equals(this.displayableName(), otherLoc.displayableName()) &&
            Objects.equals(this.expandedCountry(), otherLoc.expandedCountry()) &&
            Objects.equals(this.name(), otherLoc.name()) &&
            Objects.equals(this.projectsCount(), otherLoc.projectsCount()) &&
            Objects.equals(this.state(), otherLoc.state());
    }

    return equals;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
