
package com.example.apple.myeq.view;


import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.apple.myeq.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class EqManager {
    //小悬浮窗View的实例
    private static FloatingButtonView mFloatingBtnView;

    //大悬浮窗View的实例
    private static EqWindowView mEqWindowView;

    //小悬浮View的参数
    private static LayoutParams mFloatingBtnParams;

    //大悬浮View的参数
    private static LayoutParams mEqWindowParams;

    //用于控制在屏幕上添加或移除悬浮窗
    private static WindowManager mWindowManager;


    //用于获取手机可用内存
    private static ActivityManager mActivityManager;

    public static void createSmallWindow(Context context) {
        //WindowManager基本用到:addView，removeView，updateViewLayout
        WindowManager windowManager = getWindowManager(context);
        //获取屏幕宽高 abstract Display  getDefaultDisplay()；  //获取默认显示的 Display 对象
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        //设置小悬浮窗口的位置以及相关参数
        if (mFloatingBtnView == null) {
            mFloatingBtnView = new FloatingButtonView(context);
            if (mFloatingBtnParams == null) {
                mFloatingBtnParams = new LayoutParams();//
                mFloatingBtnParams.type = LayoutParams.TYPE_PHONE;//设置窗口的window type
                mFloatingBtnParams.format = PixelFormat.RGBA_8888;//设置图片格式，效果为背景透明
                mFloatingBtnParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;//下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
                mFloatingBtnParams.gravity = Gravity.LEFT | Gravity.TOP;//调整悬浮窗口位置在左边中间
                mFloatingBtnParams.width = FloatingButtonView.mViewWidth;//设置悬浮窗口的宽高
                mFloatingBtnParams.height = FloatingButtonView.mViewHeight;
                mFloatingBtnParams.x = screenWidth;//设置悬浮窗口位置
                mFloatingBtnParams.y = screenHeight / 2;
            }
            mFloatingBtnView.setParams(mFloatingBtnParams);
            windowManager.addView(mFloatingBtnView, mFloatingBtnParams);//将需要加到悬浮窗口中的View加入到窗口中
        }
    }

    /**
     * 创建一个大悬浮窗。位置为屏幕正中间。
     *
     * @param context 必须为应用程序的Context.
     */
//    @SuppressWarnings("deprecation")
    public static void createBigWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (mEqWindowView == null) {
            mEqWindowView = new EqWindowView(context);
            if (mEqWindowParams == null) {
                mEqWindowParams = new LayoutParams();
                mEqWindowParams.x = screenWidth / 3 - EqWindowView.viewWidth / 3;
                mEqWindowParams.y = screenHeight / 3 - EqWindowView.viewHeight / 3;
                mEqWindowParams.type = LayoutParams.TYPE_PHONE;
                mEqWindowParams.format = PixelFormat.RGBA_8888;
                mEqWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                mEqWindowParams.width = EqWindowView.viewWidth;
                mEqWindowParams.height = EqWindowView.viewHeight;
            }
            windowManager.addView(mEqWindowView, mEqWindowParams);
        }
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     * abstract void removeViewImmediate(View view)；//是removeView(View) 的一个特殊扩展，
     * 在方法返回前能够立即调用该视图层次的View.onDetachedFromWindow() 方法。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context) {
        if (mFloatingBtnView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mFloatingBtnView);//移除悬浮窗口
            mFloatingBtnView = null;
        }
    }

    /**
     * 将大悬浮窗从屏幕上移除。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void removeBigWindow(Context context) {
        if (mEqWindowView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mEqWindowView);
            mEqWindowView = null;
        }
    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return mFloatingBtnView != null || mEqWindowView != null;
        //return smallWindowActivity != null;
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context 必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 计算已使用内存的百分比，并返回。
     *
     * @param context 可传入应用程序上下文。
     * @return 已使用内存的百分比，以字符串形式返回。
     */
    public static String getUsedPercentValue(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            long availableSize = getAvailableMemory(context) / 1024;
            int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
            return percent + "%";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "悬浮窗";
    }

    /**
     * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比。
     *
     * @param context 可传入应用程序上下文。
     */
    public static void updateUsedPercent(Context context) {
        if (mFloatingBtnView != null) {
            TextView percentView = (TextView) mFloatingBtnView.findViewById(R.id.percent);
            percentView.setText(getUsedPercentValue(context));
        }
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存。
     */
    private static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
     *
     * @param context 可传入应用程序上下文。
     * @return ActivityManager的实例，用于获取手机可用内存。
     */
    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }
}

