package edu.brown.engn931.diningdashboard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class HomeFragment extends Fragment {
    ArrayList<Card> cards;
    SwipeRefreshLayout listSwipeView;
    ProgressBar homeSpinner;

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        setUpRefresh(view);
        if (getActivity() != null) {
            MainActivity.cardsLoaded = false;
            if (MainActivity.taskFinished) {
                loadCards(view);
            }
        }
        return view;
    }

    /**
     * Sets up the pull to refresh mechanism
     *
     * @param view - the current view, used for layout inflation of the swiperefreshlayouts
     */
    public void setUpRefresh(View view) {
        listSwipeView = (SwipeRefreshLayout) view.findViewById(R.id.listRefreshView);

        listSwipeView.setColorSchemeResources(R.color.red_primary);

        listSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int connection = MainActivity.getNetworkState(getActivity());
                if (connection == 1 || connection == 2) {
                    listSwipeView.setRefreshing(true);
                    MainActivity.taskFinished = false;
                    MainActivity.cardsLoaded = false;
                    new SwipeRefreshTask(getActivity()).execute();
                }
            }
        });
    }

    public void loadCards(View view) {
        DiningCard andrewsCard = new DiningCard(getActivity(), R.drawable.bk_andrews2, "Andrews Commons");
        DiningCard blueroomCard = new DiningCard(getActivity(), R.drawable.bk_blueroom2, "Blue Room");
        DiningCard josCard = new DiningCard(getActivity(), R.drawable.bk_jos, "Josiah's");
        DiningCard rattyCard = new DiningCard(getActivity(), R.drawable.bk_ratty, "Ratty");
        DiningCard vdubCard = new DiningCard(getActivity(), R.drawable.bk_vdub2, "V-Dub");

        cards = new ArrayList<>();
        cards.add(andrewsCard);
        cards.add(blueroomCard);
        cards.add(josCard);
        cards.add(rattyCard);
        cards.add(vdubCard);

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
        CardListView listView = (CardListView) view.findViewById(R.id.diningCardList);
        if (listView != null) {
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(mCardArrayAdapter);
        }

        AnimationAdapter animCardArrayAdapter = new SwingBottomInAnimationAdapter(mCardArrayAdapter);
        animCardArrayAdapter.setAbsListView(listView);
        listView.setExternalAdapter(animCardArrayAdapter, mCardArrayAdapter);
        MainActivity.cardsLoaded = true;
        if (homeSpinner == null) {
            homeSpinner = (ProgressBar) view.findViewById(R.id.homeSpinner);
            homeSpinner.setVisibility(View.GONE);
        }
        homeSpinner.setVisibility(View.GONE);
        if (listSwipeView == null) {
            listSwipeView = (SwipeRefreshLayout) view.findViewById(R.id.listRefreshView);
        }
        listSwipeView.setVisibility(View.VISIBLE);
    }

    public void displayCards() {
        loadCards(getView());
    }

    public class SwipeRefreshTask extends AsyncTask<Void, Void, Boolean> {
        Context c;

        public SwipeRefreshTask(Context c) {
            this.c = c;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            new HoursTask(c).execute();
            new CapacityTask(c).execute();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (listSwipeView == null && getView() != null) {
                listSwipeView = (SwipeRefreshLayout) getView().findViewById(R.id.listRefreshView);
            }
            if (listSwipeView != null) {
                listSwipeView.setRefreshing(false);
            }
        }


    }
}
