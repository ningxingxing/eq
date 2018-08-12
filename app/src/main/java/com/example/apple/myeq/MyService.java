package com.example.apple.myeq;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.apple.myeq.utils.EqUtils;
import com.example.apple.myeq.view.EqManager;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    private final String TAG = "MyService";


    private Handler mHandler = new Handler();
    private Timer mTimer;
    private EqUtils mEqUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        mEqUtils = new EqUtils();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mTimer==null){
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new RefreshTask(),0,500);
        }

        return super.onStartCommand(intent, flags, startId);

    }

    class RefreshTask extends TimerTask{

        @Override
        public void run() {

            if (mEqUtils.isHome(getApplication()) && !EqManager.isWindowShowing()){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        EqManager.createSmallWindow(getApplicationContext()) ;
                    }
                });
            }else if (!mEqUtils.isHome(getApplication())&& EqManager.isWindowShowing()){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EqManager.removeSmallWindow(getApplicationContext());
                        EqManager.removeBigWindow(getApplicationContext());
                    }
                }) ;

            }
            // 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
            else if (mEqUtils.isHome(getApplication()) && EqManager.isWindowShowing()){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EqManager.updateUsedPercent(getApplicationContext());
                    }
                });
            }


        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }
    }
}
