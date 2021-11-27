package com.me.client.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.me.client.R;
import com.me.client.common.RxTools;
import com.me.client.core.FtpCore;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * 登录界面
 * */
public class LoginActivity extends AppCompatActivity {
    private EditText ipEt;
    private EditText userNameEt;
    private EditText pwdEt;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ipEt = findViewById(R.id.ipEt);
        userNameEt = findViewById(R.id.userNameEt);
        pwdEt = findViewById(R.id.pwdEt);
        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = ipEt.getText().toString().trim();
                String userName = userNameEt.getText().toString().trim();
                String pwd = pwdEt.getText().toString().trim();
                if(TextUtils.isEmpty(ip)){
                    Toast.makeText(LoginActivity.this,"please input host ip",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(LoginActivity.this,"please input username",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(pwd)){
                    Toast.makeText(LoginActivity.this,"please input password",Toast.LENGTH_SHORT).show();
                    return;
                }
                Disposable d = Observable.just("")
                    .map(new Function<String, String>() {
                        @Override
                        public String apply(String s) throws Exception {
                            return FtpCore.instance().login(ip,userName,pwd);
                        }
                    }).compose(RxTools.oIoMain())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String code) throws Exception {
                            if ("501".equals(code)) {
                                Toast.makeText(LoginActivity.this, "invalid username",
                                        Toast.LENGTH_SHORT).show();
                            } else if("530".equals(code)){
                                Toast.makeText(LoginActivity.this, "wrong password,please try again",
                                        Toast.LENGTH_SHORT).show();
                            } else if("1000".equals(code)){
                                Toast.makeText(LoginActivity.this, "service unavailable,you can try kill server app and restart",
                                        Toast.LENGTH_LONG).show();
                            } else if("".equals(code)){
                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            } else {
                                Toast.makeText(LoginActivity.this, "unknown error",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        });
    }
}