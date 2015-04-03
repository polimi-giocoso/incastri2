package it.gbresciani.legodigitalsonoro.fragments;


import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.activities.PlayActivity;

public class WaitDialogFragment extends DialogFragment {

    private PlayActivity mActivity;


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
        mActivity = (PlayActivity) getActivity();
        this.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wait_dialog, container, false);
        ButterKnife.inject(this, v);

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() != KeyEvent.ACTION_DOWN) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(mActivity);
                    adb.setTitle(mActivity.getString(R.string.exit_dialog_title))
                            .setMessage(mActivity.getString(R.string.exit_dialog_message))
                            .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    mActivity.finish();
                                }
                            })
                            .setNegativeButton(getString(android.R.string.no), null)
                            .create()
                            .show();
                    return true;
                }

                return false;
            }
        });

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
