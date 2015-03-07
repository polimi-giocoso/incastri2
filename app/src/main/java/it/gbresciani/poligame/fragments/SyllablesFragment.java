package it.gbresciani.poligame.fragments;


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
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.activities.PlayActivity;
import it.gbresciani.poligame.events.PageCompletedEvent;
import it.gbresciani.poligame.events.SyllableSelectedEvent;
import it.gbresciani.poligame.events.WordSelectedEvent;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.model.Syllable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyllablesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyllablesFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SYLLABLES = "syllables";

    private ArrayList<Syllable> syllables;
    private PlayActivity mActivity;
    private Bus BUS;
    private Random rnd = new Random();
    private ArrayList<TextView> syllableCards = new ArrayList<>();


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

    private int rndColor() {
        Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        return Color.rgb(r, g, b);
    }

    /*  Events  */


    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent) {
        for (TextView card : syllableCards) {
            card.setTextColor(getResources().getColor(android.R.color.black));
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
                int slotMargin = ((int) getResources().getDimension(R.dimen.slot_margin)) * 3;

                // Choose, as dimension for one slot, the minimum between the width of the layout and the height divided
                // by the number of slots to be drawn minus two margins
                int slotDimen = Math.min(width / 2, (height / (syllablesCount / 2))) - 2 * slotMargin;

                // Add 2 LinearLayout
                for (int i = 0; i < syllablesCount; i++) {

                    // Create a Relative as a container for the slot to center it
                    LinearLayout ll = new LinearLayout(mActivity);
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    lParams.weight = 1;
                    ll.setLayoutParams(lParams);

                    // Add the RelativeLayout container to the main layout
                    if (i % 2 == 0) {
                        syllablesLinearLayout1.addView(ll);
                    } else {
                        syllablesLinearLayout2.addView(ll);
                    }

                    TextView card = createSyllableCard(syllables.get(i).getVal(), slotDimen, slotMargin);
                    syllableCards.add(card);
                    ll.addView(card);

                }
            }
        });
    }

    private TextView createSyllableCard(String text, int cardDimen, int cardMargin) {

        TextView textCard = new TextView(mActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardDimen, cardDimen);
        params.setMargins(cardMargin, cardMargin, cardMargin, cardMargin);
        params.gravity = Gravity.CENTER;
        textCard.setBackgroundColor(rndColor());
        textCard.setText(text);
        textCard.setGravity(Gravity.CENTER);
        textCard.setTextSize(100);

        textCard.setLayoutParams(params);

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

        textCard.startAnimation(animSet);

        textCard.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String syllable = (String) ((TextView) v).getText();
                ((TextView) v).setTextColor(getResources().getColor(android.R.color.white));
                BUS.post(new SyllableSelectedEvent(syllable));
            }
        });

        return textCard;
    }
}
