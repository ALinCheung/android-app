package com.alin.android.app.adapter;

import com.alin.android.app.model.App;
import com.alin.android.core.base.BaseCoreAdapter;
import com.alin.android.app.R;

import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-01 10:02
 **/
public class MainGvAdapter extends BaseCoreAdapter<App> {

    public MainGvAdapter(List<App> mData, int mLayoutRes) {
        super(mData, mLayoutRes);
    }

    @Override
    public void bindView(BaseCoreAdapter.ViewHolder holder, App app) {
        holder.setText(R.id.main_gl_item_tv, app.getName());
    }
}
