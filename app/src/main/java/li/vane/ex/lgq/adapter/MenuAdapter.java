package li.vane.ex.lgq.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import li.vane.ex.lgq.R;
import li.vane.ex.lgq.util.ViewHolderUtil;

/**
 * User: YuJian
 * Date: 2015-04-21
 * Time: 17:42
 */
public class MenuAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    private List<String> mItems;
    private int mSelectedPos = 0;

    public MenuAdapter(Context context)
    {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public MenuAdapter(Context context, List<String> items)
    {
        this(context);
        mItems = items;
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

    // lv显示几个item就会调用几次此方法，然后返回一个view对象显示
    // position：位置
    // convertView：如果lv不能显示全部的数据，那么滚动后会把从显示到不显示的View传进来复用
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;
        if (convertView == null)
        {
            view = mInflater.inflate(R.layout.layout_menu_item, null);
        }
        else
        {
            view = convertView;
        }


        TextView tv = ViewHolderUtil.get(view, R.id.label);
        tv.setText(mItems.get(position));

        ImageView iv = ViewHolderUtil.get(view, R.id.iv_selected);
        if (position == mSelectedPos)
        {
            iv.setVisibility(View.VISIBLE);
        }
        else
        {
            iv.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    public int getSelected()
    {
        return mSelectedPos;
    }

    public void setSelected(int pos)
    {
        this.mSelectedPos = pos;
        notifyDataSetChanged();
    }
}
