package com.alin.customapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

import java.io.*;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-07 10:39
 **/
public class HttpImageView extends android.support.v7.widget.AppCompatImageView {

    public static final int GET_DATA_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;

    //子线程不能操作UI，通过Handler设置图片
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA_SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                    setBackground(drawable);
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(getContext(), "服务器发生错误", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getContext(), "设置网络图片失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public HttpImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HttpImageView(Context context) {
        super(context);
    }

    public HttpImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //设置网络图片
    public void setImageURL(String urlPath, String localPath) {
        final File file = new File(getContext().getCacheDir() + localPath);
        File parent = file.getParentFile();
        if (!parent.exists() || !parent.isDirectory()){
            parent.mkdirs();
        }
        if (file.exists() && (System.currentTimeMillis() - file.lastModified()) > 5*60*1000L){
            file.delete();
        }
        if (!file.exists()){
            try {
                file.createNewFile();
                AsyncHttpClient httpClient = new AsyncHttpClient();
                httpClient.get(urlPath, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (statusCode == 200){
                            Bitmap bitmap = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(file.getAbsoluteFile());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            sendHandleMessage(bitmap);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        //网络连接错误
                        handler.sendEmptyMessage(NETWORK_ERROR);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            sendHandleMessage(bitmap);
        }
    }

    /**
     * 利用Message把图片发给Handler
     * @param bitmap
     */
    private void sendHandleMessage(Bitmap bitmap){
        Message msg = Message.obtain();
        msg.obj = bitmap;
        msg.what = GET_DATA_SUCCESS;
        handler.sendMessage(msg);
    }
}
