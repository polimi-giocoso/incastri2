package it.gbresciani.legodigitalsonoro.fragments;


import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.events.ExitEvent;
import it.gbresciani.legodigitalsonoro.events.RepeatEvent;
import it.gbresciani.legodigitalsonoro.helper.BusProvider;


public class EndGameDialogFragment extends DialogFragment {

    private static final String PASSIVE = "passive";

    private boolean passive = false;

    private Bus BUS;

    @InjectView(R.id.repeat_button_dialog) ImageButton repeatButton;
    @InjectView(R.id.exit_button_dialog) ImageButton exitButton;

    public static EndGameDialogFragment newInstance(boolean passive) {
        EndGameDialogFragment fragment = new EndGameDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(PASSIVE, passive);
        fragment.setArguments(args);
        return fragment;
    }

    public EndGameDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BUS = BusProvider.getInstance();
        if (getArguments() != null) {
            passive = getArguments().getBoolean(PASSIVE);
        }
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
        if (passive) {
            exitButton.setVisibility(View.GONE);
            repeatButton.setVisibility(View.GONE);
        }

        return v;
    }

    @Override public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }


        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        int dialogDimen = (int) (height * 0.8);
        int buttonDimen = (int) (dialogDimen * 0.33);

        // Set the objects dimensions based on the device screen size
        repeatButton.getLayoutParams().height = buttonDimen;
        repeatButton.getLayoutParams().width = buttonDimen;

        exitButton.getLayoutParams().height = buttonDimen;
        exitButton.getLayoutParams().width = buttonDimen;

        getDialog().getWindow().setLayout(dialogDimen, dialogDimen);
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
