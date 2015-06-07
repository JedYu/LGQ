package li.vane.ex.lgq.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import li.vane.ex.lgq.R;
import li.vane.ex.lgq.bean.LgqPic;
import li.vane.ex.lgq.util.ViewHolderUtil;

/**
 * User: YuJian
 * Date: 2015-04-21
 * Time: 17:42
 */
public class PicsAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    private List<LgqPic> mItems;

    public PicsAdapter(Context context)
    {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public PicsAdapter(Context context, List<LgqPic> items)
    {
        this(context);
        mItems = items;
    }

    public void newPic(LgqPic pic)
    {
        mItems.add(pic);
        notifyDataSetChanged();
    }
    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mItems.get(position);
    }

    // itemId
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;
        if (convertView == null)
        {
            view = mInflater.inflate(R.layout.layout_pic_item, null);
        }
        else
        {
            view = convertView;
        }

        LgqPic pic = mItems.get(position);

        ImageView iv = ViewHolderUtil.get(view, R.id.iv_pic);
        File file = new File(pic.path);
        if (file.exists())
        {
            Bitmap bm = BitmapFactory.decodeFile(pic.path);
            //将图片显示到ImageView中
            iv.setImageBitmap(bm);
        }

        return view;
    }

}
