package com.luis.bioref;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends Activity {

    Button login, signin;
    private EditText username, password, email;
    private ProgressDialog pDialog;
    int flag = 0;
    JSONParser jsonParser = new JSONParser();
    private static String url = "http://192.168.1.6/android_connect/add_user.php";
    private static final String TAG_SUCCESS = "success";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);

            }
        });
        signin = (Button) findViewById(R.id.signin);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        email = (EditText) findViewById(R.id.email);

        signin.setOnClickListener(new View.OnClickListener() {
            //alterado de 10 para 6
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (username.length() < 4) {
                    Toast.makeText(RegisterActivity.this, "Please Enter correct username", Toast.LENGTH_LONG).show();
                    return;
                }
                //alterado de 4 para 8
                if (password.length() < 4) {
                    Toast.makeText(RegisterActivity.this, "Please Enter minimum 8 letters in password", Toast.LENGTH_LONG).show();
                    return;
                }

                if (email.length() < 11) {
                    Toast.makeText(RegisterActivity.this, "Please Enter correct email", Toast.LENGTH_LONG).show();
                    return;
                }
                //check connectivity
                if (!isOnline(RegisterActivity.this)) {
                    Toast.makeText(RegisterActivity.this, "No network connection", Toast.LENGTH_LONG).show();
                    return;
                }

                //from login.java
                new loginAccess().execute();
            }

            private boolean isOnline(Context mContext) {
                ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
                return false;
            }
            //Close code that check online details
        });


    }

    class loginAccess extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Sign in...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String username = ((EditText) findViewById(R.id.username)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();
            String email = ((EditText) findViewById(R.id.email)).getText().toString();

            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("email", email));

            JSONObject json = jsonParser.makeHttpRequest(url, "POST", params);

            Log.d("Create Response", json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    flag = 0;
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("username", username);
                    i.putExtra("password", password);
                    i.putExtra("email", email);
                    startActivity(i);
                    finish();
                } else {
                    // failed to Sign in
                    flag = 1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            if (flag == 1)
                Toast.makeText(RegisterActivity.this, "Please Enter Correct informations", Toast.LENGTH_LONG).show();

        }

    }

}
