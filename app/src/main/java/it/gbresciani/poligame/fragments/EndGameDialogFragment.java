package it.gbresciani.poligame.fragments;


import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.OnClick;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.ExitEvent;
import it.gbresciani.poligame.events.RepeatEvent;
import it.gbresciani.poligame.helper.BusProvider;


public class EndGameDialogFragment extends DialogFragment {


    private Bus BUS;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WordConfirmDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EndGameDialogFragment newInstance() {
        EndGameDialogFragment fragment = new EndGameDialogFragment();
        return fragment;
    }

    public EndGameDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BUS = BusProvider.getInstance();
        int style = DialogFragment.STYLE_NO_TITLE, theme = 0;
        setStyle(style, theme);
    }

    @Override
    public void onResume() {
        super.onResume();
        BUS.register(this);
    }

    @Override
    public void onPause() {
        BUS.unregister(this);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_end_game_dialog, container, false);
        ButterKnife.inject(this, v);

        return v;
    }

    @Override public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }

        int dialogWidth = 1200;
        int dialogHeight = 1200;

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }

    @OnClick(R.id.repeat_button_dialog)
    public void repeat() {
        BUS.post(new RepeatEvent());
        dismiss();
    }

    @OnClick(R.id.exit_button_dialog)
    public void exit() {
        BUS.post(new ExitEvent());
        dismiss();
    }
}
