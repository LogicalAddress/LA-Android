package ng.com.nhub.paygis.Sociale;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseObject;

import ng.com.nhub.paygis.R;
import ng.com.nhub.paygis.UI.LaImageView;

/**
 * Created by retnan on 5/31/16.
 */
public class RobotViewHolder extends DefaultViewHolder{


    private final FrameLayout priceTag;

    public RobotViewHolder(View itemView) {
        super(itemView);
        priceTag = (FrameLayout) itemView.findViewById(R.id.priceTag);
    }

    public void create(ParseObject robotMessage) {
        super.create(robotMessage);
        priceTag.bringToFront();
    }

    @Override
    public void clear() {
        for(int i = 0; i < medialayout.getChildCount(); i++){
            if (medialayout.getChildAt(i) instanceof LaImageView){
                medialayout.removeViewAt(i);
            }
        }
    }
}
