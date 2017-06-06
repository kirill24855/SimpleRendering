package pro.shpin.kirill.simplerendering;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Created by wiish on 10/16/2016.
 */

public class GLApplication extends Application {
    public static Context context;
    public static Resources resources;

    public void onCreate() {
        super.onCreate();
        GLApplication.context = getApplicationContext();
        GLApplication.resources = getResources();
    }
}
