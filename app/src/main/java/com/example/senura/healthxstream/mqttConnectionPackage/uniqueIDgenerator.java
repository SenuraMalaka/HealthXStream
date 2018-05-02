package com.example.senura.healthxstream.mqttConnectionPackage;

import android.util.Log;

import java.util.UUID;

/**
 * Created by senura on 3/26/18.
 */

public class uniqueIDgenerator {


    static String uniqueId = null;
    static String uniqueIdForMQTT = null;//for MQTTconClass

    //generating UUID
    public static String getUUID(){


        if(uniqueId == null || uniqueId.isEmpty()){
            //generate random UUIDs
            uniqueId = UUID.randomUUID().toString();
        }

        Log.d("uniqueIDGENCLASS","this"+uniqueId);

        return uniqueId;
    }


    //generating UUID
    public static String generateUUID(){


        //generate random UUIDs
        uniqueIdForMQTT = UUID.randomUUID().toString();


        Log.d("uniqueIDGENCLASS","this mqtt"+uniqueIdForMQTT);

        return uniqueIdForMQTT;
    }



}
