package com.alin.android.core.base;

import android.net.ParseException;
import androidx.annotation.NonNull;
import com.google.gson.JsonParseException;
import io.reactivex.observers.ResourceObserver;
import org.json.JSONException;
import retrofit2.HttpException;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * @Description 基础观察者
 * @Author zhangwl
 * @Date 2021/7/9 16:28
 */
public abstract class BaseObserver<T> extends ResourceObserver<T> {

    protected BaseObserver() {
    }

    @Override
    public void onNext(@NonNull T value) {
        onAccept(value, "");
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            switch (httpException.code()) {
                case UNAUTHORIZED:
                    onAccept(null, "登录验证已过期");
                    break;
                case INTERNAL_SERVER_ERROR:
                    onAccept(null, "服务器错误");
                    break;
                case FORBIDDEN:
                case NOT_FOUND:
                    onAccept(null, "无效的请求");
                    break;
                case REQUEST_TIMEOUT:
                case GATEWAY_TIMEOUT:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                default:
                    onAccept(null, httpException.getMessage());
                    break;
            }
        } else if (e instanceof ConnectException) {
            onAccept(null, "网络连接异常，请检查您的网络状态");
        } else if (e instanceof SocketTimeoutException) {
            onAccept(null, "网络连接超时，请检查您的网络状态，稍后重试");
        } else if (e instanceof UnknownHostException) {
            onAccept(null, "网络异常，请检查您的网络状态");
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            onAccept(null, "数据解析错误");
        } else if (e instanceof SSLHandshakeException) {
            onAccept(null, "证书验证失败");
        } else if (e instanceof RuntimeException) {
            onAccept(null, "运行时异常");
        } else {
            onAccept(null, e.toString());
        }
    }

    @Override
    public void onComplete() {
    }

    public abstract void onAccept(T t, String error);

    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;
}
