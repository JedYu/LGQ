package li.vane.ex.lgq;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.activeandroid.query.Select;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.Polygon;
import com.amap.api.maps2d.model.PolygonOptions;

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
import li.vane.ex.lgq.bean.PolygonPoint;


public class MainActivity extends ActionBarActivity implements LocationSource, AMapLocationListener, AMap.OnMarkerClickListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private AMap mAMap;
    private MapView mMapView;
    private LocationSource.OnLocationChangedListener mListener;

    private LocationManagerProxy mLocationManagerProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadDBF();
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mAMap = mMapView.getMap();

        mAMap.setLocationSource(this);
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);
        mAMap.setMyLocationEnabled(true);

        mAMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener()
        {
            @Override
            public void onMyLocationChange(Location location)
            {
                Log.d(TAG, "onMyLocationChange:" + location);
            }
        });

        List<LGQ> lgqs = getAll();
        Log.d(TAG, "city:" + lgqs.get(0).city);
        Log.d(TAG, "all:" + lgqs);


        for (LGQ lgq:lgqs)
        {
            PolygonOptions options = new PolygonOptions();

            List<PolygonPoint> points = lgq.polygon();
            int numPoints = points.size();
            // 绘制一个椭圆
            for (int i = 0; i <= numPoints; i++)
            {
                int index = i;
                if (index == numPoints)
                {
                    index = 0;
                }
                options.add(new LatLng(points.get(index).lat, points.get(index).lng));
            }

            Polygon polygon = mAMap.addPolygon(options.strokeWidth(1f)
                    .strokeColor(Color.parseColor("#FFCCCCCC")).fillColor(Color.parseColor("#9900FF00")));

        }

        mAMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        Log.d(TAG, "你点击的是" + marker.getTitle());
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        if (mListener != null) {
            mListener.onLocationChanged(aLocation);// 显示系统小蓝点
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener)
    {
        mListener = listener;
        if (mLocationManagerProxy == null)
        {
            mLocationManagerProxy = LocationManagerProxy.getInstance(this);
            /*
			 * mAMapLocManager.setGpsEnable(false);//
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true
			 */
            // Location API定位采用GPS和网络混合定位方式，时间最短是2000毫秒
            mLocationManagerProxy.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 2000, 10, this);
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate()
    {
        mListener = null;
        if (mLocationManagerProxy != null)
        {
            mLocationManagerProxy.removeUpdates(this);
            mLocationManagerProxy.destroy();
        }
        mLocationManagerProxy = null;

    }

    public static List<LGQ> getAll() {
        return new Select()
                .from(LGQ.class)
                .execute();
    }

    private void loadDBF()
    {
        LGQ lgq = LGQ.load(LGQ.class, 0);
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

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mMapView.onDestroy();
    }
}
