package li.vane.ex.lgq;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

import org.jamel.dbf.processor.DbfProcessor;
import org.jamel.dbf.processor.DbfRowMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import li.vane.ex.lgq.bean.LGQ;
import li.vane.ex.lgq.bean.LgqPic;
import li.vane.ex.lgq.bean.PolygonPoint;
import li.vane.ex.lgq.util.AssetUtil;


public class InitActivity extends ActionBarActivity
{
    private Handler mUIHandler;
    String[] mPicFilesPath;
    TextView mLoadingTV;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        mUIHandler = new Handler(Looper.getMainLooper());

        mLoadingTV = (TextView) findViewById(R.id.tv_loading);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                copyOfflineMap();

                copyPics();

                loadDBF();

                mUIHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
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

                        mLoadingTV.setVisibility(View.GONE);
                    }
                });
            }
        }).start();




    }

    private void copyOfflineMap()
    {

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


    private void copyPics()
    {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            Toast.makeText(InitActivity.this, "没有检测到SD卡", Toast.LENGTH_LONG).show();
            return;
        }


        String sdDir = Environment.getExternalStorageDirectory().getPath();
        String dstDir = sdDir + "/Android/data/li.vane.ex.lgq/files/pic";

        Log.d("1", "mapDir:" + dstDir);

        mPicFilesPath = AssetUtil.copyAssets(InitActivity.this, "pic", dstDir);
    }


    private File createFileFromInputStream(InputStream inputStream)
    {

        try
        {
            String sdDir = Environment.getExternalStorageDirectory().getPath();
            String dstDir = sdDir + "/Android/data/li.vane.ex.lgq/files/pic";

            File f = new File(dstDir + "/l.dbf");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0)
            {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }
        catch (IOException e)
        {
            //Logging exception
        }

        return null;
    }
    private void loadDBF()
    {
        LGQ lgq = LGQ.load(LGQ.class, 1);
        if (null != lgq)
        {
            return;
        }


        AssetManager am = getAssets();
        InputStream inputStream;
        try
        {

            inputStream = am.open("lgq.dbf");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        File dbf = createFileFromInputStream(inputStream);

        List<Object> streets = DbfProcessor.loadData(dbf, new DbfRowMapper<Object>()
        {
            @Override
            public Object mapRow(Object[] row)
            {
                LGQ lgq = LGQ.load(LGQ.class, ((Double) row[0]).intValue());

                if (lgq == null)
                {
                    try
                    {
                        lgq = new LGQ();
                        lgq.city = new String((byte[]) row[1], "GBK").trim();
                        lgq.county = new String((byte[]) row[2], "GBK").trim();
                        lgq.name = new String((byte[]) row[3], "GBK").trim();
                        lgq.level = new String((byte[]) row[4], "GBK").trim();
                        lgq.area = (double) (Float) row[5];
                        lgq.crop = new String((byte[]) row[6], "GBK").trim();
                        lgq.planYear = String.valueOf((int) (double) row[7]);
                        lgq.identifiedYear = String.valueOf((int) (double) row[8]);

                        if (lgq.level.isEmpty() || lgq.level.startsWith("后备"))
                        {
                            lgq.status = "待建";
                        }
                        else
                        {
                            lgq.status = "已建";
                        }
                        lgq.beginYear = lgq.planYear + "年2月";
                        lgq.endYear =  "" + (Integer.parseInt(lgq.identifiedYear) - 1)   + "年11月";
                        lgq.save();

                        String sdDir = Environment.getExternalStorageDirectory().getPath();
                        String picDir = sdDir + "/Android/data/li.vane.ex.lgq/files/pic/";


                        for(String f:mPicFilesPath)
                        {
                            if (f.startsWith(String.valueOf(lgq.getId())))
                            {
                                LgqPic pic = new LgqPic(picDir + f, lgq);
                                pic.save();
                            }
                        }

                        Log.d("1111111111111", "load：" + lgq.getId());
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                        return null;
                    }
                }

                LatLng ll = new LatLng((double) (Float) row[11], (double) (Float) row[10]);
                LatLng bdll = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(ll).convert();
                PolygonPoint latlng = new PolygonPoint(bdll.latitude, bdll.longitude, lgq);
                latlng.save();




                return null;
            }
        });
    }

}
