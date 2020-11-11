package com.example.peeps_client.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
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


public class RegisterActivity extends AppCompatActivity {

    //progress dialog
    private ProgressDialog pDialog;

    //JsonParser
    final JSONParser jsonParser = new JSONParser();

    //GUI elements
    Button btnRegister;
    EditText etxtUsername;
    EditText etxtPassword;

    // JSON tags
    private static final String TAG_USERNAME = "username";
    private static final String TAG_PASSWORD = "password";
    private static final String TAG_SUCCESS = "success";

    //Emulator IP
    private static String url_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //set server ip
        url_register = "http://"+getApplicationContext().getResources().getString(R.string.serverIP)+"/peeps-server/register_user.php";

        //EditText setup
        etxtUsername = (EditText) findViewById(R.id.etxtUsername);
        etxtPassword = (EditText) findViewById(R.id.etxtPassword);

        //Button setup
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create new user in database
                System.out.println("startingClick");
                //temp
                pDialog = new ProgressDialog(RegisterActivity.this);
                pDialog.setMessage("Creating Account..");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();

                String username = etxtUsername.getText().toString();
                String password = etxtPassword.getText().toString();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(TAG_USERNAME, username);
                    jsonObject.put(TAG_PASSWORD, password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                new Thread(){
                    public void run(){
                        JSONObject JSONReturn = jsonParser.makeHttpRequest(url_register, "POST", jsonObject);
                        pDialog.dismiss();

                        if (JSONReturn==null){
                            Log.d("Post response: ", "JSON returned NULL");
                            return;
                        }
                        Log.d("Post response: ", JSONReturn.toString());
                        try {
                            int success = JSONReturn.getInt(TAG_SUCCESS);
                            if (success == 1){
                                //succeeded
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Registration successful.",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);

                            } else{
                                //failed
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Registration unsuccessful.",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }
}
