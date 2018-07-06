package com.changf.drag;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class FloatingWindowView extends View {
    private static final String TAG = FloatingWindowView.class.getSimpleName();
    //内圆圆心坐标
    public static float inner_circle_x;
    public static float inner_circle_y;
    //外圆圆心坐标
    public static float outer_circle_x;
    public static float outer_circle_y;
    //半径
    private static float radius = 80;

    public FloatingWindowView(Context context) {
        this(context,null);
    }

    public FloatingWindowView(Context context,AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);

//        Log.e(TAG,"wSpecSize-->"+wSpecSize+" hSpecSize-->"+hSpecSize+" wSpecMode-->"+wSpecMode+" hSpecMode-->"+hSpecMode);

        //宽高都为wrap_content
        if (wSpecMode == MeasureSpec.AT_MOST && hSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int)radius*4, (int)radius*4);
        //取固定高度作为View的长宽
        } else if (wSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(hSpecSize, hSpecSize);
        //取固定宽度作为View的长宽
        } else if (hSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(wSpecSize, wSpecSize);
        //宽高固定取最小的值作为宽高
        } else if (wSpecMode == MeasureSpec.EXACTLY && hSpecMode == MeasureSpec.EXACTLY) {
            int minSize = Math.min(wSpecSize,hSpecSize);
            setMeasuredDimension(minSize, minSize);
        }
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
        radius = getMeasuredWidth()/4;
        inner_circle_x = (right - left)/2;
        inner_circle_y = (bottom - top)/2;
        outer_circle_x = inner_circle_x;
        outer_circle_y = inner_circle_y;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            outer_circle_x = inner_circle_x;
            outer_circle_y = inner_circle_y;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            //控件内
            if (event.getX()>0 && event.getX()<radius*4&&event.getY()>0 && event.getY()<radius*4) {
                Log.e(TAG,"在圆内");
                //超出范围则限定大圆的边在小圆的圆心位置
                if(event.getX()>(inner_circle_x+radius)) {
                    outer_circle_x = inner_circle_x + radius;
                }else if(event.getX()<(inner_circle_x-radius)){
                    outer_circle_x = inner_circle_x-radius;
                }else{
                    outer_circle_x = event.getX();
                }
                if(event.getY()>(inner_circle_y+radius)) {
                    outer_circle_y = inner_circle_y + radius;
                }else if(event.getY()<(inner_circle_y-radius)){
                    outer_circle_y = inner_circle_y - radius;
                }else{
                    outer_circle_y = event.getY();
                }
            //控件外
            } else {
                Log.e(TAG,"在圆外");
                //按下的点与圆点之间的角度
                double angle = Math.atan((event.getY()-outer_circle_y)/(event.getX()-outer_circle_x));
                double intevelY = Math.sin(angle)*radius;
                double intevelX = Math.cos(angle)*radius;
                //右上区间
                if(event.getY()<outer_circle_y&&event.getX()>outer_circle_x){
                    outer_circle_y = (float) (inner_circle_y-Math.abs(intevelY));
                    outer_circle_x = (float) (inner_circle_x+Math.abs(intevelX));
                //右下区间
                }else if(event.getY()>outer_circle_y&&event.getX()>outer_circle_x){
                    outer_circle_y = (float) (inner_circle_y+Math.abs(intevelY));
                    outer_circle_x = (float) (inner_circle_x+Math.abs(intevelX));
                //左上区间
                }else if(event.getY()<outer_circle_y&&event.getX()<outer_circle_x){
                    outer_circle_y = (float) (inner_circle_y-Math.abs(intevelY));
                    outer_circle_x = (float) (inner_circle_x-Math.abs(intevelX));
                //左下区间
                }else if(event.getY()>outer_circle_y&&event.getX()<outer_circle_x){
                    outer_circle_y = (float) (inner_circle_y+Math.abs(intevelY));
                    outer_circle_x = (float) (inner_circle_x-Math.abs(intevelX));
                }
            }
        }
        invalidate();
        return true;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#7effffff"));
        canvas.drawCircle(outer_circle_x, outer_circle_y, radius, paint);
        paint.setColor(Color.parseColor("#c5fcfbfb"));
        canvas.drawCircle(inner_circle_x, inner_circle_y, radius - 30, paint);
    }


}
