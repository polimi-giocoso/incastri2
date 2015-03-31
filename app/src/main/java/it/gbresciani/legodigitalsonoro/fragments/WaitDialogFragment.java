package it.gbresciani.legodigitalsonoro.fragments;


import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import it.gbresciani.legodigitalsonoro.R;

public class WaitDialogFragment extends DialogFragment {

    public static WaitDialogFragment newInstance() {
        WaitDialogFragment fragment = new WaitDialogFragment();
        return fragment;
    }

    public WaitDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int style = DialogFragment.STYLE_NO_TITLE, theme = 0;
        setStyle(style, theme);
        this.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wait_dialog, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }
//
//        int dialogWidth = 1200;
//        int dialogHeight = 1200;
//
//        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }
}
