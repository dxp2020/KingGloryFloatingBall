package com.changf.drag.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.changf.drag.R;

public class SubMenu extends LinearLayout {
    private ImageView menuView;
    private TextView menuText;

    public SubMenu(Context context) {
        this(context,null);
    }

    public SubMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        menuView = new ImageView(context);
        menuView.setImageResource(R.mipmap.suspend_1);

        menuText = new TextView(context);
        menuText.setText("通知");
        menuText.setTextColor(Color.WHITE);
        menuText.setRotation(90);
        menuText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,10);

        addView(menuText);
        addView(menuView);
    }

    public void setImageResource(int resId) {
        menuView.setImageResource(resId);
    }

    public void setText(String text) {
        menuText.setText(text);
    }

}
