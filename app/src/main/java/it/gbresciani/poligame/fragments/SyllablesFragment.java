package it.gbresciani.poligame.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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

                // Add 2 LinearLayout
                for (int i = 0; i < 2; i++) {
                    LinearLayout ll = new LinearLayout(mActivity);
                    LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                    ll.setOrientation(LinearLayout.VERTICAL);
                    ll.setLayoutParams(lparams);

                    // Add 1 or 2 slot according to the preferences
                    for (int j = 0; j < noSyllables / 2; j++) {
                        // Create a Relative as a container for the slot to center it
                        RelativeLayout rl = new RelativeLayout(mActivity);
                        LinearLayout.LayoutParams rParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        rParams.weight = 1;
                        rl.setLayoutParams(rParams);

                        View slot = new View(mActivity);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(slotDimen, slotDimen);
                        params.setMargins(slotMargin, slotMargin, slotMargin, slotMargin);
                        params.addRule(RelativeLayout.CENTER_IN_PARENT);
                        params.setMargins(slotMargin, slotMargin, slotMargin, slotMargin);
                        slot.setBackgroundResource(R.drawable.shape_empty_slot);
                        slot.setLayoutParams(params);
                        rl.addView(slot);

                        // Add the RelativeLayout container to the main layout
                        ll.addView(rl);
                    }

                    syllablesContainerLinearLayout.addView(ll);
                }
            }
        });
    }
}
