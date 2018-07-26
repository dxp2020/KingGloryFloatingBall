package com.changf.drag.view;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.changf.drag.model.Direction;
import com.changf.drag.utils.ViewUtils;

public class FloatingViewGroup extends ViewGroup {
    private static final String TAG = FloatingViewGroup.class.getSimpleName();
    //menu收缩到边框内
    private static final int HIDDEN_MENU = 0;
    //menu从边框弹出
    private static final int POP_MENU = 1;
    //自动隐藏Menu（收缩到边框内、并隐藏子menu）
    private static final int AUTO_HIDDEN = 2;

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
    //menuGroup的半径
    private static int menuGroupRadius;
    //mainMenu的半径
    private static int menuRadius;

    //滚动工具类
    private Scroller mScroller;
    //是否在滚动
    private boolean isScrolling = false;
    //是否隐藏
    private boolean isHidden = true;
    //是否显示了子菜单
    private boolean isShowingSubMenu = false;
    //小球隐藏时间
    private long duration = 250;
    //在被判定为滚动之前用户手指可以移动的最大值。
    private int touchSlop;
    //菜单
    private MenuGroup menuGroup;

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

        menuGroup = new MenuGroup(context);
        addView(menuGroup);
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
            menuGroupRadius = menuGroup.getMeasuredWidth()/2;
            menuRadius = menuGroup.getMainMenu().getMeasuredWidth()/2;
            centerX = (r-l)/2;
            centerY = (b-t)/2;
            intevalY = ViewUtils.getScreenHeight(getContext())-b;

            int width = menuGroup.getMeasuredWidth();
            int left = -width/2;
            int right = width/2;
            int top = (int) (-width/2+centerX);
            int bottom = (int) (width/2+centerX);
            menuGroup.layout(left,top,right,bottom);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //触控区域下面没有子View的情况下，DOWN事件也会回传到父View的ontouch,此时返回false，屏蔽掉后续的action
                return isImageMainArea(event);
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(downX-event.getRawX())>touchSlop||Math.abs(downY-event.getRawY())>touchSlop) {
                    moveMainMenu(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isClickEvent(downX, downY, event.getRawX(), event.getRawY(), lastDownTime, event.getEventTime(), shortClickTime)){
                    onClickEvent();
                }else{
                    attachedToSide();
                }
                break;
        }
        return true;
    }

    private void onClickEvent() {
        //移除HIDDEN_MENU事件
        handler.removeMessages(HIDDEN_MENU);
        //移除AUTO_HIDDEN事件
        handler.removeMessages(AUTO_HIDDEN);

        if(menuGroup.isShowing()){
            hiddenMenu();
            handler.sendEmptyMessageDelayed(HIDDEN_MENU,750);
        }else{
            showMenu();
            //小球从屏幕中弹出
            handler.sendEmptyMessage(POP_MENU);
            //设置自动隐藏事件
            handler.sendEmptyMessageDelayed(AUTO_HIDDEN,2500);
        }
    }

    private void showMenu() {
        isShowingSubMenu = true;
        menuGroup.setDirection(getDirection());
        menuGroup.showMenu();
    }

    private void hiddenMenu() {
        isShowingSubMenu = false;
        menuGroup.hiddenMenu();
    }

    private Direction getDirection(){
        if(menuGroup.getTop()<0){
            return Direction.TOP;
        }else if(menuGroup.getLeft()<0){
            return Direction.LEFT;
        }else if(menuGroup.getRight()>getRight()){
            return Direction.RIGHT;
        }else if(menuGroup.getBottom()>getBottom()){
            return Direction.BOTTOM;
        }
        return null;
    }

    private Direction getDirection(float x,float y){
        if(y<centerY&&((x<centerX&&y<x)||(x>centerX&&y<(getWidth()-x)))){
            return Direction.TOP;
        }else if(y>centerY&&((x<centerX&&(getBottom()-y)<x)||(x>centerX&&((getBottom()-y)<(getWidth()-x))))){
            return Direction.BOTTOM;
        }else if(x<centerX){
            return Direction.LEFT;
        }else if(x>centerX){
            return Direction.RIGHT;
        }else {
            return null;
        }
    }

    private void attachedToSide() {
        isHidden = false;
        attachedToSide(getMainMenuCenterX(),getMainMenuCenterY());
    }

    private void attachedToSide(float x,float y) {
        Direction direction = getDirection(x,y);
        //当小球移动到左上部分、右上部分的时候
        if(direction==Direction.TOP){
            //吸附到顶部
            if((x-menuGroupRadius)>0&&(x+menuGroupRadius)<getWidth()){
                mScroller.startScroll((int) x, (int) y, 0, (int) -(y - menuRadius));
                invalidate();
                return;
            }else if(x<centerX){
                mScroller.startScroll((int) x, (int) y, (int) -(x-menuGroupRadius), (int) -(y - menuRadius));
                invalidate();
                return;
            }else if(x>centerX){
                mScroller.startScroll((int) x, (int) y, (int) (getWidth()-x-menuGroupRadius), (int) -(y - menuRadius));
                invalidate();
                return;
            }
            //小球移动到左下部分、右下部分的时候
        }else if(direction==Direction.BOTTOM){
            //吸附到底部
            if((x-menuGroupRadius)>0&&(x+menuGroupRadius)<getWidth()){
                mScroller.startScroll((int) x, (int) y, 0, (int) (getBottom()-y - menuRadius));
                invalidate();
                return;
            }else if(x<centerX){
                mScroller.startScroll((int) x, (int) y, (int) -(x-menuGroupRadius),(int) (getBottom()-y - menuRadius));
                invalidate();
                return;
            }else if(x>centerX){
                mScroller.startScroll((int) x, (int) y, (int) (getWidth()-x-menuGroupRadius),(int) (getBottom()-y - menuRadius));
                invalidate();
                return;
            }
            return;
        }
        if(direction==Direction.LEFT){
            if(y<centerY&&y<menuGroupRadius) {
                //吸附到左上角
                mScroller.startScroll((int) x, (int) y, (int) -(x - menuRadius), (int) -(y - menuGroupRadius));
            }else if(y>centerY&&(getBottom()-y)<menuGroupRadius){
                //吸附到左下角
                mScroller.startScroll((int) x, (int) y, (int) -(x - menuRadius), (int) (getBottom()-y-menuGroupRadius));
            }else{
                //吸附到左边
                mScroller.startScroll((int) x,(int)y, (int) -(x-menuRadius), 0);
            }
        }else if(direction==Direction.RIGHT){
            if(y<centerY&&y<menuGroupRadius) {
                //吸附到左上角
                mScroller.startScroll((int) x, (int) y, (int) (getWidth()-x-menuRadius), (int) -(y - menuGroupRadius));
            }else if(y>centerY&&(getBottom()-y)<menuGroupRadius){
                //吸附到左下角
                mScroller.startScroll((int) x, (int) y, (int) (getWidth()-x-menuRadius), (int) (getBottom()-y-menuGroupRadius));
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
            menuGroup.offsetLeftAndRight(mScroller.getCurrX()-getMainMenuCenterX());
            menuGroup.offsetTopAndBottom(mScroller.getCurrY()-getMainMenuCenterY());
            postInvalidate();
        }else{
            isScrolling = false;
            if(isHiddenMenu()){
                //移除AUTO_HIDDEN事件
                handler.removeMessages(AUTO_HIDDEN);
                handler.sendEmptyMessageDelayed(HIDDEN_MENU,duration);
            }
        }
    }

    private boolean isHiddenMenu() {
        //子菜单显示的时候，不自动隐藏
        if(menuGroup.isShowing()){
            return false;
        }
        Direction direction = getDirection();
        float left = getMainMenuLeft();
        float top = getMainMenuTop();
        float right = getMainMenuRight();
        float bottom = getMainMenuBottom();
        if(direction == Direction.LEFT){
            return left>=getLeft();
        }else if(direction == Direction.TOP){
            return top>=getTop();
        }else if(direction == Direction.RIGHT){
            return right<=getRight();
        }else if(direction == Direction.BOTTOM){
            return bottom<=getBottom();
        }
        return false;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //小球收缩进屏幕
                case HIDDEN_MENU:
//                    Log.e(TAG,"HIDDEN_MENU");
                    hiddenMainMenu();
                    break;
                //小球从屏幕中弹出
                case POP_MENU:
//                    Log.e(TAG,"POP_MENU");
                    showMainMenu();
                    break;
                case AUTO_HIDDEN:
//                    Log.e(TAG,"AUTO_HIDDEN");
                    hiddenMainMenu();
                    hiddenMenu();
                    break;
            }
        }
    };

    private void showMainMenu() {
        isHidden = false;
        int startX = getMainMenuCenterX();
        int startY = getMainMenuCenterY();
        Direction direction = getDirection();
        if(direction==Direction.LEFT){
            mScroller.startScroll(startX, startY, menuRadius-startX, 0);
            invalidate();
        }else if(direction==Direction.RIGHT){
            mScroller.startScroll(startX, startY, getRight()-startX-menuRadius, 0);
            invalidate();
        }else if(direction==Direction.TOP){
            mScroller.startScroll(startX, startY, 0, menuRadius-startY);
            invalidate();
        }else if(direction==Direction.BOTTOM){
            mScroller.startScroll(startX, startY, 0, getBottom()-startY-menuRadius);
            invalidate();
        }
    }

    private void hiddenMainMenu() {
        isHidden = true;
        int startX = getMainMenuCenterX();
        int startY = getMainMenuCenterY();
        Direction direction = getDirection();
        if(direction==Direction.LEFT){
            mScroller.startScroll(startX, startY, -startX, 0);
            invalidate();
        }else if(direction==Direction.RIGHT){
            mScroller.startScroll(startX, startY, getRight()-startX, 0);
            invalidate();
        }else if(direction==Direction.TOP){
            mScroller.startScroll(startX, startY, 0, -startY);
            invalidate();
        }else if(direction==Direction.BOTTOM){
            mScroller.startScroll(startX, startY, 0, getBottom()-startY);
            invalidate();
        }
    }

    private void moveMainMenu(MotionEvent event) {
        if(isShowingSubMenu){
            isShowingSubMenu = false;
            hiddenMenu();
            //移除自动隐藏事件
            handler.removeMessages(AUTO_HIDDEN);
        }
        int intevalX = (int) (event.getX()-lastX);
        int intevalY = (int) (event.getY()-lastY);
        lastX = event.getX();
        lastY = event.getY();
        menuGroup.offsetLeftAndRight(intevalX);
        menuGroup.offsetTopAndBottom(intevalY);
    }

    private boolean isImageMainArea(MotionEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        downX = event.getRawX();
        downY = event.getRawY();
        float x = lastX;
        float y = lastY;
        lastDownTime = event.getDownTime();

        int left = getMainMenuLeft();
        int top = getMainMenuTop();
        int right = getMainMenuRight();
        int bottom = getMainMenuBottom();
        if(x>left&&x<right&&y>top&&y<bottom){
            return true;
        }
        return false;
    }

    private int getMainMenuLeft(){
        return menuGroup.getLeft()+menuGroup.getMainMenu().getLeft();
    }

    private int getMainMenuTop(){
        return menuGroup.getTop()+menuGroup.getMainMenu().getTop();
    }

    private int getMainMenuRight(){
        return getMainMenuLeft()+menuGroup.getMainMenu().getMeasuredWidth();
    }

    private int getMainMenuBottom(){
        return getMainMenuTop()+menuGroup.getMainMenu().getMeasuredHeight();
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

    private int getMainMenuCenterX() {
        return getMainMenuLeft()+menuRadius;
    }

    private int getMainMenuCenterY() {
        return getMainMenuTop()+menuRadius;
    }

    public void setMenuItemClickListener(final MenuGroup.OnMenuItemClickListener onMenuItemClickListener) {
        menuGroup.setMenuItemClickListener(new MenuGroup.OnMenuItemClickListener(){
            @Override
            public void onItemClick(int position) {
                if(onMenuItemClickListener!=null){
                    onMenuItemClickListener.onItemClick(position);
                }
                //移除AUTO_HIDDEN事件
                handler.removeMessages(AUTO_HIDDEN);
                //设置自动隐藏事件
                handler.sendEmptyMessageDelayed(AUTO_HIDDEN,2500);
            }
        });
    }

}
