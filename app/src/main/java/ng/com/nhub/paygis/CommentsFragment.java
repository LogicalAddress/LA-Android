package ng.com.nhub.paygis;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ng.com.nhub.paygis.Sociale.Comment;
import ng.com.nhub.paygis.Sociale.CommentAdapter;
import ng.com.nhub.paygis.Sociale.DividerItemDecoration;

public class CommentsFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "postId";
    private String postId;
    private SwipeRefreshLayout swipeContainer;
    CommentAdapter commentAdapter;
    public RecyclerView cRecyclerView;
    private EditText userComment;
    private ImageView btnSend;
//    private ParseObject post;
    public static ParseObject post;

    public CommentsFragment() {
        // Required empty public constructor
    }

    public static CommentsFragment newInstance(String postId, ParseObject parseObjectItem) {
        post = parseObjectItem; //bad but should work
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_PARAM1);
        }else{
            throw new RuntimeException("Use the newInstance method");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_comment, container, false);
        cRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.commentList);
        userComment = (EditText) fragmentView.findViewById(R.id.user_comment);
        userComment.setFilters(new InputFilter[] {new InputFilter.LengthFilter(140)});
        btnSend = (ImageView) fragmentView.findViewById(R.id.send);
        // btnSend.setEnabled(false);
        btnSend.setOnClickListener(this);
        commentAdapter = new CommentAdapter(new ArrayList<Comment>());
        cRecyclerView.setAdapter(commentAdapter);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        cRecyclerView.addItemDecoration(itemDecoration);
        cRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SlideInDownAnimator slideInDownAnimator = new SlideInDownAnimator();
        slideInDownAnimator.setAddDuration(700);
        cRecyclerView.setItemAnimator(slideInDownAnimator); //not working joor
        swipeContainer = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipeCommentContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(true);
            }
        });
        // loadData();
        //re-using the progress bar
        swipeContainer.post(new Runnable() {
            @Override public void run() {
                swipeContainer.setRefreshing(true);
                loadData(false);
            }
        });

        return fragmentView;
    }

    public void loadData(final boolean refresh){
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.addDescendingOrder("createdAt");
        query.whereEqualTo("post", post);
        query.include("user");
        query.setLimit(50);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> commentList, ParseException e) {
                if (e == null){
                    if (swipeContainer.isRefreshing()){
                        swipeContainer.setRefreshing(false);
                        commentAdapter.clear();
                        commentAdapter.notifyDataSetChanged();
                    }
                    if(commentList.size() > 0){
                        commentAdapter.set(commentList);
                        commentAdapter.notifyItemInserted(commentAdapter.getItemCount() - 1);
                    }
                    // btnSend.setEnabled(true);
                }
            }
        });

//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
//        query.whereEqualTo("objectId", postId);
//        query.getFirstInBackground(new GetCallback<ParseObject>() {
//            @Override
//            public void done(ParseObject postItem, ParseException e) {
//                if (e == null){
//                    post = postItem;
//                    btnSend.setEnabled(true);
//                    ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
//                    query.addDescendingOrder("createdAt");
//                    query.whereEqualTo("post", post);
//                    //        query.whereEqualTo("parent", postId);
//                    query.include("user");
//                    query.setLimit(50);
//                    if(refresh){
//                        query.clearCachedResult();
//                    }else{
//                        if(query.hasCachedResult()){
//                            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
//                        }
//                    }
//                    query.findInBackground(new FindCallback<Comment>() {
//                        @Override
//                        public void done(List<Comment> commentList, ParseException e) {
//                            if (e == null){
//                                if (swipeContainer.isRefreshing()){
//                                    swipeContainer.setRefreshing(false);
//                                    commentAdapter.clear();
//                                    commentAdapter.notifyDataSetChanged();
//                                }
//                                commentAdapter.set(commentList);
////                                for (Comment comment : commentList) {
////                                    commentAdapter.append(comment);
////                                }
//                                //commentAdapter.notifyDataSetChanged();
//                                commentAdapter.notifyItemInserted(commentAdapter.getItemCount() - 1);
//                            }
//                        }
//                    });
//                }
//            }
//        });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send:
                if(validate()){
                    sendComment();
                }
                break;
        }
    }

    private boolean validate(){
        if(userComment.getText().length() == 0 || post == null){
            return false;
        }
        return true;
    }

    private void sendComment(){
        Comment myComment = new Comment();
        myComment.put("user", ParseUser.getCurrentUser());
        myComment.put("content", userComment.getText().toString());
        myComment.put("post", post);
        myComment.saveEventually();
        post.increment("nComments"); //cache
        post.saveEventually();
        commentAdapter.append(myComment);
        commentAdapter.notifyDataSetChanged();
        userComment.setText("");
    }
}