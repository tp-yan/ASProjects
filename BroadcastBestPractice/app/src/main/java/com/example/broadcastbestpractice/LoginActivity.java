package com.example.broadcastbestpractice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {
    EditText username, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        username = findViewById(R.id.username);
        pass = findViewById(R.id.password);
    }

    public void login(View view) {
        String name = String.valueOf(username.getText());
        String pwd = String.valueOf(pass.getText());
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pwd)) {
            if ("admin".equals(name) && "123456".equals(pwd)) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Toast.makeText(this, "用户或密码错误！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "请填写用户名和密码", Toast.LENGTH_SHORT).show();
        }
    }
}
