package com.changf.drag.view;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import com.changf.drag.R;
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

        ImageView menuView2 = new ImageView(context);
        menuView2.setImageResource(R.mipmap.suspend_2);

        ImageView menuView3 = new ImageView(context);
        menuView3.setImageResource(R.mipmap.suspend_3);

        ImageView menuView4 = new ImageView(context);
        menuView4.setImageResource(R.mipmap.suspend_4);

        mMenuViews.add(menuView);
        mMenuViews.add(menuView2);
        mMenuViews.add(menuView3);
        mMenuViews.add(menuView4);

        addView(menuView);
        addView(menuView2);
        addView(menuView3);
        addView(menuView4);
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

    private void onClickEvent() {
        if(mMenuViews.get(0).getVisibility()==View.GONE){
            showMenu();
            handler.sendEmptyMessage(1);
        }else{
            hiddenMenu();
            handler.sendEmptyMessageDelayed(0,750);
        }
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
            if(!isHidden&&!isShowingMenu()){
                handler.sendEmptyMessageDelayed(0,duration);
            }
        }
    }

    private boolean isShowingMenu(){
        if(mMenuViews.size()>0&&mMenuViews.get(0).getVisibility()==View.VISIBLE){
            return true;
        }
        return false;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //小球收缩进屏幕
                case 0:
                    hiddenMainMenu();
                    break;
                //小球从屏幕中弹出
                case 1:
                    showMainMenu();
                    break;
            }
        }
    };

    private void showMainMenu() {
        isHidden = false;
        float startX = getMainMenuX();
        float startY = getMainMenuY();
        if(imageMain.getLeft()<getLeft()){
            mScroller.startScroll((int) startX, (int) startY, (int) menuRadius, 0);
            invalidate();
        }else if(imageMain.getRight()>getRight()){
            mScroller.startScroll((int) startX, (int) startY, (int) -menuRadius, 0);
            invalidate();
        }else if(imageMain.getTop()<getTop()){
            mScroller.startScroll((int) startX, (int) startY, 0, (int) menuRadius);
            invalidate();
        }else if(imageMain.getBottom()>getBottom()){
            mScroller.startScroll((int) startX, (int) startY, 0, (int) -menuRadius);
            invalidate();
        }
    }

    private void hiddenMainMenu() {
        isHidden = true;
        float startX = getMainMenuX();
        float startY = getMainMenuY();

        if(Math.abs(imageMain.getLeft()-getLeft())<2){
            mScroller.startScroll((int) startX, (int) startY, (int) -menuRadius, 0);
            invalidate();
        }else if(Math.abs(imageMain.getRight()-getRight())<2){
            mScroller.startScroll((int) startX, (int) startY, (int) menuRadius, 0);
            invalidate();
        }else if(Math.abs(imageMain.getTop()-getTop())<2){
            mScroller.startScroll((int) startX, (int) startY, 0, (int) -menuRadius);
            invalidate();
        }else if(Math.abs(imageMain.getBottom()-getBottom())<2){
            mScroller.startScroll((int) startX, (int) startY, 0, (int) menuRadius);
            invalidate();
        }
    }

    private void showMenu(int[] angles,float startX,float startY,float moveDistanse){
        float endX;
        float endY;
        for(int i=0;i<mMenuViews.size();i++){
            ImageView iv = mMenuViews.get(i);
            endX = startX+moveDistanse*sin(90-angles[i])/sin(90) ;
            endY = startY-moveDistanse*sin(angles[i])/sin(90) ;
            showMenu(iv,startX,startY,endX,endY);
        }
    }

    private void hiddenMenu(int[] angles,float startX,float startY,float moveDistanse){
        float endX;
        float endY;
        for(int i=0;i<mMenuViews.size();i++){
            ImageView iv = mMenuViews.get(i);
            endX = startX+moveDistanse*sin(90-angles[i])/sin(90) ;
            endY = startY-moveDistanse*sin(angles[i])/sin(90) ;
            hiddenMenu(iv,endX,endY,startX,startY);
        }
    }

    private void showMenu() {
        float startX = imageMain.getX();
        float startY = imageMain.getY();
        float moveDistanse = ringSideRaduis;
        //左边
        if(imageMain.getLeft()<getLeft()){
            int[] angles = {70,30,-30,-70};
            showMenu(angles,startX,startY,moveDistanse);
        //上边
        }else if(imageMain.getTop()<getTop()){
            int[] angles = {-20,-60,-120,-160};
            showMenu(angles,startX,startY,moveDistanse);
        //右边
        }else if(imageMain.getRight()>getRight()){
            int[] angles = {-110,-150,150,110};
            showMenu(angles,startX,startY,moveDistanse);
        //下边
        }else if(imageMain.getBottom()>getBottom()){
            int[] angles = {160,120,60,20};
            showMenu(angles,startX,startY,moveDistanse);
        }
    }

    private void hiddenMenu() {
        float startX = imageMain.getX();
        float startY = imageMain.getY();
        float moveDistanse = ringSideRaduis;
        //左边
        if(imageMain.getLeft()<=getLeft()){
            int[] angles = {70,30,-30,-70};
            hiddenMenu(angles,startX,startY,moveDistanse);
            //上边
        }else if(imageMain.getTop()<=getTop()){
            int[] angles = {-20,-60,-120,-160};
            hiddenMenu(angles,startX,startY,moveDistanse);
            //右边
        }else if(imageMain.getRight()>=getRight()){
            int[] angles = {-110,-150,150,110};
            hiddenMenu(angles,startX,startY,moveDistanse);
            //下边
        }else if(imageMain.getBottom()>=getBottom()){
            int[] angles = {160,120,60,20};
            hiddenMenu(angles,startX,startY,moveDistanse);
        }
    }

    private void hiddenMenu(ImageView imageView,float startX,float startY,float endX,float endY){
        doMoving(imageView,startX,startY,endX,endY,false);
    }

    private void showMenu(ImageView imageView,float startX,float startY,float endX,float endY) {
        doMoving(imageView,startX,startY,endX,endY,true);
    }

    private void doMoving(final ImageView imageView,float startX,float startY,float endX,float endY,final boolean isShowMenu) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(imageView, "translationX", startX, endX);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(imageView, "translationY", startY, endY);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.setDuration(500);
        set.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation) {
                if (isShowMenu) {
                    imageView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShowMenu) {
                    imageView.setVisibility(View.GONE);
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        set.start();
    }

    private float sin(double angle){
        return (float) Math.sin(angle*Math.PI/180);
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
        return imageMain.getX()+menuRadius;
    }

    private float getMainMenuY() {
        return imageMain.getY()+menuRadius;
    }

}
