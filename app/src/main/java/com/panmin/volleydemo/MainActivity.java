package com.panmin.volleydemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.panmin.volleydemo.httpentity.AjaxCallBack;

public class MainActivity extends AppCompatActivity {

    private Button btn_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_test = (Button) findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpManager.getInstance(MainActivity.this).getOkHttp().get("http://www.baidu.com", new AjaxCallBack<String>() {
                    @Override
                    public void onSuccess(String s) {

                    }

                    @Override
                    public void onFailure(int errorNo, String errMsg) {
                        super.onFailure(errorNo, errMsg);
                    }
                });
            }
        });
    }
}
