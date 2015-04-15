package li.vane.ex.lgq;

import android.app.Application;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

public class APP extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        Log.d(APP.class.getCanonicalName(), "I am created now");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }
}
