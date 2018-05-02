package com.example.senura.healthxstream.mqttConnectionPackage;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonAccess {



    public static  String getJsonInsideObj(String jsonObj, String reqKey){

        String out="";

        try {
            JSONObject reader = new JSONObject(jsonObj);
            out = reader.getString(reqKey);
        } catch (JSONException e) {
            Log.e("JsonAccess", "jsonParseError - " + e.toString());
        }
        return out;
    }


    public static String getJsonInsideArray(String jsonArray, int index){

        String out="";

        JSONArray jArray = null;
        try {
            jArray = new JSONArray(jsonArray);

            if (jArray != null && jArray.length()>0) {
                out=jArray.getJSONObject(index).toString();
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }







}
