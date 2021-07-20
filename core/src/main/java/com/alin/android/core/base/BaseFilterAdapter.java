package com.alin.android.core.base;

import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 带过滤的基础适配器
 * @Author zhangwl
 * @Date 2021/7/20 15:24
 */
public abstract class BaseFilterAdapter<T> extends BaseCoreAdapter<T> implements Filterable {

    private List<T> mFilterData = new ArrayList<>(0);

    public BaseFilterAdapter() {
        super();
    }

    public BaseFilterAdapter(List<T> mData, int mLayoutRes) {
        super(mData, mLayoutRes);
    }

    public BaseFilterAdapter(List<T> mData, int mLayoutRes, Context mContext) {
        super(mData, mLayoutRes, mContext);
    }

    @Override
    public int getCount() {
        return mFilterData != null ? mFilterData.size() : 0;
    }

    @Override
    public T getItem(int i) {
        return mFilterData.get(i);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if (StringUtils.isBlank(charSequence)){
                    filterResults.values = mData;
                    filterResults.count = mData.size();
                }else{
                    List<T> list = new ArrayList<>(0);
                    for (T t : mData) {
                        if (filterCharSequence(t, charSequence)){
                            list.add(t);
                        }
                    }
                    filterResults.values = list;
                    filterResults.count = list.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults != null && filterResults.count > 0) {
                    mFilterData.clear();
                    mFilterData.addAll((List<T>) filterResults.values);
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    /**
     * 过滤字符条件
     * @param o
     * @param charSequence
     * @return
     */
    protected abstract boolean filterCharSequence(T o, CharSequence charSequence);

}
