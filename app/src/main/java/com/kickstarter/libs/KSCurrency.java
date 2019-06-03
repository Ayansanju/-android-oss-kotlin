package com.kickstarter.libs;

import android.os.Parcelable;

import com.kickstarter.libs.models.Country;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;

import java.math.RoundingMode;

import androidx.annotation.NonNull;
import auto.parcel.AutoParcel;

public final class KSCurrency {
  private final CurrentConfigType currentConfig;

  public KSCurrency(final @NonNull CurrentConfigType currentConfig) {
    this.currentConfig = currentConfig;
  }

  /**
   * Returns a currency string appropriate to the user's locale and location relative to a project.
   *
   * @param initialValue Value to display, local to the project's currency.
   * @param project      The project to use to look up currency information.
   */
  public @NonNull String format(final double initialValue, final @NonNull Project project, final @NonNull RoundingMode roundingMode) {
    return format(initialValue, project, true, roundingMode);
  }

  /**
   * Returns a currency string appropriate to the user's locale and location relative to a project.
   *
   * @param initialValue        Value to display, local to the project's currency.
   * @param project             The project to use to look up currency information.
   * @param excludeCurrencyCode If true, hide the US currency code for US users only.
   */
  public @NonNull String format(final double initialValue, final @NonNull Project project,
    final boolean excludeCurrencyCode) {

    return format(initialValue, project, excludeCurrencyCode, RoundingMode.DOWN);
  }

  /**
   * Returns a currency string appropriate to the user's locale and location relative to a project.
   *
   * @param initialValue        Value to display, local to the project's currency.
   * @param project             The project to use to look up currency information.
   * @param excludeCurrencyCode If true, hide the US currency code for US users only.
   */
  public @NonNull String format(final double initialValue, final @NonNull Project project,
    final boolean excludeCurrencyCode, final @NonNull RoundingMode roundingMode) {

    final Country country = Country.findByCurrencyCode(project.currency());
    if (country == null) {
      return "";
    }

    final float roundedValue = getRoundedValue(initialValue, roundingMode);
    final CurrencyOptions currencyOptions = currencyOptions(roundedValue, country, excludeCurrencyCode);

    final NumberOptions numberOptions = NumberOptions.builder()
      .currencyCode(currencyOptions.currencyCode())
      .currencySymbol(currencyOptions.currencySymbol())
      .roundingMode(roundingMode)
      .build();

    return trim(NumberUtils.format(currencyOptions.value(), numberOptions));
  }

  /**
   * Returns a currency string appropriate to the user's locale and preferred currency.
   *
   * @param initialValue Value to convert, local to the project's currency.
   * @param project The project to use to look up currency information.
   * @param roundingMode This determines whether we should round the values down or up.
   */
  public String formatWithUserPreference(final double initialValue, final @NonNull Project project,
    final @NonNull RoundingMode roundingMode) {

    final Country country = Country.findByCurrencyCode(project.currentCurrency());

    if (country == null) {
      return "";
    }

    final float convertedValue = getRoundedValue(initialValue, roundingMode) * project.fxRate();
    final CurrencyOptions currencyOptions = currencyOptions(convertedValue, country, true);

    final NumberOptions numberOptions = NumberOptions.builder()
      .currencySymbol(currencyOptions.currencySymbol())
      .roundingMode(roundingMode)
      .build();

    return trim(NumberUtils.format(currencyOptions.value(), numberOptions));
  }

  /**
   * Build {@link CurrencyOptions} based on the country.
   */
  private @NonNull CurrencyOptions currencyOptions(final float value, final @NonNull Country country, final boolean excludeCurrencyCode) {
    return CurrencyOptions.builder()
      .country(country.getCountryCode())
      .currencyCode("")
      .currencySymbol(getSymbolForCurrency(country, excludeCurrencyCode, this.currentConfig.getConfig()))
      .value(value)
      .build();
  }

  private float getRoundedValue(final double initialValue, final @NonNull RoundingMode roundingMode) {
    return roundingMode == RoundingMode.DOWN ? (float) Math.floor(initialValue) : (float) Math.round(initialValue);
  }

  private String getSymbolForCurrency(final @NonNull Country country, final boolean excludeCurrencyCode, final @NonNull Config config) {
    final boolean countryIsUS = country == Country.US;
    final boolean userInUS = config.countryCode().equals(Country.US.getCountryCode());

    if (!config.currencyNeedsCode(country.getCurrencySymbol())) {
      return country.getCurrencySymbol();
    }

    if (userInUS && excludeCurrencyCode && countryIsUS) {
      // US people looking at US projects just get the currency symbol
      return Country.US.getCurrencySymbol();
    } else if (country == Country.SG) {
      // Singapore projects get a special currency prefix
      return "\u00A0" + "S" + country.getCurrencySymbol() + "\u00A0";
    } else if (country.getCurrencySymbol().equals("kr") || country.getCurrencySymbol().equals("Fr")) {
      // Kroner projects use the currency code prefix
      return "\u00A0" + country.getCurrencyCode() + "\u00A0";
    } else {
      return "\u00A0" + country.getCountryCode() + country.getCurrencySymbol() + "\u00A0";
    }
  }

  private String trim(final @NonNull String formattedString) {
    return formattedString.replace('\u00A0', ' ').trim();
  }

  @AutoParcel
  public abstract static class CurrencyOptions implements Parcelable {
    public abstract String country();
    public abstract String currencyCode();
    public abstract String currencySymbol();
    public abstract float value();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder country(String __);
      public abstract Builder currencyCode(String __);
      public abstract Builder currencySymbol(String __);
      public abstract Builder value(float __);
      public abstract CurrencyOptions build();
    }

    public static Builder builder() {
      return new AutoParcel_KSCurrency_CurrencyOptions.Builder();
    }

    public abstract Builder toBuilder();
  }
}
