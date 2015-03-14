package it.gbresciani.poligame.fragments;


import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
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
    @InjectView(R.id.ok_button) ImageButton okButton;
    @InjectView(R.id.no_button) ImageButton noButton;

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
    public void ok() {
        BUS.post(new WordConfirmedEvent(word));
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

        final ObjectAnimator animator;
        if (wordSelectedEvent.isCorrect()) {
            if (wordSelectedEvent.isNew()) {
                animator = ObjectAnimator.ofInt(wordTextView, property, getResources().getColor(android.R.color.holo_green_light));
            } else {
                // Skip color animation
                dismiss();
                return;
            }
        } else {
            animator = ObjectAnimator.ofInt(wordTextView, property, getResources().getColor(android.R.color.holo_red_light));
        }
        animator.setDuration(200);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new AccelerateInterpolator(1));

        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {

            }

            @Override public void onAnimationEnd(Animator animation) {
                (new Handler()).postDelayed(new Runnable() {
                    @Override public void run() {
                        dismiss();
                    }
                }, 1000);
            }

            @Override public void onAnimationCancel(Animator animation) {

            }

            @Override public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
    }
}
