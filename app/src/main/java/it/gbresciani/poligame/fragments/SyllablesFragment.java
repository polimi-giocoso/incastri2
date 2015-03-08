package it.gbresciani.poligame.fragments;


import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.activities.PlayActivity;
import it.gbresciani.poligame.events.SyllableSelectedEvent;
import it.gbresciani.poligame.events.WordDismissedEvent;
import it.gbresciani.poligame.events.WordSelectedEvent;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.model.Syllable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyllablesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyllablesFragment extends Fragment {
    private static final String SYLLABLES = "syllables";

    private ArrayList<Syllable> syllables;
    private PlayActivity mActivity;
    private Bus BUS;
    private Random rnd = new Random();
    private ArrayList<CardView> syllableCards = new ArrayList<>();
    private ArrayList<Integer> syllableCardRotationDegree = new ArrayList<>();
    private ArrayList<Boolean> syllableCardSelection = new ArrayList<>();


    @InjectView(R.id.syllables_container) LinearLayout syllablesContainerLinearLayout;
    @InjectView(R.id.syllables_layout_1) LinearLayout syllablesLinearLayout1;
    @InjectView(R.id.syllables_layout_2) LinearLayout syllablesLinearLayout2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param syllables Syllables list to show
     * @return A new instance of fragment SyllablesFragment.
     */
    public static SyllablesFragment newInstance(ArrayList<Syllable> syllables) {
        SyllablesFragment fragment = new SyllablesFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SYLLABLES, syllables);
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
            syllables = getArguments().getParcelableArrayList(SYLLABLES);
        }
        mActivity = (PlayActivity) getActivity();
        BUS = BusProvider.getInstance();
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

    /*  Events  */


    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent) {
        for (CardView card : syllableCards) {
            select(card, false);

        }
    }

    @Subscribe public void wordDismissed(WordDismissedEvent wordDismissedEvent) {
        for (CardView card : syllableCards) {
            select(card, false);
        }
    }


    /*  UI Methods  */

    private void initUI() {

        final int syllablesCount = syllables.size();

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
                int slotMargin = (int) ((getResources().getDimension(R.dimen.slot_margin)) * 1.5);

                // Choose, as dimension for one slot, the minimum between the width of the layout and the height divided
                // by the number of slots to be drawn minus two margins
                int slotDimen = (Math.min(width / 2, (height / (syllablesCount / 2))) - 2 * slotMargin);

                // Add 2 LinearLayout
                for (int i = 0; i < syllablesCount; i++) {

                    // Create a Relative as a container for the slot to center it
                    LinearLayout ll = new LinearLayout(mActivity);
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    lParams.gravity = Gravity.CENTER;
                    lParams.weight = 1;
                    ll.setLayoutParams(lParams);

                    // Add the RelativeLayout container to the main layout
                    if (i % 2 == 0) {
                        syllablesLinearLayout1.addView(ll);
                    } else {
                        syllablesLinearLayout2.addView(ll);
                    }

                    CardView card = createSyllableCard(syllables.get(i), slotDimen, slotMargin);
                    ll.addView(card);

                }
            }
        });
    }

    private CardView createSyllableCard(Syllable syllable, int cardDimen, int cardMargin) {

        CardView syllableCardView = new CardView(mActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardDimen, cardDimen);
        params.setMargins(cardMargin, cardMargin, cardMargin, cardMargin);
        params.gravity = Gravity.CENTER;
        syllableCardView.setLayoutParams(params);
        syllableCardView.setCardBackgroundColor(Color.parseColor(syllable.getColor()));

        TextView textView = new TextView(mActivity);
        textView.setText(syllable.getVal());
        textView.setTextSize(100);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        textView.setGravity(Gravity.CENTER);
        textView.setAllCaps(true);

        syllableCardView.addView(textView);

        // Choose a random rotation angle between -25 and 25
        final int degree = rnd.nextInt(50) - 25;

        // Create entering animations
        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setStartOffset(500);
        scaleAnimation.setDuration(750);

        RotateAnimation rotateAnimation = new RotateAnimation(0, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setStartOffset(500);
        rotateAnimation.setDuration(750);

        animSet.addAnimation(scaleAnimation);
        animSet.addAnimation(rotateAnimation);

        syllableCardView.startAnimation(animSet);

        syllableCardView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // Get the only child view that is a TextView
                String syllable = (String) ((TextView) ((CardView) v).getChildAt(0)).getText();
                select(v, true);
                BUS.post(new SyllableSelectedEvent(syllable));
            }
        });

        syllableCards.add(syllableCardView);
        syllableCardRotationDegree.add(degree);
        syllableCardSelection.add(false);

        return syllableCardView;
    }

    private void select(View v, boolean selected) {
        int cardIndex = syllableCards.indexOf(v);
        float viewDegree = syllableCardRotationDegree.get(cardIndex);
        boolean viewSelection = syllableCardSelection.get(cardIndex);
        float toDegree;
        float fromDegree;
        float toScale;
        float fromScale;
        if (selected && !viewSelection) {
            fromDegree = viewDegree;
            toDegree = 0;
            fromScale = 1f;
            toScale = 1.5f;

        } else if (!selected && viewSelection) {
            fromDegree = 0;
            toDegree = viewDegree;
            fromScale = 1.5f;
            toScale = 1f;

        } else {
            return;
        }

        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);
        animSet.setDuration(250);

        ScaleAnimation scaleAnimation = new ScaleAnimation(fromScale, toScale, fromScale, toScale,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);

        scaleAnimation.setStartOffset(250);
        scaleAnimation.setDuration(250);

        RotateAnimation rotateAnimation = new RotateAnimation(fromDegree, toDegree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setStartOffset(0);
        scaleAnimation.setDuration(250);

        animSet.addAnimation(scaleAnimation);
        animSet.addAnimation(rotateAnimation);

        v.startAnimation(animSet);
        syllableCardSelection.set(cardIndex, selected);
    }
}
