package com.changf.drag.view;

import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

public class FloatingLayout extends ViewGroup{

    private WindowManager.LayoutParams layoutParams = null;
    /**
     * 在被判定为滚动之前用户手指可以移动的最大值。
     */
    private int touchSlop;

    /**
     * View内部的x坐标
     */
    private float x;
    /**
     * View内部的y坐标
     */
    private float y;
    /**
     * 屏幕的x坐标
     */
    private float rawX;
    /**
     * 屏幕的y坐标
     */
    private float rawY;

    //刚按下时间
    private long lastDownTime = 0;

    //时间间隔
    private long longPressTime = 500;

    private Vibrator vibrator;

    private WindowManager windowManager = null;


    public FloatingLayout(Context context) {
        this(context,null);
    }

    public FloatingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        addView(new FloatingView(context));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View child = getChildAt(0);
        measureChild(child,widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(child.getMeasuredWidth(),child.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View view = getChildAt(0);
        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                rawX = event.getRawX();
                rawY = event.getRawY();
                lastDownTime = event.getDownTime();
                break;
            case MotionEvent.ACTION_MOVE:
                return isLongPressed(rawX, rawY, event.getRawX(), event.getRawY(), lastDownTime, event.getEventTime(), longPressTime);
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                rawX = event.getRawX();
                rawY = event.getRawY();
                layoutParams.x = (int)rawX-getMeasuredWidth() / 2;
                layoutParams.y = (int)rawY-getMeasuredHeight() / 2;
                windowManager.updateViewLayout(this, layoutParams);
                break;
        }
        return true;
    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    public void setLayoutParams(WindowManager.LayoutParams layoutParams) {
        this.layoutParams = layoutParams;
    }

    //判断是否是长按事件
    private boolean isLongPressed(float lastX, float lastY, float thisX,
                                 float thisY, long lastDownTime, long thisEventTime,
                                 long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= 10 && offsetY <= 10 && intervalTime >= longPressTime) {
            vibrator.vibrate(100);
            return true;
        }
        return false;
    }
}
