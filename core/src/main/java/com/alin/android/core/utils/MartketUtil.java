package com.alin.android.core.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @Description 市场工具类
 * @Author zhangwl
 * @Date 2021/7/23 16:51
 */
public class MartketUtil {

    /**
     * 应用商店包名列表
     */
    private static List<String> marketPackageList = Arrays.asList(new String[]{
            "com.android.vending",//Google Play
            "com.tencent.android.qqdownloader",//应用宝
            "com.qihoo.appstore",//360手机助手
            "com.baidu.appsearch",//百度手机助
            "com.xiaomi.market",//小米应用商店
            "com.wandoujia.phoenix2",//豌豆荚
            "com.huawei.appmarket",//华为应用市场
            "com.taobao.appcenter",//淘宝手机助手
            "com.hiapk.marketpho",//安卓市场
            "cn.goapk.market",//安智市场
    });

    /**
     * 获取所有已安装程序的包信息
     * @param context
     * @return
     */
    private static List<PackageInfo> getPackageInfoList(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        return packageManager.getInstalledPackages(0);
    }

    /**
     * 判断市场是否存在的方法
     *
     * @param context
     * @return
     */
    public static boolean isAvilible(Context context) {
        List<PackageInfo> pinfo = getPackageInfoList(context);
        if (pinfo != null) {
            for (PackageInfo packageInfo : pinfo) {
                if (marketPackageList.contains(packageInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取市场包名
     *
     * @param context
     * @return
     */
    public static String getMarketPkg(Context context) {
        if (OsUtil.isMIUI()) {
            return "com.xiaomi.market";
        }
        List<PackageInfo> pinfo = getPackageInfoList(context);
        if (pinfo != null) {
            for (PackageInfo packageInfo : pinfo) {
                if (marketPackageList.contains(packageInfo.packageName)) {
                    return packageInfo.packageName;
                }
            }
        }
        return null;
    }

    /**
     * 启动到应用商店app详情界面
     *
     * @param context
     * @param appPkg    目标App的包名
     * @param marketPkg 应用商店包名 ,如果为""则由系统弹出应用商店列表供用户选择,否则调转到目标市场的应用详情界面，某些应用商店可能会失败
     */
    public static void install(Context context, String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) {
                return;
            }
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
