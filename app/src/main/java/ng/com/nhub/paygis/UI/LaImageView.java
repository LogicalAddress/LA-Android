package ng.com.nhub.paygis.UI;

import android.content.Context;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import ng.com.nhub.paygis.R;
import ng.com.nhub.paygis.etc.ApplicationLoader;

/**
 * Created by retnan on 5/26/16.
 */
public class LaImageView extends SimpleDraweeView {

    private Context context;

    public static final int RENDER_FRESCO = 0;
    public static final int RENDER_GLIDE = 1;

    private int renderType = 0;

    public LaImageView(Context _context) {
        super(_context, getConfig());
        context = _context;
    }

    public void setRenderingLibary(int _renderType){
        renderType = _renderType;
    }

    public void showStaticImage(Uri uri){
        switch (renderType){
            case RENDER_FRESCO:{
                setImageURI(uri);
                return;
            }
            case RENDER_GLIDE:{
                Glide.with(context).load(uri).into(this);
                return;
            }
            default:{
                setImageURI(uri);
            }
        }
    }
    public void showDynamicImage(Uri uri){
        switch (renderType){
            case RENDER_FRESCO:{
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setAutoPlayAnimations(true)
                        .build();
                setController(controller);
                return;
            }
            case RENDER_GLIDE:{
                Glide.with(context).load(uri).into(this);
                return;
            }
            default:{
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setAutoPlayAnimations(true)
                        .build();
                setController(controller);
            }
        }
    }


    public static GenericDraweeHierarchy getConfig(){
        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(ApplicationLoader.appContext
                        .getResources());
        RoundingParams roundingParams = new RoundingParams();
        // Colors Taken From: https://about.twitter.com/company/brand-assets
        roundingParams.setBorder(0xccd6dd, 2);
//        roundingParams.setCornersRadius(10);
        roundingParams.setOverlayColor(0xe1e8edff);

        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(300)
                .setPlaceholderImage(ApplicationLoader.appContext.
                        getResources().getDrawable( R.drawable.sample))
                .setRoundingParams(roundingParams)
                .build();
        return hierarchy;
    }
}