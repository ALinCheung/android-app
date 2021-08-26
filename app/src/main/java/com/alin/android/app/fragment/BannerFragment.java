package com.alin.android.app.fragment;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.alin.android.app.R;
import com.alin.android.app.adapter.BannerPageAdapter;
import com.alin.android.app.model.Banner;
import com.alin.android.core.base.BaseFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-06 16:18
 **/
public class BannerFragment extends BaseFragment implements ViewPager.OnPageChangeListener {

    @BindView(R.id.banner_viewpager)
    public ViewPager bannerViewPager;
    @BindView(R.id.banner_title)
    public TextView titleView;
    @BindView(R.id.banner_dots_line)
    public LinearLayout ll_dots;

    private Unbinder unbinder;
    private BannerPageAdapter bannerPageAdapter;
    private List<Banner> bannerList;
    private List<ImageView> dotsList;

    //线程是否停止
    private static boolean isStop = false;
    //间隔时间
    private static int PAGER_TIME = 5000;
    private Handler handler;

    public BannerFragment(List<Banner> bannerList) {
        this.bannerList = bannerList;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.banner;
    }

    @Override
    protected void initData() {
        unbinder = ButterKnife.bind(this, view);
        handler = new BannerFragmentHandle(this);
        // 初始化viewPager
        initViewPager();
        // 初始化小点
        initDots();
        // 自动轮播
        autoPlayView();
    }

    /**
     * 初始化viewPager
     */
    private void initViewPager() {
        if (bannerList != null && bannerList.size() > 0) {
            bannerPageAdapter = new BannerPageAdapter(mContext, bannerList, handler);
            bannerViewPager.setAdapter(bannerPageAdapter);
            bannerViewPager.addOnPageChangeListener(this);
            bannerViewPager.setCurrentItem(Integer.MAX_VALUE / 2);
        }
    }

    /**
     * 初始化小点
     */
    private void initDots() {
        dotsList = new ArrayList<ImageView>();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dip2px(mContext, 20), dip2px(mContext, 10));
        // params.setMargins(dip2px(mContext, 10), 0, dip2px(mContext, 10), 0);
        for (Banner banner : bannerList) {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.banner_dots_default));
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
    public void autoPlayView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
                    SystemClock.sleep(PAGER_TIME);
                    if (isAdded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int index;
                                if (bannerViewPager == null) {
                                    index = 0;
                                } else {
                                    index = bannerViewPager.getCurrentItem();
                                }
                                bannerViewPager.setCurrentItem(index + 1);
                            }
                        });
                    }
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
                dotView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.banner_dots_default));
            }
            dotsList.get(position).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.banner_dots_checked));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onDestroyView() {
        isStop = true;
        handler.removeCallbacksAndMessages(null);
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    public static class BannerFragmentHandle extends Handler {

        private final WeakReference<BannerFragment> mWeakReference;

        BannerFragmentHandle(BannerFragment bannerFragment) {
            mWeakReference = new WeakReference<>(bannerFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            BannerFragment bannerFragment = mWeakReference.get();
            switch (msg.what) {
                case 0:
                    isStop = true;
                    break;
                case 1:
                    isStop = false;
                    bannerFragment.autoPlayView();
                    break;
                default:
                    break;
            }
        }
    }
}
