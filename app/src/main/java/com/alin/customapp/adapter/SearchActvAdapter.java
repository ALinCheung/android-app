package com.alin.customapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.alin.customapp.R;
import com.alin.customapp.model.App;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-01 10:02
 **/
public class SearchActvAdapter extends BaseAdapter implements Filterable{

    private Context mContext;
    private List<App> mData;

    public SearchActvAdapter() {
    }

    public SearchActvAdapter(Context context, List<App> data) {
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
        TextView tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.support_simple_spinner_dropdown_item, viewGroup, false);
        tv.setText(mData.get(i).getName());
        return tv;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if (charSequence == null || "".equals(charSequence)){
                    filterResults.values = mData;
                    filterResults.count = mData.size();
                }else{
                    List<App> apps = new ArrayList<>(0);
                    for (App app : mData) {
                        if (app.getName().contains(charSequence.toString().trim())){
                            apps.add(app);
                        }
                    }
                    filterResults.values = apps;
                    filterResults.count = apps.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults != null && filterResults.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                App app = (App) resultValue;
                return super.convertResultToString(app.getName());
            }
        };
    }
}
