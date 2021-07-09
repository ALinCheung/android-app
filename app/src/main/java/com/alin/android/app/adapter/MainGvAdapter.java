package com.alin.android.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.alin.android.app.model.App;
import com.alin.app.R;

import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-01 10:02
 **/
public class MainGvAdapter extends BaseAdapter{

    private Context mContext;
    private List<App> mData;

    public MainGvAdapter() {
    }

    public MainGvAdapter(Context context, List<App> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null){
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.main_gridview_item, viewGroup, false);
            holder.tv = (TextView) view.findViewById(R.id.main_gl_item_tv);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        holder.tv.setText(mData.get(i).getName());
        return view;
    }

    private class ViewHolder{
        TextView tv;
    }
}
