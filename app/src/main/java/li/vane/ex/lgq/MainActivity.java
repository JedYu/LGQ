package li.vane.ex.lgq;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

import org.jamel.dbf.processor.DbfProcessor;
import org.jamel.dbf.processor.DbfRowMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import li.vane.ex.lgq.bean.LGQ;
import li.vane.ex.lgq.bean.PolygonPoint;


public class MainActivity extends ActionBarActivity implements BDLocationListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    public LocationClient mLocationClient = null;
    private Handler mUIHandler = null;
    private LayoutInflater mInflater = null;

    private List<Polygon> mLgqPolygons = new ArrayList<Polygon>();


    private static final LatLng CENTER = new LatLng(29.912988, 121.478965);


    private BaiduMap.OnMapStatusChangeListener mMapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener()
    {

        @Override
        public void onMapStatusChangeStart(MapStatus arg0)
        {
        }

        @Override
        public void onMapStatusChangeFinish(MapStatus arg0)
        {
        }

        @Override
        public void onMapStatusChange(MapStatus arg0)
        {
        }
    };


    private BDLocationListener mLocationListener = new BDLocationListener()
    {

        @Override
        public void onReceiveLocation(BDLocation location)
        {
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation
                    || location.getLocType() == BDLocation.TypeOffLineLocation)
            {
                Log.d(TAG, "getLatitude:" + location.getLatitude() + ", getLongitude:" + location.getLongitude());

                MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();

                mBaiduMap.setMyLocationData(locData);


//                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
//                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
//                mBaiduMap.animateMapStatus(u);
            }
            else
            {
                Log.w(TAG, "getLocType:" + location.getLocType());
            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());

        mInflater = getLayoutInflater();
        setContentView(R.layout.activity_main);

        mUIHandler = new Handler(getMainLooper());
        Log.d(TAG, "------onCreate------");
        loadDBF();
        mMapView = (MapView) findViewById(R.id.map);
        mBaiduMap = mMapView.getMap();

        initMap();

        Log.d(TAG, "------ onCreate end------");
    }


    private void initMap()
    {
        mMapView.showZoomControls(false);
        mMapView.setKeepScreenOn(true);
        mBaiduMap.setMyLocationEnabled(true);
        // 设置中心点
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(30.419, 120.300)));

        // 设置默认显示级别
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));

        mBaiduMap.setOnMapStatusChangeListener(mMapStatusChangeListener);

        // 定位初始化
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(mLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(60000);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(CENTER);
        mBaiduMap.animateMapStatus(u);



        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback()
        {
            @Override
            public void onMapLoaded()
            {

                Log.d(TAG, "------ onMapLoaded------");
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        addLgqPolygons();
                    }
                }).start();
                Log.d(TAG, "------ onMapLoaded end------");


            }
        });
    }
    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public void onReceiveLocation(BDLocation location)
    {
        if (location == null)
            return ;

        Log.d(TAG, "onReceiveLocation:" + location);

    }

    private void addLgqPolygons()
    {
        List<LGQ> lgqs = getAll();
        Log.d(TAG, "city:" + lgqs.get(0).city);
        Log.d(TAG, "lat:" + lgqs.get(0).polygon().get(0).lat);
        Log.d(TAG, "lng:" + lgqs.get(0).polygon().get(0).lng);
        Log.d(TAG, "all:" + lgqs);


        for (LGQ lgq:lgqs)
        {
            List<LatLng> pts = new ArrayList<LatLng>();
            List<PolygonPoint> points = lgq.polygon();
            int numPoints = points.size();

            for (int i = 0; i < numPoints; i=i+3)
            {

                Log.d(TAG, "Id:" + points.get(i).getId());
                LatLng ll = new LatLng(points.get(i).lat, points.get(i).lng);
                pts.add(ll);

                View view = mInflater.inflate(R.layout.layout_text_marker, null);
                TextView t = (TextView) view.findViewById(R.id.label);
                t.setText(i + 1 + "");

                BitmapDescriptor bdA = BitmapDescriptorFactory.fromView(view);
                OverlayOptions options = new MarkerOptions()
                        .position(ll)  //设置marker的位置
                        .icon(bdA)  //设置marker图标
                        .draggable(true);  //设置手势拖拽

                mBaiduMap.addOverlay(options);
            }



            OverlayOptions polygonOption = new PolygonOptions()
                    .points(pts)
                    .stroke(new Stroke(5, 0xAA00FF00))
                    .fillColor(0xAAFFFF00);

            mBaiduMap.addOverlay(polygonOption);

        }

    }


    public static List<LGQ> getAll() {
        return new Select()
                .from(LGQ.class)
                .orderBy("id ASC")
                .execute();
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
                        lgq.city = new String((byte[]) row[1], "GBK");
                        lgq.county = new String((byte[]) row[2], "GBK");
                        lgq.name = new String((byte[]) row[3], "GBK");
                        lgq.level = new String((byte[]) row[4], "GBK");
                        lgq.area = (double) (Float) row[5];
                        lgq.crop = new String((byte[]) row[6], "GBK");
                        lgq.planYear = String.valueOf((double) row[7]);
                        lgq.identifiedYear = String.valueOf((double) row[8]);
                        lgq.save();
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                        return null;
                    }
                }

                PolygonPoint latlng = new PolygonPoint((double) (Float)  row[11], (double) (Float)  row[10], lgq);
                latlng.save();

                return null;
            }
        });
    }

    private File createFileFromInputStream(InputStream inputStream)
    {

        try
        {
            File f = new File("/mnt/sdcard/l.dbf");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mMapView.onDestroy();

        mLocationClient.unRegisterLocationListener(mLocationListener);
        mLocationClient.stop();

    }
}
