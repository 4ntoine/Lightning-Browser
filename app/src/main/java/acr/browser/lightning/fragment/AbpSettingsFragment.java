package acr.browser.lightning.fragment;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;

import acr.browser.lightning.R;
import acr.browser.lightning.app.BrowserApp;

public class AbpSettingsFragment
  extends LightningPreferenceFragment
  implements Preference.OnPreferenceChangeListener {

  private static final String SETTINGS_ENABLED = "abp_enabled";
  private static final String SETTINGS_ACCEPTABLE_ADS_ENABLED = "aa_enabled";

  private CheckBoxPreference cbenabled;
  private CheckBoxPreference cbacceptableads;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    BrowserApp.getAppComponent().inject(this);
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preference_abp);

    initPrefs();
  }

  private void initPrefs() {
    cbenabled = (CheckBoxPreference) findPreference(SETTINGS_ENABLED);
    cbacceptableads = (CheckBoxPreference) findPreference(SETTINGS_ACCEPTABLE_ADS_ENABLED);

    cbenabled.setOnPreferenceChangeListener(this);
    cbacceptableads.setOnPreferenceChangeListener(this);

    boolean abpEnabled = mPreferenceManager.getAbpEnabled();
    cbenabled.setChecked(abpEnabled);
    applyAbpEnabled(abpEnabled);

    cbacceptableads.setChecked(mPreferenceManager.getAcceptableAdsEnabled());
  }

  @Override
  public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
    switch (preference.getKey()) {
      case SETTINGS_ENABLED:
        boolean abpEnabled = (Boolean) newValue;
        mPreferenceManager.setAbpEnabled(abpEnabled);
        applyAbpEnabled(abpEnabled);
        return true;
      case SETTINGS_ACCEPTABLE_ADS_ENABLED:
        mPreferenceManager.setAcceptableAdsEnabled((Boolean) newValue);
        return true;
      default:
        return false;
    }
  }

  private void applyAbpEnabled(boolean enable) {
    // "Acceptable Ads" checkbox should be available if "Enable ABP" is checked only
    cbacceptableads.setEnabled(enable);
  }
}
