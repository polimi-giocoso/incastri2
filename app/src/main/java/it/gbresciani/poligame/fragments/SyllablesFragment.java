package it.gbresciani.poligame.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.poligame.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyllablesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyllablesFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SYLLABLES_COUNT = "syllables_count";

    private Integer noSyllables;
    private Activity mActivity;

    @InjectView(R.id.syllables_container) LinearLayout syllablesContainerLinearLayout;
    @InjectView(R.id.syllables_layout_1) LinearLayout syllablesLinearLayout1;
    @InjectView(R.id.syllables_layout_2) LinearLayout syllablesLinearLayout2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param noSyllables Parameter 1.
     * @return A new instance of fragment SyllablesFragment.
     */
    public static SyllablesFragment newInstance(Integer noSyllables) {
        SyllablesFragment fragment = new SyllablesFragment();
        Bundle args = new Bundle();
        args.putInt(SYLLABLES_COUNT, noSyllables);
        fragment.setArguments(args);
        return fragment;
    }

    public SyllablesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noSyllables = getArguments().getInt(SYLLABLES_COUNT);
        }
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_syllables, container, false);
        ButterKnife.inject(this, rootView);
        initUI();
        return rootView;
    }

    private int rndColor() {
        Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        return Color.rgb(r, g, b);
    }

    private void initUI() {

        // Waiting for the layout to be drawn in order to get the correct height and width and draw the word slots
        ViewTreeObserver vto = syllablesContainerLinearLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                // Remove the observer
                syllablesContainerLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get the layout dimensions
                int width = syllablesContainerLinearLayout.getMeasuredWidth();
                int height = syllablesContainerLinearLayout.getMeasuredHeight();

                // To maintain proportions calculates the margin according to the number of slot to be displayed
                int slotMargin = ((int) getResources().getDimension(R.dimen.slot_margin)) * 3;

                // Choose, as dimension for one slot, the minimum between the width of the layout and the height divided
                // by the number of slots to be drawn minus two margins
                int slotDimen = Math.min(width / 2, (height / (noSyllables / 2))) - 2 * slotMargin;

                List<LinearLayout> syllablesLayouts = new ArrayList<>();
                syllablesLayouts.add(syllablesLinearLayout1);
                syllablesLayouts.add(syllablesLinearLayout2);

                // Add 2 LinearLayout
                for (LinearLayout ll : syllablesLayouts) {

                    // Add 1 or 2 slot according to the preferences
                    for (int j = 0; j < noSyllables / 2; j++) {
                        // Create a Relative as a container for the slot to center it
                        LinearLayout ll2 = new LinearLayout(mActivity);
                        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        lParams.weight = 1;
                        ll2.setLayoutParams(lParams);
                        // Add the RelativeLayout container to the main layout
                        ll.addView(ll2);

                        final View slot = new View(mActivity);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(slotDimen, slotDimen);
                        params.setMargins(slotMargin, slotMargin, slotMargin, slotMargin);
                        params.gravity = Gravity.CENTER;
                        slot.setBackgroundColor(rndColor());

                        // Set random rotation angle between -25 and 25
                        Random rnd = new Random();
                        final int degree = rnd.nextInt(50) - 25;
                        slot.setLayoutParams(params);

                        AnimationSet animSet = new AnimationSet(true);
                        animSet.setInterpolator(new DecelerateInterpolator());
                        animSet.setFillAfter(true);

                        // Create entering animation
                        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f,
                                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setStartOffset(250);
                        scaleAnimation.setDuration(750);

                        RotateAnimation rotateAnimation = new RotateAnimation(0, degree,
                                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                        rotateAnimation.setDuration(750);
                        rotateAnimation.setStartOffset(250);

                        animSet.addAnimation(scaleAnimation);
                        animSet.addAnimation(rotateAnimation);

                        slot.startAnimation(animSet);

                        ll2.addView(slot);
                    }
                }
            }
        });
    }
}
