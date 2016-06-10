package ng.com.nhub.paygis.lib;

import android.graphics.Bitmap;

import java.util.Locale;

/**
 * Created by retnan on 5/20/16.
 */
public class FileUtil {

    public static Bitmap.CompressFormat getCompressFormat(String url) {
        String fileStr = url.toLowerCase(Locale.ENGLISH);
        if (fileStr.endsWith(".jpg")) {
            return Bitmap.CompressFormat.JPEG;
        } else if (fileStr.endsWith(".png")) {
            return Bitmap.CompressFormat.PNG;
        } else if(fileStr.endsWith(".gif")){
            return Bitmap.CompressFormat.WEBP;
        }
        return Bitmap.CompressFormat.JPEG;
    }
}
