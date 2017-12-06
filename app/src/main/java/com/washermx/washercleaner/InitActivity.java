package com.washermx.washercleaner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washercleaner.model.AppData;
import com.washermx.washercleaner.model.Database.DataBase;
import com.washermx.washercleaner.model.ProfileReader;
import com.washermx.washercleaner.model.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.washermx.washercleaner.model.Versiones;

public class InitActivity extends AppCompatActivity {

    SharedPreferences settings;

    private static final int ACCESS_FINE_LOCATION = 1;
    private static final int INTERNET = 2;
    private static final int VIBRATE = 3;
    private boolean allPermissionsOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();
        settings = getSharedPreferences(AppData.FILE,0);
        initView();
        reviewPermissions();
    }

    private void reviewPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},INTERNET);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, VIBRATE);
        } else if (settings.getString(AppData.APP_VERSION,null) == null){
            createAlertTerms();
        } else {
            allPermissionsOk = true;
            settings = getSharedPreferences(AppData.FILE, 0);
            String token = settings.getString(AppData.TOKEN, null);
            new Iniciar().execute(token);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],@NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!allPermissionsOk)
                reviewPermissions();
        } else {
            createAlert();
        }
    }

    private void createAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Permission denied")
                .setMessage(getResources().getString(R.string.app_name) + " Can't work without this permission")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void createAlertTerms() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.terms_alert))
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        AppData.saveVersion(settings,"1");
                        reviewPermissions();
                    }
                })
                .show();
    }

    private void initView() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) optionsTitleBar.hide();
    }

    private void leerUsuario(String token) throws ProfileReader.errorReadingProfile, User.noSessionFound, User.errorSavingFireBaseToken {
        ProfileReader.run(getBaseContext());
        DataBase db = new DataBase(getBaseContext());
        User user = db.readUser();
        user.token = token;
        AppData.saveData(settings,user);
        String fireBaseToken = settings.getString(AppData.FB_TOKEN,"");
        user.saveFirebaseToken(token, fireBaseToken);
    }

    private void postAlert(final String message)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(),activity);
        startActivity(intent);
        finish();
    }

    void mostrarNuevaActualizacion() {
        TextView tv  = new TextView(this);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setGravity(Gravity.CENTER);
        tv.setText(Html.fromHtml("<a href=https://play.google.com/store/apps/details?id=com.washermx.washercleaner>" + getString(R.string.mensaje_nueva_version) +"</a>"));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.actualizar_titulo))
                .setView(tv)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private class Iniciar extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... valores) {
            String token = valores[0];
            try {
                Versiones.leerVersion();
                if (token == null) {
                    return "token";
                } else {
                    leerUsuario(token);
                    return "ok";
                }
            } catch (Versiones.actualizacionRequerida e) {
                return "version";
            } catch (User.errorSavingFireBaseToken | User.noSessionFound | ProfileReader.errorReadingProfile e) {
                return "error";
            }
        }

        // Se ejecuta despues de doInBackground en el thread principal
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            switch (result) {
                case "ok":
                    changeActivity(MapActivity.class);
                    break;
                case "error":
                    postAlert(getString(R.string.error_sesion));
                    ProfileReader.delete(getBaseContext());
                    changeActivity(MainActivity.class);
                    break;
                case "token":
                    changeActivity(MainActivity.class);
                    break;
                case "version":
                    mostrarNuevaActualizacion();
                    break;
            }
        }
    }
}
