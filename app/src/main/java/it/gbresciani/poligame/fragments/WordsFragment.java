package it.gbresciani.poligame.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ActionMenuView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.poligame.R;

/**
 * The fragment that shows the list of words
 */
public class WordsFragment extends Fragment {
    private static final String NO_SYLLABLES = "no_syllables";

    private Integer noSyllables;
    private Activity mActivity;

    @InjectView(R.id.words_container) LinearLayout wordsContainerLinearLayout;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param noSyllables Parameter 1.
     * @return A new instance of fragment WordsFragment.
     */
    public static WordsFragment newInstance(Integer noSyllables) {
        WordsFragment fragment = new WordsFragment();
        Bundle args = new Bundle();
        args.putInt(NO_SYLLABLES, noSyllables);
        fragment.setArguments(args);
        return fragment;
    }

    public WordsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noSyllables = getArguments().getInt(NO_SYLLABLES);
        }
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_words, container, false);
        ButterKnife.inject(this, rootView);
        initUI();
        return rootView;
    }

    private void initUI() {

        // Waiting for the layout to be drawn in order to get the correct height and width and draw the word slots
        ViewTreeObserver vto = wordsContainerLinearLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                // Remove the observer
                wordsContainerLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get the layout dimensions
                int width = wordsContainerLinearLayout.getMeasuredWidth();
                int height = wordsContainerLinearLayout.getMeasuredHeight();

                // To maintain proportions calculates the margin according to the number of slot to be displayed
                int slotMargin = ((int) getResources().getDimension(R.dimen.slot_margin)) / noSyllables;

                // Choose, as dimension for one slot, the minimum between the width of the layout and the height divided
                // by the number of slots to be drawn minus two margins
                int slotDimen = Math.min(width, (height / noSyllables)) - 2 * slotMargin;

                // Draw as much slots as needed
                for (int i = 0; i < noSyllables; i++) {
                    // Create a FrameLayout as a container for the slot to center it
                    RelativeLayout rl = new RelativeLayout(mActivity);
                    LinearLayout.LayoutParams rParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                                    ViewGroup.LayoutParams.MATCH_PARENT);
                    rParams.weight = 1;
                    rl.setLayoutParams(rParams);

                    // Create the slot View and add it to the FrameLayout container
                    View slot = new View(mActivity);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(slotDimen, slotDimen);
                    params.setMargins(slotMargin, slotMargin, slotMargin, slotMargin);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    slot.setBackgroundResource(R.drawable.shape_empty_slot);
                    slot.setLayoutParams(params);
                    rl.addView(slot);

                    // Add the FrameLayout container to the main layout
                    wordsContainerLinearLayout.addView(rl);
                }
            }
        });
    }
}
