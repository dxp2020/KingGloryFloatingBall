package com.changf.drag.view;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.changf.drag.R;
import com.changf.drag.utils.MotionEventUtils;
import com.changf.drag.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class FloatingViewGroup extends ViewGroup {
    private static final String TAG = FloatingViewGroup.class.getSimpleName();

    private ImageView imageMain;
    private boolean isInitLocation;//是否初始化了位置
    private float lastX;
    private float lastY;
    private float downX;
    private float downY;
    //刚按下时间
    private long lastDownTime = 0;
    //点击时间间隔
    private long shortClickTime = 300;
    //rawY与Y之间的差值
    private float intevalY;
    //View的中心X坐标
    private static float centerX;
    //View的中心Y坐标
    private static float centerY;
    //圆环最大边沿半径
    private static float ringSideRaduis = 300;
    //mainMenu的半径
    private static float menuRadius;

    //滚动工具类
    private Scroller mScroller;
    //菜单
    private List<ImageView> mMenuViews = new ArrayList<>();
    //是否在滚动
    private boolean isScrolling = false;
    //是否隐藏
    private boolean isHidden = true;
    //小球隐藏时间
    private long duration = 250;
    //在被判定为滚动之前用户手指可以移动的最大值。
    private int touchSlop;

    public FloatingViewGroup(Context context) {
        this(context,null);
    }

    public FloatingViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        imageMain = new ImageView(context);
        imageMain.setImageResource(R.mipmap.icon_suspend_main);
        addView(imageMain);

        ImageView menuView = new ImageView(context);
        menuView.setImageResource(R.mipmap.suspend_1);
        mMenuViews.add(menuView);

        addView(menuView);
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
            menuRadius = width/2;
            centerX = (r-l)/2;
            centerY = (b-t)/2;
            intevalY = ViewUtils.getScreenHeight(getContext())-b;

            imageMain.layout(-width/2,b/2-height/2,width/2,b/2+height/2);

            for(ImageView imageView:mMenuViews){
                int w = imageView.getMeasuredWidth();
                int h = imageView.getMeasuredHeight();
                imageView.layout(0,0,w,h);
                imageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return isImageMainArea(event);
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(downX-event.getRawX())>touchSlop||Math.abs(downY-event.getRawY())>touchSlop) {
                    moveMainMenu(event);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(isClickEvent(downX, downY, event.getRawX(), event.getRawY(), lastDownTime, event.getEventTime(), shortClickTime)){
                    onClickEvent();
                }else{
                    attachedToSide();
                }
                break;
        }
        return false;
    }

    private void attachedToSide() {
        isHidden = false;
        attachedToSide(getMainMenuX(),getMainMenuY());
    }

    private void attachedToSide(float x,float y) {
        //当小球移动到左上部分、右上部分的时候
        if(y<centerY&&((x<centerX&&y<x)||(x>centerX&&y<(getWidth()-x)))){
            //吸附到顶部
            if((x-ringSideRaduis)>0&&(x+ringSideRaduis)<getWidth()){
                mScroller.startScroll((int) x, (int) y, 0, (int) -(y - menuRadius));
                invalidate();
                return;
            }else if(x<centerX){
                mScroller.startScroll((int) x, (int) y, (int) -(x-ringSideRaduis), (int) -(y - menuRadius));
                invalidate();
                return;
            }else if(x>centerX){
                mScroller.startScroll((int) x, (int) y, (int) (getWidth()-x-ringSideRaduis), (int) -(y - menuRadius));
                invalidate();
                return;
            }
            //小球移动到左下部分、右下部分的时候
        }else if(y>centerY&&((x<centerX&&(getBottom()-y)<x)||(x>centerX&&((getBottom()-y)<(getWidth()-x))))){
            //吸附到底部
            if((x-ringSideRaduis)>0&&(x+ringSideRaduis)<getWidth()){
                mScroller.startScroll((int) x, (int) y, 0, (int) (getBottom()-y - menuRadius));
                invalidate();
                return;
            }else if(x<centerX){
                mScroller.startScroll((int) x, (int) y, (int) -(x-ringSideRaduis),(int) (getBottom()-y - menuRadius));
                invalidate();
                return;
            }else if(x>centerX){
                mScroller.startScroll((int) x, (int) y, (int) (getWidth()-x-ringSideRaduis),(int) (getBottom()-y - menuRadius));
                invalidate();
                return;
            }
            return;
        }
        if(x<centerX){
            if(y<centerY&&y<ringSideRaduis) {
                //吸附到左上角
                mScroller.startScroll((int) x, (int) y, (int) -(x - menuRadius), (int) -(y - ringSideRaduis));
            }else if(y>centerY&&(getBottom()-y)<ringSideRaduis){
                //吸附到左下角
                mScroller.startScroll((int) x, (int) y, (int) -(x - menuRadius), (int) (getBottom()-y-ringSideRaduis));
            }else{
                //吸附到左边
                mScroller.startScroll((int) x,(int)y, (int) -(x-menuRadius), 0);
            }
        }else if(x>centerX){
            if(y<centerY&&y<ringSideRaduis) {
                //吸附到左上角
                mScroller.startScroll((int) x, (int) y, (int) (getWidth()-x-menuRadius), (int) -(y - ringSideRaduis));
            }else if(y>centerY&&(getBottom()-y)<ringSideRaduis){
                //吸附到左下角
                mScroller.startScroll((int) x, (int) y, (int) (getWidth()-x-menuRadius), (int) (getBottom()-y-ringSideRaduis));
            }else{
                //吸附到左边
                mScroller.startScroll((int) x,(int)y, (int) (getWidth()-x-menuRadius), 0);
            }
        }
        invalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            isScrolling = true;
            imageMain.offsetLeftAndRight((int) (mScroller.getCurrX()-getMainMenuX()));
            imageMain.offsetTopAndBottom((int) (mScroller.getCurrY()-getMainMenuY()));
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
                    isHidden = true;
                    float mainMenuX = getMainMenuX();
                    float mainMenuY = getMainMenuY();

                    if(Math.abs(mainMenuX-menuRadius)<2){
                        mScroller.startScroll((int) mainMenuX, (int) mainMenuY, (int) -menuRadius, 0);
                        postInvalidate();
                    }else if(Math.abs(mainMenuX-getRight()+menuRadius)<2){
                        mScroller.startScroll((int) mainMenuX, (int) mainMenuY, (int) menuRadius, 0);
                        postInvalidate();
                    }else if(Math.abs(mainMenuY-menuRadius)<2){
                        mScroller.startScroll((int) mainMenuX, (int) mainMenuY, 0, (int) -menuRadius);
                        postInvalidate();
                    }else if(Math.abs(mainMenuY-getBottom()+menuRadius)<2){
                        mScroller.startScroll((int) mainMenuX, (int) mainMenuY, 0, (int) menuRadius);
                        postInvalidate();
                    }
                    break;
            }
        }
    };

    private void onClickEvent() {
        if(mMenuViews.get(0).getVisibility()==View.GONE){
            showMenu( mMenuViews.get(0),imageMain.getX(),imageMain.getY(),imageMain.getX()+200,imageMain.getY()+200);
        }else{
            hiddenMenu( mMenuViews.get(0),mMenuViews.get(0).getX(),mMenuViews.get(0).getY(),imageMain.getX(),imageMain.getY());
        }
    }

    private void hiddenMenu(ImageView imageView,float startX,float startY,float endX,float endY){
        doMoving(imageView,startX,startY,endX,endY,false);
    }

    private void showMenu(ImageView imageView,float startX,float startY,float endX,float endY) {
        doMoving(imageView,startX,startY,endX,endY,true);
    }

    private void doMoving(ImageView imageView,float startX,float startY,float endX,float endY,final boolean isShowMenu) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(imageView, "translationX", startX, endX);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(imageView, "translationY", startY, endY);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.setDuration(1000);
        set.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation) {
                if (isShowMenu) {
                    for(ImageView imageView:mMenuViews){
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShowMenu) {
                    for(ImageView imageView:mMenuViews){
                        imageView.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        set.start();
    }

    private void moveMainMenu(MotionEvent event) {
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
        downX = event.getRawX();
        downY = event.getRawY();
        float x = lastX;
        float y = lastY;
        lastDownTime = event.getDownTime();

        int left = imageMain.getLeft();
        int top = imageMain.getTop();
        int right = imageMain.getRight();
        int bottom = imageMain.getBottom();
        if(x>left&&x<right&&y>top&&y<bottom){
            return true;
        }
        return false;
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

    private float getMainMenuX() {
        return imageMain.getX()+imageMain.getWidth()/2;
    }

    private float getMainMenuY() {
        return imageMain.getY()+imageMain.getHeight()/2;
    }

}
