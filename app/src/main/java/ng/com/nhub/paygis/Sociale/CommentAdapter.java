package ng.com.nhub.paygis.Sociale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.Date;
import java.util.List;

import ng.com.nhub.paygis.R;

public class CommentAdapter extends
        RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> comments;

    private static int[] arrColors = {0xffe56555, 0xfff28c48, 0xffeec764, 0xff76c84d, 0xff5fbed5,
            0xff549cdd, 0xff8e85ee, 0xfff2749a, 0xffd86f65, 0xfff69d61, 0xfffabb3c, 0xff67b35d,
            0xff56a2bb, 0xff5c98cd, 0xff8c79d2, 0xfff37fa6, 0xffca6056, 0xfff18944, 0xfff2b02c,
            0xff56a14c, 0xff4492ac, 0xff4c84b6, 0xff7d6ac4, 0xffe66b94, 0xfff9cbc5, 0xfffdddc8,
            0xfffce5bb, 0xffc0edba, 0xffb8e2f0, 0xffb3d7f7, 0xffcdc4ed, 0xfffed1e0, 0xffca5650,
            0xffd87b29, 0xffc7a21c, 0xff50b232, 0xff42b1a8, 0xff4e92cc, 0xff4e92cc, 0xffdb5b9d};

    // Pass in the contact array into the constructor
    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_comment_viewholder, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String userHandler = comments.get(position).getParseObject("user").getString("handle");
        TextDrawable avaterDrawable = TextDrawable.builder()
                .buildRect(userHandler.substring(0, 1), getColorForId(randInt(1, 100)));
        holder.face.setImageDrawable(avaterDrawable);
        holder.description.setText(comments.get(position).getString("content"));
        holder.title.setText(userHandler);
        if(comments.get(position).getCreatedAt() != null){
            holder.timeAgo.setReferenceTime(comments.get(position).getCreatedAt().getTime());
        }else{
            holder.timeAgo.setReferenceTime(new Date().getTime()); //Now - Parse can't fake it.
        }
    }

    public void append(Comment comment) {
        comments.add(comment);
    }

    public void set(List<Comment> comments){
        clear();
        this.comments.addAll(comments);
    }

    public void clear() {
        comments.clear();
    }

    private static int getColorIndex(int id) {
        if (id >= 0 && id < 8) {
            return id;
        }
        return Math.abs(id % arrColors.length);
    }

    private int getColorForId(int id) {
        return arrColors[getColorIndex(id)];
    }

    private int randInt(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return comments.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public RelativeTimeTextView timeAgo;
        public ImageView face;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            timeAgo = (RelativeTimeTextView)itemView.findViewById(R.id.timeAgo);
            face = (ImageView) itemView.findViewById(R.id.face);
        }
    }
}