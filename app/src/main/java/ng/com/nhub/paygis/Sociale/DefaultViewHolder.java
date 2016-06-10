package ng.com.nhub.paygis.Sociale;

import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;

import ng.com.nhub.paygis.R;
import ng.com.nhub.paygis.UI.LaImageView;
import ng.com.nhub.paygis.lib.AndroidUtilities;
import ng.com.nhub.paygis.lib.LocaleController;

public class DefaultViewHolder extends RecyclerView.ViewHolder {

    private static int[] arrColors = {0xffe56555, 0xfff28c48, 0xffeec764, 0xff76c84d, 0xff5fbed5,
            0xff549cdd, 0xff8e85ee, 0xfff2749a, 0xffd86f65, 0xfff69d61, 0xfffabb3c, 0xff67b35d,
            0xff56a2bb, 0xff5c98cd, 0xff8c79d2, 0xfff37fa6, 0xffca6056, 0xfff18944, 0xfff2b02c,
            0xff56a14c, 0xff4492ac, 0xff4c84b6, 0xff7d6ac4, 0xffe66b94, 0xfff9cbc5, 0xfffdddc8,
            0xfffce5bb, 0xffc0edba, 0xffb8e2f0, 0xffb3d7f7, 0xffcdc4ed, 0xfffed1e0, 0xffca5650,
            0xffd87b29, 0xffc7a21c, 0xff50b232, 0xff42b1a8, 0xff4e92cc, 0xff4e92cc, 0xffdb5b9d};
    public TextView title;
    public TextView description;
    public RelativeTimeTextView timeAgo;
    public ImageView face;
    public RelativeLayout medialayout;
    public RatingBar ratingBar;
    public final RatingBar totalRating;
    public TextView numRaters;
    public TextView numComments;

    public DefaultViewHolder(View itemView) {

        super(itemView);

        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        timeAgo = (RelativeTimeTextView)itemView.findViewById(R.id.timeAgo);
        face = (ImageView) itemView.findViewById(R.id.face);
        medialayout = (RelativeLayout) itemView.findViewById(R.id.mediaLayout);
        totalRating = (RatingBar) itemView.findViewById((R.id.full_rating));
        ratingBar = (RatingBar) itemView.findViewById((R.id.rating));

        numRaters = (TextView) itemView.findViewById(R.id.num_raters);
        numComments = (TextView) itemView.findViewById(R.id.num_comments);
    }


    public void create(ParseObject socialMessage) {

        if(socialMessage.getParseFile("image") == null){
            medialayout.setVisibility(View.GONE);
        }else{
            // TODO* Support for Multi-Picture Upload
            for (int i = 0; i < 1; i++){
                LaImageView contentImage = new LaImageView(medialayout.getContext());
                Uri uri = Uri.parse(socialMessage.getParseFile("image").getUrl());
                String fileStr = socialMessage.getParseFile("image").getUrl().toLowerCase(Locale.ENGLISH);
                if(fileStr.endsWith(".gif")){
                    contentImage.showDynamicImage(uri);
                }else{
                    contentImage.showStaticImage(uri);
                }
                contentImage.setTag(uri);
                // contentImage.setPadding(0, AndroidUtilities.dp(15), 0, AndroidUtilities.dp(15));
                contentImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                contentImage.setPadding(AndroidUtilities.dp(15), AndroidUtilities.dp(15),
                        AndroidUtilities.dp(15), AndroidUtilities.dp(15));

                medialayout.addView(contentImage);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) contentImage.getLayoutParams();
                layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                contentImage.setLayoutParams(layoutParams);
                contentImage.setVisibility(View.VISIBLE);


                medialayout.requestLayout();
                medialayout.setVisibility(View.VISIBLE);
            }
        }
        TextDrawable avaterDrawable = TextDrawable.builder()
                .buildRect(socialMessage.getParseObject("user").getString("handle").substring(0, 1), getColorForId(randInt(1, 100)));
        face.setImageDrawable(avaterDrawable);
        face.setVisibility(View.VISIBLE);
        if(socialMessage.getString("content") != null && socialMessage.getString("content").trim().length() == 0){
            description.setVisibility(View.GONE);
        }else{
            description.setText(socialMessage.getString("content"));
            description.setVisibility(View.VISIBLE);
        }
        title.setText(socialMessage.getParseObject("user").getString("handle"));
        title.setVisibility(View.VISIBLE);
        timeAgo.setReferenceTime(socialMessage.getCreatedAt().getTime());
        timeAgo.setVisibility(View.VISIBLE);


        float rating = 0;
        int nRaters = 0;
        List<UserRating> rawRating = socialMessage.getList("ratings");
        if(rawRating != null && rawRating.size() >= 1){
            double sum = 0;
            boolean hasRated = false;
            float curUserRating = 1; //(default - same as in XML
            String curUserId = ParseUser.getCurrentUser().getObjectId();
            for(UserRating userRating : rawRating){
                sum += userRating.getDouble("rating");
                if(userRating.getString("userId").equals(curUserId) ||
                        userRating.getString("userId") == curUserId){
                    hasRated = true;
                    curUserRating = (float) userRating.getDouble("rating");
                }
            }
            nRaters = rawRating.size();
            rating = (float) (sum / nRaters); //div by 0 avoided in the IF statement
            if (hasRated){
                ratingBar.setEnabled(false);
                ratingBar.setRating(curUserRating);
                ratingBar.setIsIndicator(true);
                ratingBar.setFocusable(false);
            }else{
                ratingBar.setRating(1);
            }
            totalRating.setRating(rating);
        }else{
            ratingBar.setEnabled(true);
            ratingBar.setNumStars(5);
            ratingBar.setMax(5);
            ratingBar.setRating(1);
            ratingBar.setIsIndicator(false);
        }
//        ratingBar.setVisibility(View.VISIBLE);
        if (nRaters <= 1){
            numRaters.setText(String.valueOf(nRaters) + " Rater,");
        }else{
            numRaters.setText(String.valueOf(nRaters) + " Raters,");
        }
        if (socialMessage.getInt("nComments") <= 1){
            numComments.setText(String.valueOf(socialMessage.getInt("nComments")) + " Comment");
        }else{
            numComments.setText(String.valueOf(socialMessage.getInt("nComments")) + " Comments");
        }
    }

    public void clear() {
        medialayout.removeAllViews();
    }

    public static int getColorIndex(int id) {
        if (id >= 0 && id < 8) {
            return id;
        }
        return Math.abs(id % arrColors.length);
    }

    public static int getColorForId(int id) {
        return arrColors[getColorIndex(id)];
    }

    public static int randInt(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}