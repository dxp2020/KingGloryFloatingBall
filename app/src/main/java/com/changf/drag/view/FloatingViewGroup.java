package com.changf.drag.view;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.changf.drag.R;
import com.changf.drag.utils.MotionEventUtils;
import com.changf.drag.utils.ViewUtils;

public class FloatingViewGroup extends ViewGroup {
    private static final String TAG = FloatingViewGroup.class.getSimpleName();

    private ImageView imageMain;
    private boolean isInitLocation;//是否初始化了位置
    private float lastX;
    private float lastY;

    public FloatingViewGroup(Context context) {
        this(context,null);
    }

    public FloatingViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        imageMain = new ImageView(context);
        imageMain.setImageResource(R.mipmap.icon_suspend_main);
        addView(imageMain);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int count = getChildCount();
        for(int i=0;i<count;i++){
            View view = getChildAt(i);
            measureChild(view,widthMeasureSpec,heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(!isInitLocation){
            isInitLocation = true;
            int width = imageMain.getMeasuredWidth();
            int height = imageMain.getMeasuredHeight();
            int left = (r-l)/2-width/2;
            int top = (b-t)/2-height/2;
            int right = (r-l)/2+width/2;
            int bottom = (b-t)/2+height/2;
            imageMain.layout(left,top,right,bottom);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(TAG, MotionEventUtils.getPrintStr(event.getAction()));
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return isImageMainArea(event);
            case MotionEvent.ACTION_MOVE:
                moveImageMain(event);
                break;
        }
        return true;
    }

    private void moveImageMain(MotionEvent event) {
        int intevalX = (int) (event.getX()-lastX);
        int intevalY = (int) (event.getY()-lastY);
        lastX = event.getX();
        lastY = event.getY();
        imageMain.offsetLeftAndRight(intevalX);
        imageMain.offsetTopAndBottom(intevalY);
    }

    private boolean isImageMainArea(MotionEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        float x = lastX;
        float y = lastY;

        int left = imageMain.getLeft();
        int top = imageMain.getTop();
        int right = imageMain.getRight();
        int bottom = imageMain.getBottom();
        if(x>left&&x<right&&y>top&&y<bottom){
            return true;
        }
        return false;
    }
}
