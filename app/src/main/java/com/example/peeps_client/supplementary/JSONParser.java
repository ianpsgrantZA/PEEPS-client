package com.example.peeps_client.supplementary;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JSONParser {
    private static final String TAG = "JSONParser";

    //constructor
    public JSONParser(){

    }

    //make GET or POST HTTP request
    public JSONObject makeHttpRequest(String urlName, String method, JSONObject jsonInput){

        JSONObject jsonOutput= null;

        try {
            if (method=="POST"){


                String message = jsonInput.toString();
                System.out.println("JSON message: "+message);


                URL url = new URL(urlName);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setReadTimeout( 10000 /*milliseconds*/ );
                con.setConnectTimeout( 15000 /* milliseconds */ );
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setFixedLengthStreamingMode(message.getBytes().length);
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                //open
                con.connect();
                //send
                OutputStream os = new BufferedOutputStream(con.getOutputStream());
                os.write(message.getBytes());
                //clean
                os.flush();

                //read from server
                String respo = null;
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    respo = response.toString();
                    System.out.println(response.toString());
                }

                jsonOutput = new JSONObject(respo);


            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonOutput;

    }
}
