package com.example.peeps_client.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.peeps_client.R;
import com.example.peeps_client.supplementary.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //progress dialog
    private ProgressDialog pDialog;

    //GUI elements
    Button btnRegister;
    Button btnLogin;

    EditText etxtUsername;
    EditText etxtPassword;

    // JSON tags
    private static final String TAG_USERNAME = "username";
    private static final String TAG_PASSWORD = "password";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_LOGIN_STATUS = "login_status";

    //Emulator IP
    private static String url_register;

    //JsonParser
    final JSONParser jsonParser = new JSONParser();

    //Shared preference editor (for storing login data)
    SharedPreferences sharedPreferences;
    String SPLocation = "com.example.peeps_client";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity: ", "Starting App");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set server ip
        url_register = "http://"+getApplicationContext().getResources().getString(R.string.serverIP)+"/peeps-server/login_user.php";

        // Load past username/password
        etxtUsername = (EditText) findViewById(R.id.etxtUsername);
        etxtPassword = (EditText) findViewById(R.id.etxtPassword);
        sharedPreferences = getSharedPreferences(SPLocation,MODE_PRIVATE);
        etxtUsername.setText(sharedPreferences.getString(SPLocation+".username",""));
        etxtPassword.setText(sharedPreferences.getString(SPLocation+".password",""));

        // Register button setup
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });


        // Login button setup
        btnLogin = (Button) findViewById(R.id.btnCalculate);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Login:", "button pressed.");

                //Logging-in alert
                pDialog = new ProgressDialog(MainActivity.this);
                pDialog.setMessage("Logging in..");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();

                //get login info
                String username = etxtUsername.getText().toString();
                String password = etxtPassword.getText().toString();

                //create login details JSON
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(TAG_USERNAME, username);
                    jsonObject.put(TAG_PASSWORD, password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //create login thread
                Thread thread = new Thread() {
                    public void run() {
                        JSONObject JSONReturn = jsonParser.makeHttpRequest(url_register, "POST", jsonObject);
                        pDialog.dismiss();

                        if (JSONReturn == null) {
                            Log.d("Post response: ", "JSON returned NULL");
                            return;
                        }
                        Log.d("Post response: ", JSONReturn.toString());
                        pDialog.dismiss();

                        try {
                            int loginStatus = JSONReturn.getInt(TAG_LOGIN_STATUS);
                            if (loginStatus == 1) {
                                //succeeded

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Login successful.",Toast.LENGTH_SHORT).show();
                                    }
                                });

                                //store user data locally
                                sharedPreferences.edit().putString(SPLocation+".username",etxtUsername.getText().toString()).apply();
                                sharedPreferences.edit().putString(SPLocation+".password",etxtPassword.getText().toString()).apply();

                                // Go to next activity
                                Intent i = new Intent(getApplicationContext(), PopDensityActivity.class);
                                startActivity(i);
                            } else {
                                //failed
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Login unsuccessful.",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this,"Something went wrong.",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                };
                //start login thread
                thread.start();

            }
        });
    }
}