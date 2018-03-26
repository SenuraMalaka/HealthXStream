package com.example.senura.healthxstream.mqttConnectionPackage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

/**
 * Created by senura on 3/26/18.
 */

public class MqttConnection {



    uniqueIDgenerator unique=new uniqueIDgenerator();
    private Context myContext=null;
    private MqttAndroidClient client=null;
    private static final String brokerAddress="ssl://a36q3zlv5b6zdr.iot.ap-southeast-1.amazonaws.com:8883";
    private static final String TAG = "MqttConnectionClass";
    private static String clientID=null;

    public MqttAndroidClient connect(Context passedContext , final Activity currentAct, final String passedTopic, final boolean needClientID) {
        Log.d("Mytag","in mqttconnection class"+unique.generateUUID());
        //start
        myContext=passedContext;


        clientID = unique.generateUUID();
        client = new MqttAndroidClient(myContext.getApplicationContext(), brokerAddress,
                clientID);


        final MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setConnectionTimeout(5);
        options.setKeepAliveInterval(60);

        SslUtil util1=new SslUtil(myContext.getApplicationContext());
        options.setSocketFactory(util1.getSocketFactory(""));//we only pass the password (empty password)- Sen

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected

                    String topic = passedTopic;
                    if (needClientID)
                    { topic = passedTopic+clientID;}//add clientId if it needs to be added to the topic



                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // The message was published

                                ////////commented on 18thDec -Sen
                                //EventHandleCheck.setClientAsConnected();
                                //EventHandleCheck.setCheckbtnVisible();
                                ////////commented on 18thDec -Sen

                                Toast.makeText(myContext, "Successfully Connected", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards

                                Log.d(TAG, "Error occured onfailure : "+exception.toString());
                                //Toast.makeText(myContext, "Error Message Sen: "+exception.toString(), Toast.LENGTH_LONG).show();


                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems

                    Log.d(TAG, "onFailure"+exception.toString()); //this gives an error on android 4.0 -Sen
                    Toast.makeText(myContext, "Couldn't connect. Check the internet connection.", Toast.LENGTH_LONG).show();

                    ////////commented on 18thDec -Sen
                    //myContext.startActivity( new Intent(myContext, MainActivity.class ));//goBack
                    ////////commented on 18thDec -Sen

                    currentAct.finish();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            Log.d(TAG, "Connect method error : "+e.toString());
        }



        return client;
        //end

    }



    public boolean publishMessage (String passedPayload, String passedTopic)
    {
        boolean isPublished=false;

        if (!client.equals(null)) {
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = passedPayload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(passedTopic, message);
                isPublished=true;
            } catch (UnsupportedEncodingException | MqttException e) {
                Log.e(TAG, "Publishing failed with UnsupportedEncodingException: "+e.toString());
            }
            catch(Exception e){
                Log.e(TAG, "Publishing failed : "+e.toString());
            }

        }
        else
        {

            //Warn if the client has not assigned -Sen
            Log.w(TAG, "Client has not assigned. Assign it first..!!");
        }

        return isPublished;

    }//pub msg end





}
