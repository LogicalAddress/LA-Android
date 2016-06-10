package ng.com.nhub.paygis.Sociale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.ParseObject;

import java.util.ArrayList;

import ng.com.nhub.paygis.R;

/**
 * Created by retnan on 4/8/16.
 */
public class PostAdapter extends RecyclerView.Adapter<DefaultViewHolder> {

    // The items to display in your RecyclerView
    private ArrayList<com.parse.ParseObject> mMessages;
    SectionClickListener listener;
    Context context;

    private final static Object sync = new Object();

    public PostAdapter(ArrayList<com.parse.ParseObject> messages, Context _context, SectionClickListener l) {
        this.mMessages = messages;
        listener = l;
        context = _context;
    }

    /**
     * This method creates different RecyclerView.ViewHolder objects based on the item view type.\
     *
     * @param viewGroup ViewGroup container for the item
     * @param viewType type of view to be inflated
     * @return viewHolder to be inflated
     */

    @Override
    public DefaultViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view;
        switch (viewType) {
            case ContentTypes.EMERGENCY:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_emergency_viewholder, viewGroup, false);
                return new EmergencyViewHolder(view);
            case ContentTypes.POLL:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_poll_viewholder, viewGroup, false);
                return new PollViewHolder(view);
            case ContentTypes.HISTORY:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_history_viewholder, viewGroup, false);
                return new HistoryViewHolder(view);
            case ContentTypes.OPINION:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_opinion_viewholder, viewGroup, false);
                return new OpinionViewHolder(view);
            case ContentTypes.BUSINESS:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_business_viewholder, viewGroup, false);
                return new BusinessViewHolder(view);
            case ContentTypes.NEWS:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_news_viewholder, viewGroup, false);
                return new NewsViewHolder(view);
            case ContentTypes.HEALTH:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_health_viewholder, viewGroup, false);
                return new HealthViewHolder(view);
            case ContentTypes.SOCIAL:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_social_viewholder, viewGroup, false);
                return new SocialViewHolder(view);
            case ContentTypes.ROBOT:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_robot_viewholder, viewGroup, false);
                return new RobotViewHolder(view);
            default:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_default_viewholder, viewGroup, false);
                return new DefaultViewHolder(view);
        }
    }

    private void setUpListeners(View view, final int position) {

        view.findViewById(R.id.mediaLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup children = ((ViewGroup) v);
                if (children.getChildAt(0) instanceof SimpleDraweeView){
                    listener.onImageClick(children.getChildAt(0), position);
                }
            }
        });

        RatingBar rating = (RatingBar) view.findViewById(R.id.rating);
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(fromUser){
                    listener.onRate(ratingBar, rating, mMessages.get(position), position);
                }
            }
        });

        Button commentBtn = (Button) view.findViewById(R.id.btn_comment);
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCommentClick(v, position);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {

        if (mMessages.get(position).getString("contentType").equals("emergency")) {
            return ContentTypes.EMERGENCY;
        } else if (mMessages.get(position).getString("contentType").equals("poll")) {
            return ContentTypes.POLL;
        }else if (mMessages.get(position).getString("contentType").equals("history")) {
            return ContentTypes.HISTORY;
        }else if (mMessages.get(position).getString("contentType").equals("opinion")) {
            return ContentTypes.OPINION;
        }else if (mMessages.get(position).getString("contentType").equals("business")) {
            return ContentTypes.BUSINESS;
        }else if (mMessages.get(position).getString("contentType").equals("news")) {
            return ContentTypes.NEWS;
        }else if (mMessages.get(position).getString("contentType").equals("health")) {
            return ContentTypes.HEALTH;
        }else if (mMessages.get(position).getString("contentType").equals("social")) {
            return ContentTypes.SOCIAL;
        }else if (mMessages.get(position).getString("contentType").equals("robot")) {
            return ContentTypes.ROBOT;
        }
        return ContentTypes.DEFAULT;
    }

    /**
     * This method internally calls onBindViewHolder(ViewHolder, int) to update the
     * RecyclerView.ViewHolder contents with the item at the given position
     * and also sets up some private fields to be used by RecyclerView.
     *
     * @param viewHolder The type of RecyclerView.ViewHolder to populate
     * @param position Item position in the viewgroup.
     */
    @Override
    public void onBindViewHolder(DefaultViewHolder viewHolder, int position) {
        
        switch (viewHolder.getItemViewType()) {
            case ContentTypes.EMERGENCY:
                EmergencyViewHolder vh1 = (EmergencyViewHolder) viewHolder;
                configureEmergencyView(vh1, position);
                break;
            case ContentTypes.POLL:
                PollViewHolder vh2 = (PollViewHolder) viewHolder;
                configurePollView(vh2, position);
                break;
            case ContentTypes.HISTORY:
                HistoryViewHolder vh3 = (HistoryViewHolder) viewHolder;
                configureHistoryView(vh3, position);
                break;
            case ContentTypes.OPINION:
                OpinionViewHolder vh4 = (OpinionViewHolder) viewHolder;
                configureOpinionView(vh4, position);
                break;
            case ContentTypes.BUSINESS:
                BusinessViewHolder vh5 = (BusinessViewHolder) viewHolder;
                configureBusinessView(vh5, position);
                break;
            case ContentTypes.NEWS:
                NewsViewHolder vh6 = (NewsViewHolder) viewHolder;
                configureNewsView(vh6, position);
                break;
            case ContentTypes.HEALTH:
                HealthViewHolder vh7 = (HealthViewHolder) viewHolder;
                configureHealthView(vh7, position);
                break;
            case ContentTypes.SOCIAL:
                SocialViewHolder vh8 = (SocialViewHolder) viewHolder;
                configureSocialView(vh8, position);
                break;
            case ContentTypes.ROBOT:
                RobotViewHolder vh9 = (RobotViewHolder) viewHolder;
                configureRobotView(vh9, position);
                break;
            default:
                DefaultViewHolder vh = viewHolder;
                configureDefaultViewHolder(vh, position);
                break;
        }
    }

    private void configureDefaultViewHolder(DefaultViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configureNewsView(NewsViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configureBusinessView(BusinessViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configureOpinionView(OpinionViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configureHistoryView(HistoryViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configurePollView(PollViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configureEmergencyView(EmergencyViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configureHealthView(HealthViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configureSocialView(SocialViewHolder viewHolder, int position) {
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    private void configureRobotView(RobotViewHolder viewHolder, int position){
        viewHolder.clear();
        viewHolder.create(mMessages.get(position));
        setUpListeners(viewHolder.itemView, position);
    }

    @Override
    public long getItemId(int position) {
        return convertParseIdToLongInt(getItem(position).getObjectId());
    }

    public static long convertParseIdToLongInt(String contentId) {
        long id = 0;
        for (int i = 0; i < contentId.length(); i++){
            id += Character.getNumericValue(contentId.charAt(i));
        }
        return id;
    }

    @Override
    public int getItemCount() {
        return this.mMessages.size();
    }

    public void append(ParseObject messageObject) {
        synchronized (sync) {
            mMessages.add(messageObject);
        }
    }

    public void set(int position, ParseObject parseObject){
        synchronized (sync) {
            mMessages.set(position, parseObject);
        }
    }

    public void clear() {
        synchronized (sync) {
            mMessages.clear();
        }
    }

    public ParseObject getItem(int position){
        return mMessages.get(position);
    }

    public void replace(ArrayList<com.parse.ParseObject> newList) {
        synchronized (sync) {
            mMessages = newList;
        }
    }

    public void removeItem(int position){
        mMessages.remove(position);
        notifyItemRemoved(position);
    }
}