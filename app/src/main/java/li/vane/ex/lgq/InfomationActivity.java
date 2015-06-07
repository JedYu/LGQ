package li.vane.ex.lgq;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;

import li.vane.ex.lgq.adapter.PicsAdapter;
import li.vane.ex.lgq.bean.LGQ;
import li.vane.ex.lgq.bean.LgqPic;


public class InfomationActivity extends ActionBarActivity
{
    LGQ mLGQ;
    private ListView mPicsView;
    private PicsAdapter mPicsAdapter;
    private EditText mEditName;
    private EditText mEditCity;
    private EditText mEditCounty;
    private EditText mEditCrop;
    private EditText mEditArea;
    private EditText mEditLevel;
    private EditText mEditStatus;
    private EditText mEditPlanYear;
    private EditText mEditBeginYear;
    private EditText mEditEndYear;
    private EditText mEditIdentifiedYear;
    private Button mBtnEdit;
    private Button mBtnTakePic;
    private Button mBtnBack;
    private String mTempPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomation);

        long id = getIntent().getLongExtra("id", -1);

        mLGQ = LGQ.load(LGQ.class, id);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        mPicsView = (ListView) findViewById(R.id.lv_pics);
        mPicsAdapter = new PicsAdapter(InfomationActivity.this, mLGQ.pics());
        mPicsView.setAdapter(mPicsAdapter);

        mEditName = (EditText) findViewById(R.id.et_name);
        mEditName.setText(mLGQ.name);

        mEditCity = (EditText) findViewById(R.id.et_city);
        mEditCity.setText(mLGQ.city);

        mEditCounty = (EditText) findViewById(R.id.et_county);
        mEditCounty.setText(mLGQ.county);

        mEditCrop = (EditText) findViewById(R.id.et_crop);
        mEditCrop.setText(mLGQ.crop);

        mEditArea = (EditText) findViewById(R.id.et_area);
        mEditArea.setText(String.valueOf(mLGQ.area));

        mEditLevel = (EditText) findViewById(R.id.et_level);
        mEditLevel.setText(mLGQ.level);

        mEditStatus = (EditText) findViewById(R.id.et_status);
        mEditStatus.setText(mLGQ.status);

        mEditPlanYear = (EditText) findViewById(R.id.et_plan_year);
        mEditPlanYear.setText(mLGQ.planYear);
        mEditBeginYear = (EditText) findViewById(R.id.et_begin_year);
        mEditBeginYear.setText(mLGQ.beginYear);
        mEditEndYear = (EditText) findViewById(R.id.et_end_year);
        mEditEndYear.setText(mLGQ.endYear);
        mEditIdentifiedYear = (EditText) findViewById(R.id.et_identified_year);
        mEditIdentifiedYear.setText(mLGQ.identifiedYear);

        mBtnTakePic  = (Button) findViewById(R.id.btn_take_pic);
        mBtnTakePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent();
                // 指定开启系统相机的Action
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                String sdDir = Environment.getExternalStorageDirectory().getPath();
                String picDir = sdDir + "/Android/data/li.vane.ex.lgq/files/pic/";
                // 根据文件地址创建文件
                File file = new File(picDir, "" + mLGQ.getId() + "-" + System.currentTimeMillis() + ".jpg");
                if (file.exists()) {
                    file.delete();
                }
                mTempPath = file.getAbsolutePath();
                // 把文件地址转换成Uri格式
                Uri uri = Uri.fromFile(file);
                // 设置系统相机拍摄照片完成后图片文件的存放地址
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, 0);
            }
        });
        mBtnEdit  = (Button) findViewById(R.id.btn_edit);
        mBtnEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                boolean editable = false;
                if (mBtnEdit.getText().equals("编辑"))
                {
                    mBtnEdit.setText("保存");
                    editable = true;
                }
                else
                {
                    mBtnEdit.setText("编辑");
                    mLGQ.name = mEditName.getText().toString().trim();
                    mLGQ.city = mEditCity.getText().toString().trim();
                    mLGQ.county = mEditCounty.getText().toString().trim();
                    mLGQ.crop = mEditCrop.getText().toString().trim();
                    try
                    {
                        mLGQ.area = Double.parseDouble(mEditArea.getText().toString().trim());
                    }
                    catch (Exception e)
                    {

                    }

                    mLGQ.level = mEditLevel.getText().toString().trim();
                    mLGQ.status = mEditStatus.getText().toString().trim();
                    mLGQ.planYear = mEditPlanYear.getText().toString().trim();
                    mLGQ.beginYear = mEditBeginYear.getText().toString().trim();
                    mLGQ.endYear = mEditEndYear.getText().toString().trim();
                    mLGQ.identifiedYear = mEditIdentifiedYear.getText().toString().trim();
                    mLGQ.save();
                }

                mEditName.setEnabled(editable);
                mEditCity.setEnabled(editable);
                mEditCounty.setEnabled(editable);
                mEditCrop.setEnabled(editable);
                mEditArea.setEnabled(editable);
                mEditLevel.setEnabled(editable);
                mEditStatus.setEnabled(editable);
                mEditPlanYear.setEnabled(editable);
                mEditBeginYear.setEnabled(editable);
                mEditEndYear.setEnabled(editable);
                mEditIdentifiedYear.setEnabled(editable);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            LgqPic pic = new LgqPic(mTempPath, mLGQ);
            pic.save();
            mPicsAdapter.newPic(pic);

        }
    }
}
