package li.vane.ex.lgq.util;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by 健 on 2014/11/24.
 */
public class ViewHolderUtil
{
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id)
    {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null)
        {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null)
        {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
