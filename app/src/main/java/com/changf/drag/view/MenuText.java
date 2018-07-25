package com.changf.drag.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class MenuText extends View {
    private String content;
    private Paint paint;
    private int textGap = dp2px(2);

    public MenuText(Context context) {
        this(context, null);
    }

    public MenuText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!TextUtils.isEmpty(content)) {
            Rect rect = new Rect();
            paint.getTextBounds(content,0,content.length(), rect);
            setMeasuredDimension(rect.height()+2*textGap,rect.width()+2*textGap);
        }
    }

    private void init() {
        paint = new Paint();
        paint.setTextSize(dp2px(10));
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setText(String content) {
        this.content = content;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (!TextUtils.isEmpty(content)) {
            canvas.rotate(90);
            canvas.drawText(content,  textGap,-1.5f*textGap, paint);
        }
    }

    public  int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp*density+0.5);
    }

    public void setTextColor(int white) {
        paint.setColor(white);
        invalidate();
    }

    public void setTextSize(int dp) {
        paint.setTextSize(dp2px(dp));
        invalidate();
    }
}