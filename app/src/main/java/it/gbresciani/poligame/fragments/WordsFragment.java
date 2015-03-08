package it.gbresciani.poligame.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.activities.PlayActivity;
import it.gbresciani.poligame.events.WordSelectedEvent;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.model.Word;

/**
 * The fragment that shows the list of words
 */
public class WordsFragment extends Fragment {
    private static final String NO_SYLLABLES = "no_syllables";

    private List<Word> words;
    private PlayActivity mActivity;
    private Bus BUS;
    private ArrayList<CardView> availableSlots = new ArrayList<>();

    @InjectView(R.id.words_container) LinearLayout wordsContainerLinearLayout;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param words The list of words available in this pagew.
     * @return A new instance of fragment WordsFragment.
     */
    public static WordsFragment newInstance(ArrayList<Word> words) {
        WordsFragment fragment = new WordsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(NO_SYLLABLES, words);
        args.putSerializable(NO_SYLLABLES, words);
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
            words = getArguments().getParcelableArrayList(NO_SYLLABLES);
        }
        mActivity = (PlayActivity) getActivity();
        BUS = BusProvider.getInstance();
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

    private void initUI() {

        final int wordsToFind = words.size() <= 4 ? words.size() : 4;

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
                int slotMargin = ((int) getResources().getDimension(R.dimen.slot_margin)) / wordsToFind;

                // Choose, as dimension for one slot, the minimum between the width of the layout and the height divided
                // by the number of slots to be drawn minus two margins
                int slotDimen = Math.min(width, (height / wordsToFind)) - 2 * slotMargin;

                // Draw as much slots as needed
                for (int i = 0; i < wordsToFind; i++) {
                    // Create a Relative as a container for the slot to center it
                    RelativeLayout rl = new RelativeLayout(mActivity);
                    LinearLayout.LayoutParams rParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    rParams.weight = 1;
                    rl.setLayoutParams(rParams);

                    // Create the slot View and add it to the RelativeLayout container
                    CardView card = new CardView(mActivity);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(slotDimen, slotDimen);
                    params.setMargins(slotMargin, slotMargin, slotMargin, slotMargin);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    card.setLayoutParams(params);
                    TextView slot = new TextView(mActivity);
                    slot.setGravity(Gravity.CENTER);
                    slot.setTextSize(50);
                    slot.setAllCaps(true);

                    // Add the slot to its RelativeLayout
                    card.addView(slot);
                    card.setCardElevation(0);
                    rl.addView(card);
                    availableSlots.add(card);

                    // Add the RelativeLayout container to the main layout
                    wordsContainerLinearLayout.addView(rl);
                }
            }
        });
    }

    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent) {
        if (wordSelectedEvent.isCorrect() && wordSelectedEvent.isNew()) {
            ((TextView) availableSlots.get(0).getChildAt(0)).setText(wordSelectedEvent.getWord().getLemma());
            availableSlots.get(0).setCardElevation(10);
            availableSlots.remove(0);
        }
    }
}
