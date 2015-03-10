package it.gbresciani.poligame.fragments;


import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.WordConfirmedEvent;
import it.gbresciani.poligame.events.WordDismissedEvent;
import it.gbresciani.poligame.events.WordSelectedEvent;
import it.gbresciani.poligame.helper.BusProvider;


public class WordConfirmDialogFragment extends DialogFragment {
    private static final String ARG_PARAM1 = "word";

    private String word;
    @InjectView(R.id.word_textview) TextView wordTextView;
    @InjectView(R.id.wrong_imageview) ImageView wrongImageView;
    @InjectView(R.id.correct_imageview) ImageView correctImageView;
    @InjectView(R.id.ok_button) Button okButton;
    @InjectView(R.id.no_button) Button noButton;

    private Bus BUS;
    private Dialog dialog;

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

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        return dialog;
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
    public void ok() {
        BUS.post(new WordConfirmedEvent(word));
        //Prevent other click
        okButton.setClickable(false);
        noButton.setClickable(false);
    }

    @OnClick(R.id.no_button)
    public void no() {
        BUS.post(new WordDismissedEvent());
        dialog.dismiss();
    }

    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent){

        //TODO define animation in xml
        final WordSelectedEvent event = wordSelectedEvent;
        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new AccelerateInterpolator());
        animSet.setFillAfter(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0f, 1f, 0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(450);

        animSet.addAnimation(scaleAnimation);

        final AnimationSet animSet2 = new AnimationSet(true);
        animSet.setInterpolator(new AccelerateInterpolator());
        animSet.setFillAfter(true);

        ScaleAnimation scaleAnimation2 = new ScaleAnimation(0f, 1f, 0f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation2.setDuration(450);

        animSet2.addAnimation(scaleAnimation2);
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {

            }

            @Override public void onAnimationEnd(Animation animation) {
                if(event.isCorrect()){
                    correctImageView.setVisibility(View.VISIBLE);
                    correctImageView.startAnimation(animSet2);
                }else{
                    wrongImageView.setVisibility(View.VISIBLE);
                    wrongImageView.startAnimation(animSet2);
                }
                (new Handler()).postDelayed(new Runnable() {
                    @Override public void run() {
                        dialog.dismiss();
                    }
                }, 1000);
            }

            @Override public void onAnimationRepeat(Animation animation) {

            }
        });

        wordTextView.startAnimation(animSet);

    }


}
