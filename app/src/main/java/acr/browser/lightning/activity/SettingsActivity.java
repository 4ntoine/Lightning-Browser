/*
 * Copyright 2014 A.C.R. Development
 */
package acr.browser.lightning.activity;

import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.anthonycr.grant.PermissionsManager;

import org.adblockplus.libadblockplus.android.AdblockEngine;
import org.adblockplus.libadblockplus.android.Utils;
import org.adblockplus.libadblockplus.android.settings.Adblock;
import org.adblockplus.libadblockplus.android.settings.AdblockGeneralSettingsFragment;
import org.adblockplus.libadblockplus.android.settings.AdblockSettings;
import org.adblockplus.libadblockplus.android.settings.AdblockSettingsFragment;
import org.adblockplus.libadblockplus.android.settings.AdblockSettingsStorage;
import org.adblockplus.libadblockplus.android.settings.AdblockWhitelistedDomainsSettingsFragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import acr.browser.lightning.R;
import acr.browser.lightning.app.BrowserApp;

public class SettingsActivity extends ThemableSettingsActivity
    implements
        AdblockGeneralSettingsFragment.Provider,
        AdblockGeneralSettingsFragment.Listener,
        AdblockWhitelistedDomainsSettingsFragment.Listener,
        FragmentManager.OnBackStackChangedListener {

    private static final String TAG = Utils.getTag(SettingsActivity.class);

    private static final List<String> mFragments = new ArrayList<>(7);

    private FrameLayout whitelistingContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this is a workaround for the Toolbar in PreferenceActitivty
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        View content = root.getChildAt(0);
        ViewGroup toolbarContainer = (LinearLayout) View.inflate(this, R.layout.toolbar_settings, null);

        // becuase of workaround above we can't just replace android.R.id.content with wl fragment
        whitelistingContainer = (FrameLayout) toolbarContainer.findViewById(R.id.settings_wl_container);
        whitelistingContainer.setVisibility(View.GONE);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        // now we can set the Toolbar using AppCompatPreferenceActivity
        Toolbar toolbar = (Toolbar) toolbarContainer.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ad blocking
        Adblock.get().retain(false);

        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onDestroy()
    {
        getFragmentManager().removeOnBackStackChangedListener(this);

        // ad blocking
        Adblock.get().release();

        super.onDestroy();
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
        mFragments.clear();
        Iterator<Header> headerIterator = target.iterator();
        while (headerIterator.hasNext()) {
            Header header = headerIterator.next();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // Workaround for bug in the AppCompat support library
                header.iconRes = R.drawable.empty;
            }

            if (header.titleRes == R.string.debug_title) {
                if (BrowserApp.isRelease()) {
                    headerIterator.remove();
                } else {
                    mFragments.add(header.fragment);
                }
            } else {
                mFragments.add(header.fragment);
            }
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return mFragments.contains(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // ad blocking

    @Override
    public AdblockEngine getAdblockEngine() {
        return Adblock.get().getEngine();
    }

    @Override
    public AdblockSettingsStorage getAdblockSettingsStorage() {
        return Adblock.get().getStorage();
    }

    @Override
    public boolean isValidDomain(AdblockWhitelistedDomainsSettingsFragment fragment,
                                 String domain, AdblockSettings settings) {
        return domain != null && domain.length() > 0;
    }

    @Override
    public void onAdblockSettingsChanged(AdblockSettingsFragment fragment) {
        Log.d(TAG, "Adblock setting changed: " + fragment.getSettings());
    }

    @Override
    public void onWhitelistedDomainsClicked(AdblockGeneralSettingsFragment fragment) {
        getFragmentManager()
            .beginTransaction()
            .replace(
                R.id.settings_wl_container,
                AdblockWhitelistedDomainsSettingsFragment.newInstance())
            .addToBackStack(AdblockWhitelistedDomainsSettingsFragment.class.getSimpleName())
            .commit();
    }

    @Override
    public void onBackStackChanged() {
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            // whitelisting fragment added
            whitelistingContainer.setVisibility(View.VISIBLE);
        } else {
            // returned back from whitelisting fragment
            whitelistingContainer.setVisibility(View.GONE);
        }
    }
}
