package com.alin.android.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.alin.app.R;
import com.alin.android.app.adapter.BannerPageAdapter;
import com.alin.android.app.model.Banner;
import com.alin.android.app.view.HttpImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-06 16:18
 **/
public class BannerFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private Context context;
    private List<Banner> bannerList;
    private View bannerView = null;
    private BannerPageAdapter bannerPageAdapter;
    private ViewPager bannerViewPager;
    private HttpImageView imageView;
    private TextView titleView;
    private LinearLayout ll_dots;
    private List<ImageView> dotsList;

    //线程是否停止
    private boolean isStop = false;
    //间隔时间
    private static int PAGER_TIME = 5000;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    isStop = true;
                    break;
                case 1:
                    isStop = false;
                    autoPlayView();
                    break;
                default:
                    break;
            }
        }
    };

    public BannerFragment() {
    }

    public BannerFragment(Context context, List<Banner> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 初始化viewPager
        initViewPager();
        // 初始化小点
        initDots();
        // 自动轮播
        autoPlayView();
        return bannerView;
    }

    /**
     * 初始化viewPager
     */
    private void initViewPager() {
        bannerView = LayoutInflater.from(context).inflate(R.layout.banner, null);
        titleView = (TextView) bannerView.findViewById(R.id.banner_title);
        bannerViewPager = (ViewPager) bannerView.findViewById(R.id.banner_viewpager);
        if (bannerList != null && bannerList.size() > 0) {
            bannerPageAdapter = new BannerPageAdapter(context, bannerList, handler);
            bannerViewPager.setAdapter(bannerPageAdapter);
            bannerViewPager.addOnPageChangeListener(this);
            bannerViewPager.setCurrentItem(Integer.MAX_VALUE / 2);
        }
    }

    /**
     * 初始化小点
     */
    private void initDots() {
        ll_dots = (LinearLayout) bannerView.findViewById(R.id.banner_dots_line);
        dotsList = new ArrayList<ImageView>();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dip2px(context, 20), dip2px(context, 10));
        // params.setMargins(dip2px(context, 10), 0, dip2px(context, 10), 0);
        for (Banner banner : bannerList) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.banner_dots_default));
            imageView.setLayoutParams(params);
            ll_dots.addView(imageView);
            dotsList.add(imageView);
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 自动轮播
     */
    private void autoPlayView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
                    SystemClock.sleep(PAGER_TIME);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bannerViewPager.setCurrentItem(bannerViewPager.getCurrentItem() + 1);
                        }
                    });
                }
            }
        }).start();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        position %= bannerList.size();
        titleView.setText(bannerList.get(position).getName());

        if (dotsList != null){
            for (ImageView dotView : dotsList) {
                dotView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.banner_dots_default));
            }
            dotsList.get(position).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.banner_dots_checked));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onDestroyView() {
        isStop = true;
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}
