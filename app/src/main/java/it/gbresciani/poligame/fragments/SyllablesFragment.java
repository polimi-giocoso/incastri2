package it.gbresciani.poligame.fragments;


import android.app.Fragment;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.io.InputStream;
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
    private ArrayList<View> syllableViews = new ArrayList<>();
    private ArrayList<Integer> syllableViewRotationDegree = new ArrayList<>();
    private ArrayList<Boolean> syllableViewSelection = new ArrayList<>();


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
        for (View view : syllableViews) {
            select(view, false);

        }
    }

    @Subscribe public void wordDismissed(WordDismissedEvent wordDismissedEvent) {
        for (View view : syllableViews) {
            select(view, false);
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

                    View view = createSyllableView(syllables.get(i), slotDimen, slotMargin);
                    ll.addView(view);

                }
            }
        });
    }

    private View createSyllableView(Syllable syllable, int cardDimen, int cardMargin) {

        final ImageView syllableView = new ImageView(mActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardDimen, cardDimen);
        params.setMargins(cardMargin, cardMargin, cardMargin, cardMargin);
        params.gravity = Gravity.CENTER;
        syllableView.setLayoutParams(params);


        syllableView.setImageBitmap(loadSyllableBitmap(syllable, cardDimen, cardDimen));

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

        syllableView.startAnimation(animSet);

        syllableView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // Get the Syllable based on the index of the selected view
                //TODO Use HashMap
                Syllable syllable = syllables.get(syllableViews.indexOf(v));
                select(v, true);
                BUS.post(new SyllableSelectedEvent(syllable));
            }
        });

        syllableViews.add(syllableView);
        syllableViewRotationDegree.add(degree);
        syllableViewSelection.add(false);

        return syllableView;
    }

    /**
     *
     * Put the given view in a "selected" or "unselected"
     *
     * @param v The view to select
     * @param selected Whether if select or unselect
     */
    private void select(View v, boolean selected) {
        int cardIndex = syllableViews.indexOf(v);
        float viewDegree = syllableViewRotationDegree.get(cardIndex);
        boolean viewSelection = syllableViewSelection.get(cardIndex);
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
        syllableViewSelection.set(cardIndex, selected);
    }


    /**
     *
     * Given a Syllable load the corresponding Bitmap
     *
     * @param syllable
     * @return The Bitmap corresponding to the syllable
     */
    private android.graphics.Bitmap loadSyllableBitmap(Syllable syllable, int reqWidth, int reqHeight) {

        InputStream ims = null;
        try {
            ims = mActivity.getAssets().open("syllable_images/" + syllable.getVal() + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ims, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateSyllableImageSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(ims, null, options);
    }


    /**
     * Calculates the options.inSampleSize to decode the image in a smaller size to prevent java.lang.OutofMemoryError
     *
     * @param options   the options of the bitmap decoder containing height and width calculated with options.inJustDecodeBounds
     * @param reqWidth  the desired image width
     * @param reqHeight the desired image height
     * @return the size of the desired bitmap
     */
    public static int calculateSyllableImageSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
