package it.gbresciani.poligame.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.gbresciani.poligame.R;

/**
 * The fragment that shows the list of words
 */
public class WordsFragment extends Fragment {
    private static final String WORDS_COUNT = "words_count";

    private Integer mWordsCount;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mWordsCount Parameter 1.
     * @return A new instance of fragment WordsFragment.
     */
    public static WordsFragment newInstance(Integer mWordsCount) {
        WordsFragment fragment = new WordsFragment();
        Bundle args = new Bundle();
        args.putInt(WORDS_COUNT, mWordsCount);
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
            mWordsCount = getArguments().getInt(WORDS_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words, container, false);
    }


}
