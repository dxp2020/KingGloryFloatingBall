package com.changf.drag.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import android.widget.Toast;

import com.changf.drag.utils.MotionEventUtils;
import com.changf.drag.utils.ViewUtils;

public class FloatingUtilView extends View {
    private static final String TAG = FloatingView.class.getSimpleName();
    //内圆圆心坐标
    public static float inner_circle_x;
    public static float inner_circle_y;
    //外圆圆心坐标
    public static float outer_circle_x;
    public static float outer_circle_y;
    //实心大圆半径
    private static float radius = 80;
    //圆环最大边沿半径
    private static float ringSideRaduis;
    //圆环半径
    private static float ringRadius;
    //屏幕宽度
    private static float screenWidth;
    //屏幕高度
    private static float screenHeight;
    //水平方向的差值
    private static float intevalY;
    //View的中心X坐标
    private static float centerX;
    //View的中心Y坐标
    private static float centerY;
    //手指按下的X坐标
    private static float downX;
    //手指按下的Y坐标
    private static float downY;
    //刚按下时间
    private long lastDownTime = 0;
    //点击时间间隔
    private long shortClickTime = 300;
    //长按时间间隔
    private long longPressTime = 500;

    //滚动工具类
    private Scroller mScroller;
    //振动器
    private Vibrator vibrator;
    //当前事件的MotionEvent
    private MotionEvent motionEvent;

    private Paint ringPaint;
    private RectF ringRect;
    private boolean isShowCircleView;
    private boolean isLongPress;

    public FloatingUtilView(Context context) {
        this(context,null);
    }

    public FloatingUtilView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);

        ringPaint = new Paint();
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(radius*2);
        ringPaint.setColor(Color.parseColor("#7effffff"));

        mScroller = new Scroller(context);
        screenWidth = ViewUtils.getScreenWidth(context);
        screenHeight = ViewUtils.getScreenHeight(context);
        ringRadius = screenWidth/2-radius;
        ringSideRaduis = ringRadius+radius;
    }

    /**
     * 这几个参数代表，在屏幕中的位置
     * 内圆、外圆的坐标，是相对控件的位置
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        intevalY = screenHeight-bottom;
        centerX = (right-left)/2;
        centerY = (bottom-top)/2;
        updateLocation((right-left)/2,(bottom-top)/2);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    isLongPress = isLongPressed(downX, downY, motionEvent.getRawX(), motionEvent.getRawY());
                    if (isLongPress) {
                        lastDownTime = 0;
                        vibrator.vibrate(100);
                    }
                    break;
            }
        }
    };

    public boolean onTouchEvent(MotionEvent event) {
        Log.e(TAG,MotionEventUtils.getPrintStr(event.getAction(),event.getRawX()+"",event.getRawY()+""));
        this.motionEvent = event;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return isSmallCircleArea(event);
            case MotionEvent.ACTION_MOVE:
                if(isLongPress){
                    moveView(event);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(isLongPress) {
                    attachedToSide(event);
                }
                if(isClickEvent(downX, downY, event.getRawX(), event.getRawY(), lastDownTime, event.getEventTime(), shortClickTime)){
                    onClickEvent();
                }
                isLongPress = false;
                mHandler.removeMessages(0);
                break;
        }
        return false;
    }

    private boolean isSmallCircleArea(MotionEvent event) {
        downX = event.getRawX();
        downY = event.getRawY();
        float x = event.getRawX();
        float y = getRealY(event.getRawY());
        float circleMaxX = outer_circle_x+radius;
        float circleMinX = outer_circle_x-radius;
        float circleMaxY = outer_circle_y+radius;
        float circleMinY = outer_circle_y-radius;
        if(x<circleMaxX&&x>circleMinX&&y<circleMaxY&&y>circleMinY){
            lastDownTime = event.getDownTime();
            //显示圆环的情况下，不能移动View
            if(!isShowCircleView){
                mHandler.sendEmptyMessageDelayed(0,longPressTime);
            }
            return true;
        }else{
            return false;
        }
    }

    private void attachedToSide(MotionEvent event) {
        float x = event.getRawX();
        float y = getRealY(event.getRawY());
        attachedToSide(x,y);
    }

    private void attachedToSide(float x,float y) {
        if(x<centerX){
            if(y<centerY&&y<ringSideRaduis) {
                //吸附到左上角
                mScroller.startScroll((int) x, (int) y, (int) -(x - radius), (int) -(y - radius));
            }else if(y>centerY&&(getBottom()-y)<ringSideRaduis){
                //吸附到左下角
                mScroller.startScroll((int) x, (int) y, (int) -(x - radius), (int) (getBottom()-y-radius));
            }else{
                //吸附到左边
                mScroller.startScroll((int) x,(int)y, (int) -(x-radius), 0);
            }
        }else if(x>centerX){
            if(y<centerY&&y<ringSideRaduis) {
                //吸附到左上角
                mScroller.startScroll((int) x, (int) y, (int) (screenWidth-x-radius), (int) -(y - radius));
            }else if(y>centerY&&(getBottom()-y)<ringSideRaduis){
                //吸附到左下角
                mScroller.startScroll((int) x, (int) y, (int) (screenWidth-x-radius), (int) (getBottom()-y-radius));
            }else{
                //吸附到左边
                mScroller.startScroll((int) x,(int)y, (int) (screenWidth-x-radius), 0);
            }
        }
        invalidate();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#7effffff"));
        canvas.drawCircle(outer_circle_x, outer_circle_y, radius, paint);
        paint.setColor(Color.parseColor("#c5fcfbfb"));
        canvas.drawCircle(inner_circle_x, inner_circle_y, radius - 30, paint);

        if(isShowCircleView){
            canvas.drawArc(ringRect,0,360,false,ringPaint);
        }
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            updateLocation(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    private void moveView(MotionEvent event) {
        updateLocation(event.getRawX(),getRealY(event.getRawY()));
        invalidate();
    }

    private void updateLocation(float downX,float downY){
        inner_circle_x = downX;
        inner_circle_y = downY;
        outer_circle_x = inner_circle_x;
        outer_circle_y = inner_circle_y;
        ringRect = new RectF(inner_circle_x-ringRadius,inner_circle_y-ringRadius,inner_circle_x+ringRadius,inner_circle_y+ringRadius);
    }

    private float getRealY(float rawY) {
        return rawY- intevalY;
    }

    //判断是否是单机事件
    private boolean isClickEvent(float lastX, float lastY, float thisX,
                                 float thisY, long lastDownTime, long thisEventTime,
                                 long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= 10 && offsetY <= 10 && intervalTime < longPressTime) {
            return true;
        }
        return false;
    }

    //判断是否是长按事件
    private boolean isLongPressed(float lastX, float lastY, float thisX,float thisY) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        if (offsetX <= 10 && offsetY <= 10) {
            return true;
        }
        return false;
    }

    private void onClickEvent() {
        isShowCircleView=!isShowCircleView;
        invalidate();
    }

}
