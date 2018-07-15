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
import android.view.ViewConfiguration;
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
    //小球隐藏时间
    private long duration = 250;
    //在被判定为滚动之前用户手指可以移动的最大值。
    private int touchSlop;

    //滚动工具类
    private Scroller mScroller;

    private Paint ringPaint;
    private RectF ringRect;
    private boolean isShowCircleView;
    private boolean isHidden = true;
    private boolean isScrolling = false;

    public FloatingUtilView(Context context) {
        this(context,null);
    }

    public FloatingUtilView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        ringPaint = new Paint();
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(radius*2);
        ringPaint.setColor(Color.parseColor("#7effffff"));

        mScroller = new Scroller(context);
        screenWidth = ViewUtils.getScreenWidth(context);
        screenHeight = ViewUtils.getScreenHeight(context);
        ringRadius = screenWidth/3-radius;
        ringSideRaduis = ringRadius+radius;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
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
        updateLocation(0,ringSideRaduis);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return isSmallCircleArea(event);
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(downX-event.getRawX())>touchSlop||Math.abs(downY-event.getRawY())>touchSlop) {
                    moveView(event);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(isClickEvent(downX, downY, event.getRawX(), event.getRawY(), lastDownTime, event.getEventTime(), shortClickTime)){
                    onClickEvent();
                }else{
                    attachedToSide(event);
                }
                break;
        }
        return false;
    }

    private boolean isSmallCircleArea(MotionEvent event) {
        downX = event.getRawX();
        downY = event.getRawY();
        float x = event.getRawX();
        float y = getRealY(event.getRawY());
        float circleMaxX = inner_circle_x+radius;
        float circleMinX = inner_circle_x-radius;
        float circleMaxY = inner_circle_y+radius;
        float circleMinY = inner_circle_y-radius;
        if(x<circleMaxX&&x>circleMinX&&y<circleMaxY&&y>circleMinY){
            lastDownTime = event.getDownTime();
            return true;
        }else{
            return false;
        }
    }

    private void moveView(MotionEvent event) {
        duration = 250;
        isShowCircleView = false;
        handler.removeMessages(0);
        updateLocation(event.getRawX(),getRealY(event.getRawY()));
        invalidate();
    }

    private void attachedToSide(MotionEvent event) {
        isHidden = false;
        float x = event.getRawX();
        float y = getRealY(event.getRawY());
        attachedToSide(x,y);
    }

    private void attachedToSide(float x,float y) {
        //当小球移动到左上部分、右上部分的时候
        if(y<centerY&&((x<centerX&&y<x)||(x>centerX&&y<(screenWidth-x)))){
            //吸附到顶部
            if((x-ringSideRaduis)>0&&(x+ringSideRaduis)<screenWidth){
                mScroller.startScroll((int) x, (int) y, 0, (int) -(y - radius));
                invalidate();
                return;
            }else if(x<centerX){
                mScroller.startScroll((int) x, (int) y, (int) -(x-ringSideRaduis), (int) -(y - radius));
                invalidate();
                return;
            }else if(x>centerX){
                mScroller.startScroll((int) x, (int) y, (int) (screenWidth-x-ringSideRaduis), (int) -(y - radius));
                invalidate();
                return;
            }
        //小球移动到左下部分、右下部分的时候
        }else if(y>centerY&&((x<centerX&&(getBottom()-y)<x)||(x>centerX&&((getBottom()-y)<(screenWidth-x))))){
            //吸附到底部
            if((x-ringSideRaduis)>0&&(x+ringSideRaduis)<screenWidth){
                mScroller.startScroll((int) x, (int) y, 0, (int) (getBottom()-y - radius));
                invalidate();
                return;
            }else if(x<centerX){
                mScroller.startScroll((int) x, (int) y, (int) -(x-ringSideRaduis),(int) (getBottom()-y - radius));
                invalidate();
                return;
            }else if(x>centerX){
                mScroller.startScroll((int) x, (int) y, (int) (screenWidth-x-ringSideRaduis),(int) (getBottom()-y - radius));
                invalidate();
                return;
            }
            return;
        }
        if(x<centerX){
            if(y<centerY&&y<ringSideRaduis) {
                //吸附到左上角
                mScroller.startScroll((int) x, (int) y, (int) -(x - radius), (int) -(y - ringSideRaduis));
            }else if(y>centerY&&(getBottom()-y)<ringSideRaduis){
                //吸附到左下角
                mScroller.startScroll((int) x, (int) y, (int) -(x - radius), (int) (getBottom()-y-ringSideRaduis));
            }else{
                //吸附到左边
                mScroller.startScroll((int) x,(int)y, (int) -(x-radius), 0);
            }
        }else if(x>centerX){
            if(y<centerY&&y<ringSideRaduis) {
                //吸附到左上角
                mScroller.startScroll((int) x, (int) y, (int) (screenWidth-x-radius), (int) -(y - ringSideRaduis));
            }else if(y>centerY&&(getBottom()-y)<ringSideRaduis){
                //吸附到左下角
                mScroller.startScroll((int) x, (int) y, (int) (screenWidth-x-radius), (int) (getBottom()-y-ringSideRaduis));
            }else{
                //吸附到左边
                mScroller.startScroll((int) x,(int)y, (int) (screenWidth-x-radius), 0);
            }
        }
        invalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            isScrolling = true;
            updateLocation(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }else{
            isScrolling = false;
            if(!isHidden){
                handler.sendEmptyMessageDelayed(0,duration);
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    isShowCircleView = false;
                    isHidden = true;
                    if(Math.abs(inner_circle_x-radius)<2){
                        mScroller.startScroll((int) inner_circle_x, (int) inner_circle_y, (int) -radius, 0);
                        postInvalidate();
                    }else if(Math.abs(inner_circle_x-getRight()+radius)<2){
                        mScroller.startScroll((int) inner_circle_x, (int) inner_circle_y, (int) radius, 0);
                        postInvalidate();
                    }else if(Math.abs(inner_circle_y-radius)<2){
                        mScroller.startScroll((int) inner_circle_x, (int) inner_circle_y, 0, (int) -radius);
                        postInvalidate();
                    }else if(Math.abs(inner_circle_y-getBottom()+radius)<2){
                        mScroller.startScroll((int) inner_circle_x, (int) inner_circle_y, 0, (int) radius);
                        postInvalidate();
                    }
                    break;
            }
        }
    };

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

    /**
     * 隐藏状态---->显示出来
     * 显示状态---->显示、隐藏圆环
     */
    private void onClickEvent() {
        //滚动状态禁止点击事件
        if(isScrolling){
           return;
        }
        if(!isHidden){
            isShowCircleView = !isShowCircleView;
            handler.removeMessages(0);
            invalidate();
            return;
        }
        duration = 2000;
        isHidden = false;
        isShowCircleView = !isShowCircleView;
        if(Math.abs(inner_circle_x)<2){
            mScroller.startScroll((int) inner_circle_x, (int) inner_circle_y, (int) radius, 0);
        }else if(Math.abs(inner_circle_x-getRight())<2){
            mScroller.startScroll((int) inner_circle_x, (int) inner_circle_y, (int) -radius, 0);
        }else if(Math.abs(inner_circle_y)<2){
            mScroller.startScroll((int) inner_circle_x, (int) inner_circle_y, 0, (int) radius);
        }else if(Math.abs(inner_circle_y-getBottom())<2){
            mScroller.startScroll((int) inner_circle_x, (int) inner_circle_y, 0, (int) -radius);
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

}
