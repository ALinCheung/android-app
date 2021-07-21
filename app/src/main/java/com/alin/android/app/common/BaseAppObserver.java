package com.alin.android.app.common;

import com.alin.android.app.constant.ReturnCode;
import com.alin.android.core.base.BaseActivity;
import com.alin.android.core.base.BaseObserver;
import com.alin.android.core.model.Result;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description APP基础观察者
 * @Author zhangwl
 * @Date 2021/7/19 10:07
 */
public class BaseAppObserver<T> extends BaseObserver<T> {

    protected BaseAppObserver() {
        super();
    }

    protected BaseAppObserver(BaseActivity activity) {
        super(activity);
    }

    protected BaseAppObserver(BaseActivity activity, Boolean isShowLoading) {
        super(activity, isShowLoading);
    }

    @Override
    public void onAccept(T o, String error) {
        if (StringUtils.isNotBlank(error)) {
            activity.showErrorDialog(error);
        }
        if (o instanceof Result) {
            Result result = (Result) o;
            if (ReturnCode.FAIL == result.getCode()) {
                activity.showErrorDialog(result.getMessage());
            }
        }
    }
}
