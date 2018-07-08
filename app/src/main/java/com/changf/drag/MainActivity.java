package com.changf.drag;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.android.permission.FloatWindowManager;
import com.changf.drag.bak.service.FloatingWindowService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatWindowManager.getInstance().applyOrShowFloatWindow(getApplicationContext());
            }
        });
    }
}
