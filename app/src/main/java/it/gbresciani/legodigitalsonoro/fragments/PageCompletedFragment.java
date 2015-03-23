package it.gbresciani.legodigitalsonoro.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.OnClick;
import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.events.NextPageEvent;
import it.gbresciani.legodigitalsonoro.helper.BusProvider;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PageCompletedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PageCompletedFragment extends Fragment {

    private Bus BUS;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PageCompleted.
     */
    // TODO: Rename and change types and number of parameters
    public static PageCompletedFragment newInstance() {
        PageCompletedFragment fragment = new PageCompletedFragment();

        return fragment;
    }

    public PageCompletedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BUS = BusProvider.getInstance();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_page_completed, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @OnClick(R.id.next_page_image_view)
    public void nextPage(){
        BUS.post(new NextPageEvent());
    }
}
