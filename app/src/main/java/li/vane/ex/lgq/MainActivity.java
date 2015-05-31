package li.vane.ex.lgq;

import android.app.AlertDialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.From;
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
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jamel.dbf.processor.DbfProcessor;
import org.jamel.dbf.processor.DbfRowMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import li.vane.ex.lgq.adapter.LGQAdapter;
import li.vane.ex.lgq.adapter.MenuAdapter;
import li.vane.ex.lgq.bean.LGQ;
import li.vane.ex.lgq.bean.PolygonPoint;


public class MainActivity extends ActionBarActivity implements BDLocationListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MODE_NORMAL = 0;
    private static final int MODE_LOCATE = 1;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    public LocationClient mLocationClient = null;
    private Handler mUIHandler = null;
    private LayoutInflater mInflater = null;
    private ListView mCountiesListView;
    private ListView mLevelsListView;
    private MenuAdapter mCountiesAdapter;
    private MenuAdapter mLevelsAdapter;
    private LinearLayout mSearchList;
    private LinearLayout mMainToolbar;
    private LinearLayout mLocateToolbar;
    private Button mBtnSearch;
    private Button mBtnNewPolygon;
    private Button mBtnManualLocate;
    private Button mBtnLocateCancel;
    private Button mBtnLocateComplete;

    private int mMode = MODE_NORMAL;

    private List<Polygon> mLgqPolygons = new ArrayList<Polygon>();

    private static final LatLng CENTER = new LatLng(29.912988, 121.478965);
    private String[] mCounties = {"全部", "海曙区", "江东区", "江北区", "北仑区", "镇海区", "鄞州区", "象山县", "宁海县", "余姚市", "慈溪市", "奉化市"};
    private String[] mLevels = {"全部", "省级", "市级", "县级", "后备"};


    private LGQ mNewLgq;
    private Polygon mNewLgqPolygon;
    private ArrayList<LatLng> mNewLgqPoints;


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

                if (mMode == MODE_LOCATE)
                {
                    LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                }
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

        mInflater = getLayoutInflater();
        setContentView(R.layout.activity_main);

        mUIHandler = new Handler(getMainLooper());
        Log.d(TAG, "------onCreate------");
        loadDBF();
        mMapView = (MapView) findViewById(R.id.map);
        mBaiduMap = mMapView.getMap();

        initWidget();

        initMap();

        initMenu();

        doSearchAndAddLgq();
    }


    public void initWidget()
    {
        mMainToolbar = (LinearLayout) findViewById(R.id.ll_main_toolbar);

        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mSearchList.getVisibility() != View.VISIBLE)
                {
                    mSearchList.setVisibility(View.VISIBLE);
                }
                else
                {
                    mSearchList.setVisibility(View.GONE);
                }
            }
        });

        mBtnNewPolygon = (Button) findViewById(R.id.btn_new_polygon);
        mBtnNewPolygon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final LGQ lgq = new LGQ();
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                View view = mInflater.inflate(R.layout.layout_dialog_create, null);
                dialogBuilder.setView(view);

                final AlertDialog alertDialog = dialogBuilder.create();
                final EditText tvName = (EditText) view.findViewById(R.id.tv_name);
                final EditText tvCity = (EditText) view.findViewById(R.id.tv_city);
                final EditText tvCounty = (EditText) view.findViewById(R.id.tv_county);
                final EditText tvCrop = (EditText) view.findViewById(R.id.tv_crop);
                final EditText tvArea = (EditText) view.findViewById(R.id.tv_area);
                final EditText tvLevel = (EditText) view.findViewById(R.id.tv_level);
                final EditText tvPlanYear = (EditText) view.findViewById(R.id.tv_plan_year);
                final EditText tvIdentifiedYear = (EditText) view.findViewById(R.id.tv_identified_year);
                final Button btnEdit = (Button) view.findViewById(R.id.btn_edit);


                btnEdit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        lgq.name = tvName.getText().toString().trim();
                        lgq.city = tvCity.getText().toString().trim();
                        lgq.county = tvCounty.getText().toString().trim();
                        lgq.crop = tvCrop.getText().toString().trim();
                        try
                        {
                            lgq.area = Double.parseDouble(tvArea.getText().toString().trim());
                        }
                        catch (Exception e)
                        {

                        }

                        lgq.level = tvLevel.getText().toString().trim();
                        lgq.planYear = tvPlanYear.getText().toString().trim();
                        lgq.identifiedYear = tvIdentifiedYear.getText().toString().trim();

                        if (lgq.name.isEmpty() || lgq.city.isEmpty() || lgq.county.isEmpty())
                        {
                            Toast.makeText(MainActivity.this, "请输入粮功区名称、城市名称、县市区名称", Toast.LENGTH_LONG).show();
                            return;
                        }

                        enterLocateMode();

                        mNewLgq = lgq;
                        mNewLgqPoints = new ArrayList<LatLng>();

                        alertDialog.dismiss();

                    }
                });

                alertDialog.show();
            }
        });


        mBtnManualLocate = (Button) findViewById(R.id.btn_locate_manual);
        mBtnManualLocate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                mLocationClient.requestLocation();
                MyLocationData loc = mBaiduMap.getLocationData();
                if (null != loc)
                {
                    if (null == mNewLgqPoints)
                    {
                        mNewLgqPoints = new ArrayList<LatLng>();
                    }
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(loc.latitude, loc.longitude)));

//                    double lat = loc.latitude;
//                    double lng = loc.longitude;

                    double lat = loc.latitude + 0.001*mNewLgqPoints.size();
                    double lng = loc.longitude + 0.001*(mNewLgqPoints.size()%2);


                    addMarker(new LatLng(lat, lng), String.valueOf(mNewLgqPoints.size() + 1));
                    mNewLgqPoints.add(new LatLng(lat, lng));
                    if (mNewLgqPoints.size() > 2)
                    {
                        if (mNewLgqPolygon != null)
                        {
                            mNewLgqPolygon.remove();
                            mNewLgqPolygon = null;
                        }

                        OverlayOptions polygonOption = new PolygonOptions()
                                .points(mNewLgqPoints)
                                .stroke(new Stroke(3, 0xEE00FF00))
                                .fillColor(0xEEFFFF00);

                        mNewLgqPolygon = (Polygon) mBaiduMap.addOverlay(polygonOption);

                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "还未获取到GPS信号，请到空旷地区稍后再试", Toast.LENGTH_LONG).show();
                }
            }
        });

        mBtnLocateCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnLocateCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                exitLocateMode();
                mNewLgq = null;
                mNewLgqPoints= null;
            }
        });

        mBtnLocateComplete = (Button) findViewById(R.id.btn_complete);
        mBtnLocateComplete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                exitLocateMode();
                if (null != mNewLgq && null != mNewLgqPoints)
                {
                    mNewLgq.save();

                    for(LatLng ll:mNewLgqPoints)
                    {
                        PolygonPoint latlng = new PolygonPoint(ll.latitude, ll.longitude, mNewLgq);
                        latlng.save();
                    }

                }
            }
        });

        mLocateToolbar = (LinearLayout) findViewById(R.id.ll_locate);
        mLocateToolbar.setVisibility(View.GONE);
    }

    private void doSearchAndAddLgq()
    {
        List<LGQ> lgqs = searchLgq(mCountiesAdapter.getSelected(), mLevelsAdapter.getSelected());
        Log.d(TAG, "searchLgq:" + lgqs.size());
        mBaiduMap.clear();

        if (lgqs != null && lgqs.size() > 0)
        {
            addLgqPolygons(lgqs);

            LGQ lgq = lgqs.get(0);
            List<PolygonPoint> points = lgq.polygon();
            if (null != points)
            {
                int size = points.size();
                double lat = 0;
                double lon = 0;
                for (int i = 0; i < size; i++)
                {
                    lat += points.get(i).lat;
                    lon += points.get(i).lng;
                }

                LatLng ll = new LatLng(lat / size, lon / size);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
        }
        else
        {
            Toast.makeText(MainActivity.this, "无相关搜索结果", Toast.LENGTH_LONG).show();
        }
    }

    private List<LGQ> searchLgq(int county, int level)
    {
        From query = new Select().from(LGQ.class);
        if (0 != county)
        {
            Log.d(TAG, "filter county by " + mCounties[county]);
            String clause = "County LIKE '%" + mCounties[county] + "%'";
            query = query.where(clause);

        }

        if (0 != level)
        {
            Log.d(TAG, "filter level by " + mLevels[level]);

            String clause = "Level LIKE '%" + mLevels[level] + "%'";
            query = query.where(clause);
        }

        return query.orderBy("id ASC").execute();
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
        option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
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

//                Log.d(TAG, "------ onMapLoaded------");
//                new Thread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        addLgqPolygons();
//                    }
//                }).start();
//                Log.d(TAG, "------ onMapLoaded end------");
            }
        });


        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(LatLng latLng)
            {
                for (final Polygon p : mLgqPolygons)
                {
                    boolean b = SpatialRelationUtil.isPolygonContainsPoint(p.getPoints(), latLng);
                    if (b)
                    {
                        Bundle bundle = p.getExtraInfo();
                        Log.d(TAG, "onPolygonLongClick:" + bundle.getString("id"));

                        long id = Long.parseLong(bundle.getString("id"));
                        final LGQ lgq = LGQ.load(LGQ.class, id);
                        if (null == lgq)
                        {
                            continue;
                        }


                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        View view = mInflater.inflate(R.layout.layout_dialog_detail, null);
                        dialogBuilder.setView(view);

                        final AlertDialog alertDialog = dialogBuilder.create();

                        final EditText tvName = (EditText) view.findViewById(R.id.tv_name);
                        tvName.setText(lgq.name);

                        final EditText tvCity = (EditText) view.findViewById(R.id.tv_city);
                        tvCity.setText(lgq.city);

                        final EditText tvCounty = (EditText) view.findViewById(R.id.tv_county);
                        tvCounty.setText(lgq.county);

                        final EditText tvCrop = (EditText) view.findViewById(R.id.tv_crop);
                        tvCrop.setText(lgq.crop);

                        final EditText tvArea = (EditText) view.findViewById(R.id.tv_area);
                        tvArea.setText(String.valueOf((int) (lgq.area)));

                        final EditText tvLevel = (EditText) view.findViewById(R.id.tv_level);
                        tvLevel.setText(lgq.level);

                        final EditText tvPlanYear = (EditText) view.findViewById(R.id.tv_plan_year);
                        tvPlanYear.setText(lgq.planYear);

                        final EditText tvIdentifiedYear = (EditText) view.findViewById(R.id.tv_identified_year);
                        tvIdentifiedYear.setText(lgq.identifiedYear);

                        final Button btnEdit = (Button) view.findViewById(R.id.btn_edit);


                        btnEdit.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                boolean enable = false;
                                if (btnEdit.getText().equals("编辑"))
                                {
                                    enable = true;
                                    btnEdit.setText("保存");
                                }
                                else
                                {
                                    enable = false;
                                    btnEdit.setText("编辑");

                                    lgq.name = tvName.getText().toString().trim();
                                    lgq.city = tvCity.getText().toString().trim();
                                    lgq.county = tvCounty.getText().toString().trim();
                                    lgq.crop = tvCrop.getText().toString().trim();
                                    lgq.area = Double.parseDouble(tvArea.getText().toString().trim());
                                    lgq.level = tvLevel.getText().toString().trim();
                                    lgq.planYear = tvPlanYear.getText().toString().trim();
                                    lgq.identifiedYear = tvIdentifiedYear.getText().toString().trim();
                                    lgq.save();

                                    alertDialog.dismiss();
                                }

                                tvName.setEnabled(enable);
                                tvCity.setEnabled(enable);
                                tvCounty.setEnabled(enable);
                                tvCrop.setEnabled(enable);
                                tvArea.setEnabled(enable);
                                tvLevel.setEnabled(enable);
                                tvPlanYear.setEnabled(enable);
                                tvIdentifiedYear.setEnabled(enable);
                            }
                        });

                        alertDialog.show();
                        break;
                    }
                }
            }
        });

    }

    private void initMenu()
    {

        mSearchList = (LinearLayout) findViewById(R.id.ll_search_list);
        ArrayList<String> data = new ArrayList<String>(Arrays.asList(mCounties));
        View header = getLayoutInflater().inflate(R.layout.layout_menu_list_header, null);
        ((TextView) header.findViewById(R.id.title)).setText("所在地区");
        mCountiesAdapter = new MenuAdapter(this, data);
        mCountiesListView = (ListView) findViewById(R.id.lv_counties);
        mCountiesListView.addHeaderView(header);
        mCountiesListView.setAdapter(mCountiesAdapter);
        mCountiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                mCountiesAdapter.setSelected(position - mCountiesListView.getHeaderViewsCount());
                doSearchAndAddLgq();
            }
        });

        ArrayList<String> levels = new ArrayList<String>(Arrays.asList(mLevels));
        View levelHeader = getLayoutInflater().inflate(R.layout.layout_menu_list_header, null);
        ((TextView) levelHeader.findViewById(R.id.title)).setText("粮功区级别");
        mLevelsAdapter = new MenuAdapter(this, levels);
        mLevelsListView = (ListView) findViewById(R.id.lv_levels);
        mLevelsListView.addHeaderView(levelHeader);
        mLevelsListView.setAdapter(mLevelsAdapter);

        mLevelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                mLevelsAdapter.setSelected(position - mLevelsListView.getHeaderViewsCount());
                doSearchAndAddLgq();
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
        {
            return;
        }

        Log.d(TAG, "onReceiveLocation:" + location);

    }

    private void addMarker(LatLng ll, String text)
    {
        View view = mInflater.inflate(R.layout.layout_text_marker, null);
        TextView t = (TextView) view.findViewById(R.id.label);
        t.setText(text);

        BitmapDescriptor bdA = BitmapDescriptorFactory.fromView(view);
        OverlayOptions options = new MarkerOptions()
                .position(ll)  //设置marker的位置
                .icon(bdA)  //设置marker图标
                .draggable(true);  //设置手势拖拽

        mBaiduMap.addOverlay(options);
    }

    /**
     * @param lgqs
     */
    private void addLgqPolygons(List<LGQ> lgqs)
    {
        if (null == lgqs || lgqs.size() == 0)
        {
            return;
        }
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.registerTypeAdapter(LGQ.class, new LGQAdapter()).create();

        for (LGQ lgq : lgqs)
        {
            List<LatLng> pts = new ArrayList<LatLng>();
            List<PolygonPoint> points = lgq.polygon();
            int numPoints = points.size();

            for (int i = 0; i < numPoints; i++)
            {

                //Log.d(TAG, "Id:" + points.get(i).getId());
                LatLng ll = new LatLng(points.get(i).lat, points.get(i).lng);
                pts.add(ll);

                //addMarker(ll, i + 1 + "");
            }

            OverlayOptions polygonOption = new PolygonOptions()
                    .points(pts)
                    .stroke(new Stroke(3, 0xEE00FF00))
                    .fillColor(0xEEFFFF00);

            Polygon p = (Polygon) mBaiduMap.addOverlay(polygonOption);

            Bundle bundle = new Bundle();
            bundle.putString("id", String.valueOf(lgq.getId()));
            p.setExtraInfo(bundle);

            mLgqPolygons.add(p);
        }

    }


    public List<LGQ> getAll()
    {

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
                        lgq.city = new String((byte[]) row[1], "GBK").trim();
                        lgq.county = new String((byte[]) row[2], "GBK").trim();
                        lgq.name = new String((byte[]) row[3], "GBK").trim();
                        lgq.level = new String((byte[]) row[4], "GBK").trim();
                        lgq.area = (double) (Float) row[5];
                        lgq.crop = new String((byte[]) row[6], "GBK").trim();
                        lgq.planYear = String.valueOf((int) (double) row[7]);
                        lgq.identifiedYear = String.valueOf((int) (double) row[8]);
                        lgq.save();
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


    private void enterLocateMode()
    {
        mMode = MODE_LOCATE;
        mLocateToolbar.setVisibility(View.VISIBLE);
        mMainToolbar.setVisibility(View.GONE);
        mSearchList.setVisibility(View.GONE);
    }

    private void exitLocateMode()
    {
        mMode = MODE_NORMAL;
        mLocateToolbar.setVisibility(View.GONE);
        mMainToolbar.setVisibility(View.VISIBLE);

        mBaiduMap.clear();
    }
}
