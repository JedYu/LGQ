package li.vane.ex.lgq;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.media.Image;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import li.vane.ex.lgq.util.AssetUtil;


public class InitActivity extends ActionBarActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        copyOfflineMap();

        MKOfflineMap OfflineMap = new MKOfflineMap();
        OfflineMap.init(new MKOfflineMapListener()
        {
            @Override
            public void onGetOfflineMapState(int i, int i1)
            {
                Log.d("111111111111", "onGetOfflineMapState" + i + "," + i1);
            }
        });
        OfflineMap.importOfflineData();

        ImageView enter = (ImageView) findViewById(R.id.btn_lgq_enter);
        enter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(InitActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void copyOfflineMap()
    {
        AssetManager am = getAssets();
        InputStream inputStream;

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            Toast.makeText(InitActivity.this, "没有检测到SD卡，离线地图无法生效", Toast.LENGTH_LONG).show();
            return;
        }


        String sdDir = Environment.getExternalStorageDirectory().getPath();
        String mapDir = sdDir + "/Android/data/li.vane.ex.lgq/files/BaiduMapSDK/vmp/h/";

        Log.d("1", "mapDir:" + mapDir);

        AssetUtil.copyAssets(InitActivity.this, "BaiduMapSDK", mapDir);
    }


}
