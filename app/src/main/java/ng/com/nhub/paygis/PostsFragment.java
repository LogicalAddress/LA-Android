

package ng.com.nhub.paygis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ng.com.nhub.paygis.Sociale.DividerItemDecoration;
import ng.com.nhub.paygis.Sociale.PostAdapter;
import ng.com.nhub.paygis.Sociale.SectionClickListener;
import ng.com.nhub.paygis.Sociale.SimpleItemTouchHelperCallback;
import ng.com.nhub.paygis.Sociale.SwipePostListener;
import ng.com.nhub.paygis.Sociale.UserRating;
import ng.com.nhub.paygis.etc.AppData;

public class PostsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "PostsFragment";
    public static int COMMENT_FRAG_OPEN = 1003;

    private boolean socialLoaded = false;
    private boolean userSpaceLoaded = false;
    private boolean flag = false;

    private SwipeRefreshLayout swipeContainer;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    PostAdapter mAdapter;

    public RecyclerView mRecyclerView;

    private OnMessageListFragmentInteractionListener mListener;
    private CardView welcomeTipView;

    public PostsFragment() {
        // Required empty public constructor
    }

    public static PostsFragment newInstance(String param1, String param2) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_message_list, container, false);
        mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.messageList);
        welcomeTipView = (CardView) fragmentView.findViewById(R.id.start_up_tip);
        YoYo.with(Techniques.Tada)
                .duration(700)
                .playOn(welcomeTipView);
        if(AppData.getHome().longitude == 0.0 && AppData.getHome().latitude == 0.0){
            welcomeTipView.setVisibility(View.VISIBLE);
            Button getStarted = (Button) fragmentView.findViewById(R.id.get_started);
            getStarted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    startActivity(intent);
                }
            });
        }
        mAdapter = new PostAdapter(new ArrayList<ParseObject>(), getContext(), new SectionClickListener() {
            boolean rateClicked = false;
            @Override
            public void onImageClick(View contentImage, int position) {
                Intent intent = new Intent(getActivity(), ImageViewer.class);
                 ParseObject parseObject = mAdapter.getItem(position);
                 intent.putExtra("imageUri", parseObject.getParseFile("image").getUrl());
                if (contentImage.getTag().toString().endsWith(".gif")){
                    return;
                }
                intent.putExtra("imageUri", contentImage.getTag().toString());
                startActivity(intent);
            }

            @Override
            public void onRate(RatingBar ratingBar, final float rating,
                               ParseObject thePostObject, int position) {
                if(rateClicked) return;
                rateClicked = true;

                ratingBar.setEnabled(false);
                ratingBar.setRating(rating);
                ratingBar.setIsIndicator(true);
                ratingBar.setFocusable(false);

                List<UserRating> rawRating = thePostObject.getList("ratings");
                if(rawRating != null && rawRating.size() >= 1) {
                    String curUserId = ParseUser.getCurrentUser().getObjectId();
                    for (UserRating userRating : rawRating) {
                        if (userRating.getString("userId").equals(curUserId) ||
                                userRating.getString("userId") == curUserId) {
                            rateClicked = false;
                            return;
                        }
                    }
                }
                UserRating ratingObj = new UserRating();
                ratingObj.put("userId", ParseUser.getCurrentUser().getObjectId());
                ratingObj.put("postId", thePostObject.getObjectId());
                ratingObj.put("rating", rating);
                thePostObject.add("ratings", ratingObj);
                mRecyclerView.getAdapter().notifyItemChanged(position);
                rateClicked = false;
                thePostObject.saveEventually();
            }

            @Override
            public void onCommentClick(View v, int position) {
                ParseObject parseObjectItem = mAdapter.getItem(position);
                Fragment commentFragment = CommentsFragment.newInstance(parseObjectItem.getObjectId(),
                        parseObjectItem);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_comment_container, commentFragment, "COMMENT-FRAG");
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.addToBackStack(null);
                transaction.commit();
                if (mListener != null) {
                    mListener.onMessageListFragmentInteraction(COMMENT_FRAG_OPEN);
                }
                // From within a nested fragment, you can get a reference to the parent
                // fragment by calling getParentFragment().
            }
        });
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        SlideInUpAnimator slideInUpAnimator = new SlideInUpAnimator();
//        slideInUpAnimator.setAddDuration(200);
//        mRecyclerView.setItemAnimator(slideInUpAnimator);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (getActivity() != null) {
                    ((OnMessageListScrollListener) getActivity()).onMessageListScroll(recyclerView,
                    newState);
                }
            }
        });

        /*ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(new SwipePostListener() {
                    @Override
                    public void onItemDismiss(int position) {
                        ParseObject parseObjectItem = mAdapter.getItem(position);
                        mAdapter.removeItem(position);
//                        Toast.makeText(getActivity(), "Not Implemented Yet", Toast.LENGTH_SHORT).show();
                    }
                });
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);*/

        swipeContainer = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(welcomeTipView.getVisibility() == View.VISIBLE){
                    swipeContainer.setRefreshing(false);
                    return;
                }
                loadData(true);
            }
        });
        // loadData();
        if(welcomeTipView.getVisibility() == View.GONE){
            //re-using the progress bar
            swipeContainer.post(new Runnable() {
                @Override public void run() {
                    swipeContainer.setRefreshing(true);
                    loadData(false);
                }
            });
        }

        return fragmentView;
    }

    public void loadData(final boolean refresh){
        loadUserSpaceDataInBackground(refresh);
        loadSocialDataInBackground(refresh);
    }

    private void loadUserSpaceDataInBackground(final boolean refresh) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserSpace");
        query.addDescendingOrder("createdAt").include("post")
                .whereEqualTo("user_id", ParseUser.getCurrentUser().getObjectId())
                .include("post.ratings").include("post.user").setLimit(50);
        if(refresh){
            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        }else{
            if(query.hasCachedResult()){
                query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ONLY);
            }else{
                query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
            }
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> rawParseObjects, ParseException e) {
                userSpaceLoaded = true;
                if (e == null) {
                    if(!flag){
                        mAdapter.clear();
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        flag = true;
                    }
                    for (ParseObject parseObject : rawParseObjects) {
                        mAdapter.append(parseObject.getParseObject("post"));
                    }
                    if(userSpaceLoaded & socialLoaded){
                        userSpaceLoaded = false;
                        socialLoaded = false;
                        flag = false;
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        if (swipeContainer.isRefreshing()) {
                            swipeContainer.setRefreshing(false);
                        }
                    }
                }
            }
        });
    }

    private void loadSocialDataInBackground(final boolean refresh) {
        ParseGeoPoint userLocation = new ParseGeoPoint(AppData.getHome().latitude,
                AppData.getHome().longitude);
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery("Posts");
        query.addDescendingOrder("createdAt").whereEqualTo("contentType", "social")
                .include("user").include("ratings").whereNear("location", userLocation).setLimit(50);
        if(refresh){
            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        }else{
            if(query.hasCachedResult()){
                query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ONLY);
            }else{
                query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
            }
        }
        query.findInBackground(new FindCallback<com.parse.ParseObject>() {
            @Override
            public void done(List<com.parse.ParseObject> rawParseObjects, ParseException e) {
                socialLoaded  = true;
                if (e == null){
                    if(!flag){
                        mAdapter.clear();
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        flag = true;
                    }
                    for (com.parse.ParseObject parseObject : rawParseObjects) {
                        mAdapter.append(parseObject);
                    }
                    if(userSpaceLoaded & socialLoaded){
                        userSpaceLoaded = false;
                        socialLoaded = false;
                        flag = false;
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        if (swipeContainer.isRefreshing()){
                            swipeContainer.setRefreshing(false);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMessageListFragmentInteractionListener) {
            mListener = (OnMessageListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMessageListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMessageListFragmentInteractionListener {
        void onMessageListFragmentInteraction(int actionCode);
    }

    public interface  OnMessageListScrollListener{
        void onMessageListScroll(RecyclerView recyclerView, int newState);
    }

    public void hideWelcomeCardIfVisible(){
        if(AppData.getHome().longitude == 0.0 &&
                AppData.getHome().latitude == 0.0){
            return;
        }
        if(welcomeTipView.getVisibility() == View.VISIBLE){
            welcomeTipView.setVisibility(View.GONE);
            swipeContainer.post(new Runnable() {
                @Override public void run() {
                    swipeContainer.setRefreshing(true);
                    loadData(true);
                }
            });
        }
    }
}