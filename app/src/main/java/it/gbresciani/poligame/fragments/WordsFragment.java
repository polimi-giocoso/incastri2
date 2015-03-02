package it.gbresciani.poligame.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.poligame.R;

/**
 * The fragment that shows the list of words
 */
public class WordsFragment extends Fragment {
    private static final String NO_SYLLABLES = "no_syllables";

    private Integer noSyllables;

    @InjectView(R.id.words_container) LinearLayout wordsContainer;


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView;
        if (noSyllables == 2) {
            rootView = inflater.inflate(R.layout.fragment_words_two, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_words_four, container, false);
        }
        ButterKnife.inject(this, rootView);

        return initUI(rootView);
    }

    private View initUI(View rootView){


        return rootView;
    }
}
