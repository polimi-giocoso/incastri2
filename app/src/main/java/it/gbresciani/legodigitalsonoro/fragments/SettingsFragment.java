package it.gbresciani.legodigitalsonoro.fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.events.NoEmailEvent;
import it.gbresciani.legodigitalsonoro.helper.BusProvider;

/**
 * The settings fragment
 */
public class SettingsFragment extends PreferenceFragment {

    private Bus BUS;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);

        BUS = BusProvider.getInstance();
    }


    @Override public void onResume() {
        super.onResume();
        BUS.register(this);
    }

    @Override public void onPause() {
        BUS.unregister(this);
        super.onPause();
    }

    @Subscribe public void noEmail(NoEmailEvent noEmailEvent) {
        Snackbar.with(getActivity()).
                text(R.string.no_email_snackbar)
                .type(SnackbarType.SINGLE_LINE)
                .duration(1000l)
                .show(getActivity());
    }
}
