package com.alin.android.app.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import com.alin.android.app.model.Banner;
import com.alin.android.app.view.HttpImageView;

import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-06 17:17
 **/
public class BannerPageAdapter extends PagerAdapter implements View.OnTouchListener{

    private Context context;
    private List<Banner> bannerList;
    private Handler handler;

    public BannerPageAdapter() {
    }

    public BannerPageAdapter(Context context, List<Banner> bannerList, Handler handler) {
        this.context = context;
        this.bannerList = bannerList;
        this.handler = handler;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Banner banner = bannerList.get(position % bannerList.size());
        HttpImageView imageView = new HttpImageView(context);
        imageView.setImageURL(banner.getImageUrl(), "/image/banner/"+banner.getId()+".jpg");
        imageView.setOnTouchListener(this);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacksAndMessages(null);
                handler.sendEmptyMessage(0);
                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(1, 5000);
                break;
            case MotionEvent.ACTION_CANCEL:
                handler.sendEmptyMessageDelayed(1, 5000);
                break;
            default:
                break;
        }
        return true;
    }
}
