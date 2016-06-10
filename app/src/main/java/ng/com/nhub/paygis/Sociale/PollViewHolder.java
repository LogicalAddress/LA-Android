package ng.com.nhub.paygis.Sociale;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseObject;

import ng.com.nhub.paygis.R;
import ng.com.nhub.paygis.etc.ApplicationLoader;
import ng.com.nhub.paygis.lib.AndroidUtilities;

/**
 * Created by retnan on 4/8/16.
 */
public class PollViewHolder extends DefaultViewHolder {

    LinearLayout pollLayout;

    public PollViewHolder(View itemView) {
        super(itemView);
        pollLayout = (LinearLayout) itemView.findViewById(R.id.poll_container);
    }

    //Remember that all polls have expiring date/time

    public void create(ParseObject pollMessage){

        super.create(pollMessage);

        for (int i = 0; i < 3; i++){
            TextView textView = new TextView(ApplicationLoader.appContext);
            textView.setText("Buhari Jonathan");
            textView.setTextColor(0xff757575);

            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setTextColor(0xff000000);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setMaxLines(1);
            textView.setSingleLine();
            textView.setBackgroundResource(R.drawable.poll_choice_button_bg);
            textView.setPadding(0, AndroidUtilities.dp(15), 0, AndroidUtilities.dp(15));

            pollLayout.addView(textView);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.topMargin = AndroidUtilities.dp(4);
            layoutParams.bottomMargin = AndroidUtilities.dp(4); //(4+4)=8dp
            layoutParams.leftMargin = AndroidUtilities.dp(24);
            layoutParams.rightMargin = AndroidUtilities.dp(24);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            textView.setLayoutParams(layoutParams);

//            textView.setId(/*Random Generated Id*/-1);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(ApplicationLoader.appContext, "ResId: "+String.valueOf(v.getId()),
//                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void clear() {
        super.clear();
        pollLayout.removeAllViews();
    }
}
