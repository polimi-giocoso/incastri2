package it.gbresciani.poligame.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.WordConfirmedEvent;
import it.gbresciani.poligame.events.WordDismissedEvent;
import it.gbresciani.poligame.helper.BusProvider;


public class WordConfirmDialogFragment extends DialogFragment {
    private static final String ARG_PARAM1 = "word";

    private String word;
    @InjectView(R.id.word_textview) TextView wordTextView;

    private Bus BUS;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param word Parameter 1.
     * @return A new instance of fragment WordConfirmDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WordConfirmDialogFragment newInstance(String word) {
        WordConfirmDialogFragment fragment = new WordConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, word);
        fragment.setArguments(args);
        return fragment;
    }

    public WordConfirmDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BUS = BusProvider.getInstance();
        if (getArguments() != null) {
            word = getArguments().getString(ARG_PARAM1);
        }
        int style = DialogFragment.STYLE_NO_TITLE, theme = 0;
        setStyle(style, theme);
        this.setCancelable(false);
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
        View v = inflater.inflate(R.layout.fragment_word_confirm_dialog, container, false);
        ButterKnife.inject(this, v);

        wordTextView.setText(word);

        return v;
    }

    @OnClick(R.id.ok_button)
    public void ok(){
        BUS.post(new WordConfirmedEvent(word));
        this.dismiss();
    }

    @OnClick(R.id.no_button)
    public void no(){
        BUS.post(new WordDismissedEvent());
        this.dismiss();
    }


}
