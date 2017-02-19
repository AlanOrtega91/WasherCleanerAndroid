package com.washermx.washercleaner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washercleaner.model.AppData;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText bEmail;
    EditText bPassword;
    private Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences settings;
    public static final String EMAIL = "EMAIL";
    public static final String PASSWORD = "PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        settings = getSharedPreferences(AppData.FILE, 0);
        bEmail = (EditText) findViewById(R.id.email);
        bPassword = (EditText) findViewById(R.id.password);
        configureActionBar();
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_options);
            Toolbar parent =(Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }
        TextView leftButton = (TextView)findViewById(R.id.leftButtonOptionsTitlebar);
        TextView rightButton = (TextView)findViewById(R.id.rightButtonOptionsTitlebar);
        TextView title = (TextView)findViewById(R.id.titleOptionsTitlebar);
        leftButton.setText(R.string.cancel);
        rightButton.setText(R.string.ok);
        title.setText(R.string.log_in_title);
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    public void sendLogIn() {
        try {
            String email = bEmail.getText().toString();
            String password = bPassword.getText().toString();
            reviewCredentials(email, password);
            changeActivity(LoadingActivity.class,email,password);
        } catch (invalidCredentialsMail e) {
            postAlert(getResources().getString(R.string.error_invalid_email));
        } catch (invalidCredentialsPassword e) {
            postAlert(getResources().getString(R.string.error_invalid_password));
        }
    }

    private void reviewCredentials(String email, String password) throws invalidCredentialsMail, invalidCredentialsPassword {
        if (email == null || !email.contains("@"))
            throw new invalidCredentialsMail();
        if (password == null || password.length() < 6) {
            throw new invalidCredentialsPassword();
        }
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeActivity(Class activity, String email, String password) {
        Intent intent = new Intent(getBaseContext(), activity);
        intent.putExtra(EMAIL,email);
        intent.putExtra(PASSWORD,password);
        startActivity(intent);
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButtonOptionsTitlebar:
                onBackPressed();
                break;
            case R.id.rightButtonOptionsTitlebar:
                sendLogIn();
                break;
        }
    }

    public void onClickForgotPassword(View view) {
        changeActivity(ForgotPassword.class);
    }

    @Override
    public void onBackPressed() {
        finish();
        changeActivity(MainActivity.class);
    }

    private class invalidCredentialsMail extends Throwable {
    }
    private class invalidCredentialsPassword extends Throwable {
    }
}
