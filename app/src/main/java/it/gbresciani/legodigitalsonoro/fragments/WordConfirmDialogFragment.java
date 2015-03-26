package it.gbresciani.legodigitalsonoro.fragments;


import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.activities.PlayActivity;
import it.gbresciani.legodigitalsonoro.events.WordDismissedEvent;
import it.gbresciani.legodigitalsonoro.events.WordSelectedEvent;
import it.gbresciani.legodigitalsonoro.helper.BusProvider;
import it.gbresciani.legodigitalsonoro.helper.Helper;
import it.gbresciani.legodigitalsonoro.model.Word;


public class WordConfirmDialogFragment extends DialogFragment {
    private static final String ARG_PARAM1 = "wordLemma";
    public static final int WORD_DIALOG_TIMEOUT = 1000;

    private String wordLemma;
    @InjectView(R.id.word_textview) TextView wordTextView;
    @InjectView(R.id.confirm_dialog_layout) RelativeLayout confirmDialogLayout;
    @InjectView(R.id.ok_button) ImageButton okButton;
    @InjectView(R.id.no_button) ImageButton noButton;

    private PlayActivity playActivity;

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
        playActivity = (PlayActivity) getActivity();
        if (getArguments() != null) {
            wordLemma = getArguments().getString(ARG_PARAM1);
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

        wordTextView.setText(wordLemma);

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

    @OnClick(R.id.ok_button)
    public void ok() {

        Word word = Helper.wordByLemma(wordLemma);
        if (word != null) {
            Log.d("wordSelected", wordLemma + " exists!");
            BUS.post(new WordSelectedEvent(word, true, playActivity.getGameState().getWordsAvailable().contains(word)));
        } else {
            Log.d("wordSelected", wordLemma + " does not exists!");
            BUS.post(new WordSelectedEvent(word, false, false));
        }

        //Prevent other click
        okButton.setClickable(false);
        noButton.setClickable(false);
    }

    @OnClick(R.id.no_button)
    public void no() {
        BUS.post(new WordDismissedEvent());
        dismiss();
    }

    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent) {

        final Property<TextView, Integer> property = new Property<TextView, Integer>(int.class, "textColor") {
            @Override
            public Integer get(TextView object) {
                return object.getCurrentTextColor();
            }

            @Override
            public void set(TextView object, Integer value) {
                object.setTextColor(value);
            }
        };


        final WordSelectedEvent wordEvent = wordSelectedEvent;

        final ObjectAnimator animator = ObjectAnimator.ofInt(wordTextView, property, getResources().getColor(android.R.color.white));
        animator.setDuration(100);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new AccelerateInterpolator(1));


        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0f, 1f, 0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);

        scaleAnimation.setInterpolator(new DecelerateInterpolator());
        scaleAnimation.setFillEnabled(true);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(100);


        Drawable noBackgrounds[] = new Drawable[2];
        Drawable okBackgrounds[] = new Drawable[2];
        Resources res = getResources();
        noBackgrounds[0] = res.getDrawable(R.drawable.dialog_bg);
        okBackgrounds[0] = res.getDrawable(R.drawable.dialog_bg);
        noBackgrounds[1] = res.getDrawable(R.drawable.dialog_no_bg);
        okBackgrounds[1] = res.getDrawable(R.drawable.dialog_ok_bg);

        final TransitionDrawable noCrossfader = new TransitionDrawable(noBackgrounds);
        final TransitionDrawable okCrossfader = new TransitionDrawable(okBackgrounds);

        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {

            }

            @Override public void onAnimationEnd(Animation animation) {

                (new Handler()).postDelayed(new Runnable() {
                    @Override public void run() {
                        dismiss();
                    }
                }, WORD_DIALOG_TIMEOUT + 100);
            }

            @Override public void onAnimationRepeat(Animation animation) {

            }
        });

        if (wordEvent.isCorrect()) {
            if (wordEvent.isNew()) {
                confirmDialogLayout.setBackground(okCrossfader);
                animator.start();
            } else {
                // Skip color animation
                (new Handler()).postDelayed(new Runnable() {
                    @Override public void run() {
                        dismiss();
                    }
                }, WORD_DIALOG_TIMEOUT);
                return;
            }
        } else {
            confirmDialogLayout.setBackground(noCrossfader);
            animator.start();
        }

        okCrossfader.startTransition(100);
        noCrossfader.startTransition(100);
        okButton.startAnimation(scaleAnimation);
        noButton.startAnimation(scaleAnimation);
    }
}
