package com.changf.drag.bak.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.changf.drag.view.FloatingView;
import com.changf.drag.R;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

public class FloatingWindowService extends Service {
    private static final String TAG = FloatingView.class.getSimpleName();

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View floatingWindowContainer;
    private FloatingView mFloatingWindowView;
    private float lastX = 0;//刚按下去时的X坐标
    private float lastY = 0;//刚按下去时的Y坐标
    private float thisX = 0;//当前响应时的X坐标
    private float thisY = 0;//当前响应时的Y坐标
    private long lastDownTime = 0;//刚按下时间
    private long thisEventTime = 0;//响应的时间
    private long longPressTime = 500;//时间间隔
    private boolean mLongClick = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        layoutParams = new WindowManager.LayoutParams();
        //必须是Application的windowManager
        windowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        //设置window type
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        layoutParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        layoutParams.flags = FLAG_NOT_TOUCH_MODAL | FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        //设置悬浮窗口长宽数据
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        floatingWindowContainer = View.inflate(this, R.layout.layout_floating_window,null);
        //添加mFloatLayout
        windowManager.addView(floatingWindowContainer, layoutParams);

        mFloatingWindowView = floatingWindowContainer.findViewById(R.id.fw_floating_window);

        floatingWindowContainer.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //设置监听浮动窗口的触摸移动
        mFloatingWindowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    lastX = motionEvent.getRawX();
                    lastY = motionEvent.getRawY();
                    lastDownTime = motionEvent.getDownTime();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    thisX = motionEvent.getRawX();
                    thisY = motionEvent.getRawY();
                    thisEventTime = motionEvent.getEventTime();
                    if (mLongClick) {
                        //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                        layoutParams.x = (int) motionEvent.getRawX() - mFloatingWindowView.getMeasuredWidth() / 2;
                        layoutParams.y = (int) motionEvent.getRawY() - mFloatingWindowView.getMeasuredHeight() / 2 - 25;
                        //刷新
                        windowManager.updateViewLayout(floatingWindowContainer, layoutParams);
                        return false;
                    } else {
                        mLongClick = isLongPressed(lastX, lastY, thisX, thisY, lastDownTime, thisEventTime, longPressTime);
                        if (!mLongClick) {
                            //这里添加自己想要的点击响应

                        }
                    }
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mLongClick = false;
                }
                return false;
            }
        });
    }

    //判断是否是长按事件
    static boolean isLongPressed(float lastX, float lastY, float thisX,
                                 float thisY, long lastDownTime, long thisEventTime,
                                 long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= 10 && offsetY <= 10 && intervalTime >= longPressTime) {
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (layoutParams != null) {
            //移除悬浮窗口
            windowManager.removeView(floatingWindowContainer);
        }
    }

}
