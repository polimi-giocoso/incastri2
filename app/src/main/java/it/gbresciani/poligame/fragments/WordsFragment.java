package it.gbresciani.poligame.fragments;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.activities.PlayActivity;
import it.gbresciani.poligame.events.WordClickedEvent;
import it.gbresciani.poligame.events.WordSelectedEvent;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.helper.Helper;
import it.gbresciani.poligame.model.Word;

/**
 * The fragment that shows the list of words
 */
public class WordsFragment extends Fragment {
    private static final String WORDS = "words";

    private PlayActivity mActivity;
    private Bus BUS;
    private ArrayList<ImageView> availableSlots = new ArrayList<>();
    private List<Word> availableWords;
    private ArrayList<ImageView> usedSlots = new ArrayList<>();
    private ArrayList<ImageView> wordFlags = new ArrayList<>();
    private ArrayList<ImageView> usedFlags = new ArrayList<>();
    private List<Word> foundWords = new ArrayList<>();


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
        args.putParcelableArrayList(WORDS, words);
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
            availableWords = getArguments().getParcelableArrayList(WORDS);
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

        final int wordsToFind = availableWords.size() <= 4 ? availableWords.size() : 4;

        // Waiting for the layout to be drawn in order to get the correct height and width and draw the word slots
        ViewTreeObserver vto = wordsContainerLinearLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                // Remove the observer
                wordsContainerLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get the layout dimensions
                int width = wordsContainerLinearLayout.getMeasuredWidth();
                int height = wordsContainerLinearLayout.getMeasuredHeight();

                // Choose, as dimension for one slot, the minimum between the width of the layout and the height divided
                // by the number of slots to be drawn minus two margins
                int slotDimen = Math.min(width, (height / wordsToFind));

                int containerPadding = (int) (getResources().getDimension(R.dimen.left_pane_border_width) * 2);
                wordsContainerLinearLayout.setPadding(containerPadding, containerPadding, containerPadding, containerPadding);

                // Draw as much slots as needed
                for (int i = 0; i < wordsToFind; i++) {
                    // Create a Relative as a container for the slot to center it
                    RelativeLayout rl = new RelativeLayout(mActivity);
                    LinearLayout.LayoutParams rParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    rParams.weight = 1;
                    rl.setLayoutParams(rParams);

                    // Create the slot View and add it to the RelativeLayout container
                    ImageView imageView = new ImageView(mActivity);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(slotDimen, slotDimen);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    imageView.setLayoutParams(params);
                    imageView.setImageResource(R.drawable.word_slot);

                    // Create the flag View and add it to the RelativeLayout container
                    int flagDimen = width / 6;
                    ImageView flagImageView = new ImageView(mActivity);
                    RelativeLayout.LayoutParams flagParams = new RelativeLayout.LayoutParams(flagDimen, flagDimen);
                    flagParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    flagParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    flagImageView.setLayoutParams(flagParams);
                    flagImageView.setImageResource(R.drawable.flag_en_selector);
                    flagImageView.setVisibility(View.INVISIBLE);

                    rl.addView(imageView);
                    rl.addView(flagImageView);
                    wordFlags.add(flagImageView);
                    availableSlots.add(imageView);

                    // Add the RelativeLayout container to the main layout
                    wordsContainerLinearLayout.addView(rl);
                }
            }
        });
    }

    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent) {
        if (wordSelectedEvent.isCorrect() && wordSelectedEvent.isNew()) {
            // Move slot to the used ones
            ImageView slot = availableSlots.get(0);
            ImageView flag = wordFlags.get(0);
            usedFlags.add(flag);
            usedSlots.add(slot);
            availableSlots.remove(0);
            wordFlags.remove(0);

            // Move word to the found ones
            Word word = wordSelectedEvent.getWord();
            foundWords.add(word);
            availableWords.remove(word);

            // Add action
            slot.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int index = usedSlots.indexOf(v);
                    Word clickWord = foundWords.get(index);
                    BUS.post(new WordClickedEvent(clickWord, WordClickedEvent.ITALIAN));
                }
            });

            flag.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int index = usedFlags.indexOf(v);
                    Word clickWord = foundWords.get(index);
                    BUS.post(new WordClickedEvent(clickWord, WordClickedEvent.ENGLISH));
                }
            });

            flag.setVisibility(View.VISIBLE);
            slot.setImageBitmap(loadWordBitmap(slot.getWidth(),slot.getHeight()));
        }
    }


    /**
     * Load the word slot
     *
     * @return The Bitmap corresponding to the syllable
     */
    private android.graphics.Bitmap loadWordBitmap(int reqWidth, int reqHeight) {

        InputStream ims = null;
        try {
            ims = mActivity.getAssets().open("words_images/puzzle_pieno.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ims, null, options);

        // Calculate inSampleSize
        options.inSampleSize = Helper.calculateBitmapSize(options, reqWidth, reqHeight);

        try {
            ims.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(ims, null, options);
    }
}
