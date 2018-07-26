package com.changf.drag;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.changf.drag.view.FloatingViewGroup;
import com.changf.drag.view.MenuGroup;

public class MainActivityTest2 extends Activity {

    private FloatingViewGroup fwFloatingWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test2);
        fwFloatingWindow = findViewById(R.id.fw_floating_window);
        fwFloatingWindow.setMenuItemClickListener(new MenuGroup.OnMenuItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position){
                    case 0:
                        Toast.makeText(MainActivityTest2.this,"通知",Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(MainActivityTest2.this,"福利",Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(MainActivityTest2.this,"录屏",Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        Toast.makeText(MainActivityTest2.this,"设置",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }



}
