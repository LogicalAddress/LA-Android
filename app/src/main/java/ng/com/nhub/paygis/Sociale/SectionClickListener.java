package ng.com.nhub.paygis.Sociale;

import android.view.View;
import android.widget.RatingBar;

import com.parse.ParseObject;


public interface SectionClickListener {
    void onImageClick(View v, int position);

    void onRate(RatingBar ratingBar, float rating, ParseObject theParseObject, int position);

    void onCommentClick(View v, int position);
}
