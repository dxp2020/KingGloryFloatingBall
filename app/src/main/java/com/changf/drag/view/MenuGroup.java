package com.changf.drag.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.changf.drag.R;
import com.changf.drag.model.Direction;

import java.util.ArrayList;
import java.util.List;

public class MenuGroup extends ViewGroup{
    private static final String TAG = FloatingViewGroup.class.getSimpleName();

    private boolean isInitLocation;//是否初始化了位置

    private ImageView imageMain;
    //菜单
    private List<SubMenu> mMenuViews = new ArrayList<>();
    //移动距离
    private static int moveDistanse = 250;
    //张开方向
    private Direction direction = Direction.LEFT;

    public MenuGroup(Context context) {
        this(context,null);
    }

    public MenuGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        imageMain = new ImageView(context);
        imageMain.setImageResource(R.mipmap.icon_suspend_main);
        addView(imageMain);

        SubMenu menuView = new SubMenu(context);
        menuView.setImageResource(R.mipmap.suspend_1);
        menuView.setText("通知");

        SubMenu menuView2 = new SubMenu(context);
        menuView2.setImageResource(R.mipmap.suspend_2);
        menuView2.setText("福利");

        SubMenu menuView3 = new SubMenu(context);
        menuView3.setImageResource(R.mipmap.suspend_3);
        menuView3.setText("录屏");

        SubMenu menuView4 = new SubMenu(context);
        menuView4.setImageResource(R.mipmap.suspend_4);
        menuView4.setText("设置");

        mMenuViews.add(menuView);
        mMenuViews.add(menuView2);
        mMenuViews.add(menuView3);
        mMenuViews.add(menuView4);

        addView(menuView);
        addView(menuView2);
        addView(menuView3);
        addView(menuView4);

//        imageMain.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(isShowing()){
//                    hiddenMenu();
//                }else {
//                    showMenu();
//                }
//            }
//        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int count = getChildCount();
        for(int i=0;i<count;i++){
            View view = getChildAt(i);
            measureChild(view,widthMeasureSpec,heightMeasureSpec);
        }
        int menuGroupWidth = 2*moveDistanse+mMenuViews.get(0).getMeasuredWidth();
        int menuGroupHeight = menuGroupWidth;
        setMeasuredDimension(menuGroupWidth,menuGroupHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(!isInitLocation){
            isInitLocation = true;
            int width = imageMain.getMeasuredWidth();
            int height = imageMain.getMeasuredHeight();

            int centerX = (r-l)/2;
            int centerY = (b-t)/2;
            int left = centerX-width/2;
            int right = centerX+width/2;
            int top = centerY-height/2;
            int bottom = centerY+height/2;

            imageMain.layout(left,top,right,bottom);

            for(SubMenu imageView:mMenuViews){
                int w = imageView.getMeasuredWidth();
                int h = imageView.getMeasuredHeight();
                imageView.layout(0,0,w,h);
                imageView.setVisibility(View.GONE);
            }

//            for(SubMenu imageView:mMenuViews){
//                int w = imageView.getMeasuredWidth();
//                int h = imageView.getMeasuredHeight();
//                int cx = (r-l)/2;
//                int cy = (b-t)/2;
//
//                imageView.layout(cx-w/2,cy-h/2,cx+w/2,cy+h/2);
//                imageView.setVisibility(View.GONE);
//            }
        }
    }

    /**
     * 必须返回true，不然会传递到父View的onTouch
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void showMenu() {
        float startX = imageMain.getX()-(mMenuViews.get(0).getMeasuredWidth()-imageMain.getMeasuredWidth())/2;
        float startY = imageMain.getY()-(mMenuViews.get(0).getMeasuredHeight()-imageMain.getMeasuredHeight())/2;
        //左边
        if(direction == Direction.LEFT){
            int[] angles = {85,30,-30,-85};
            showMenu(angles,startX,startY,moveDistanse);
            //上边
        }else if(direction == Direction.TOP){
            int[] angles = {-5,-60,-120,-175};
            showMenu(angles,startX,startY,moveDistanse);
            //右边
        }else if(direction == Direction.RIGHT){
            int[] angles = {-95,-150,150,95};
            showMenu(angles,startX,startY,moveDistanse);
            //下边
        }else if(direction == Direction.BOTTOM){
            int[] angles = {175,120,60,5};
            showMenu(angles,startX,startY,moveDistanse);
        }else{
            Log.e(TAG,"没有显示成功");
        }
    }

    public void hiddenMenu() {
        float startX = imageMain.getX()-(mMenuViews.get(0).getMeasuredWidth()-imageMain.getMeasuredWidth())/2;
        float startY = imageMain.getY()-(mMenuViews.get(0).getMeasuredHeight()-imageMain.getMeasuredHeight())/2;
        //左边
        if(direction == Direction.LEFT){
            int[] angles = {70,30,-30,-70};
            hiddenMenu(angles,startX,startY,moveDistanse);
            //上边
        }else if(direction == Direction.TOP){
            int[] angles = {-20,-60,-120,-160};
            hiddenMenu(angles,startX,startY,moveDistanse);
            //右边
        }else if(direction == Direction.RIGHT){
            int[] angles = {-110,-150,150,110};
            hiddenMenu(angles,startX,startY,moveDistanse);
            //下边
        }else if(direction == Direction.BOTTOM){
            int[] angles = {160,120,60,20};
            hiddenMenu(angles,startX,startY,moveDistanse);
        }else{
            Log.e(TAG,"没有隐藏成功");
        }
    }

    private void showMenu(int[] angles,float startX,float startY,float moveDistanse){
        float endX;
        float endY;
        for(int i=0;i<mMenuViews.size();i++){
            SubMenu iv = mMenuViews.get(i);
            endX = startX+moveDistanse*sin(90-angles[i])/sin(90) ;
            endY = startY-moveDistanse*sin(angles[i])/sin(90) ;
            showMenu(iv,startX,startY,endX,endY);
        }
    }

    private void hiddenMenu(int[] angles,float startX,float startY,float moveDistanse){
        float endX;
        float endY;
        for(int i=0;i<mMenuViews.size();i++){
            SubMenu iv = mMenuViews.get(i);
            endX = startX+moveDistanse*sin(90-angles[i])/sin(90) ;
            endY = startY-moveDistanse*sin(angles[i])/sin(90) ;
            hiddenMenu(iv,endX,endY,startX,startY);
        }
    }

    private void showMenu(SubMenu imageView,float startX,float startY,float endX,float endY) {
        doMoving(imageView,startX,startY,endX,endY,true);
    }

    private void hiddenMenu(SubMenu imageView,float startX,float startY,float endX,float endY){
        doMoving(imageView,startX,startY,endX,endY,false);
    }

    private void doMoving(final SubMenu imageView,float startX,float startY,float endX,float endY,final boolean isShowMenu) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(imageView, "translationX", startX, endX);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(imageView, "translationY", startY, endY);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.setDuration(300);
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

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public View getMainMenu() {
        return imageMain;
    }

    /**
     * 是否显示
     * @return
     */
    public boolean isShowing(){
        return mMenuViews.get(0).getVisibility()==View.VISIBLE;
    }

}
