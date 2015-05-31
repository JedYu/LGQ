package li.vane.ex.lgq;

import android.app.Application;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.baidu.mapapi.SDKInitializer;

public class APP extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        ActiveAndroid.initialize(this);
        Log.d(APP.class.getCanonicalName(), "I am created now");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }
}
