package com.alin.android.app.adapter;

import android.content.Context;
import com.alin.android.app.model.App;
import com.alin.android.core.base.BaseFilterAdapter;

import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-01 10:02
 **/
public class SearchActvAdapter extends BaseFilterAdapter<App> {

    public SearchActvAdapter(List<App> mData, int mLayoutRes, Context mContext) {
        super(mData, mLayoutRes, mContext);
    }

    @Override
    public void bindView(ViewHolder holder, App obj) {
        holder.setText(android.R.id.text1, obj.getName());
    }

    @Override
    protected boolean filterCharSequence(App o, CharSequence charSequence) {
        return o.getName().contains(charSequence.toString().trim());
    }

    @Override
    protected String getResultString(App o) {
        return o.getName();
    }
}
