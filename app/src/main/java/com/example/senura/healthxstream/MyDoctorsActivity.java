package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyDoctorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_doctors);

        // Get reference of widgets from XML layout
        final ListView lv = (ListView) findViewById(R.id.ListView_MyDocs);

        // Initializing a new String Array
        String[] fruits = new String[] {
                "Senura Dissanayake",
                "Kanishka Perera",
                "Kumar Perera",
                "Derek Soysa",
                "Senura Dissanayake",
                "Kanishka Perera",
                "Kumar Perera",
                "Derek Soysa"
        };

        // Create a List from String Array elements
        final List<String> fruits_list = new ArrayList<String>(Arrays.asList(fruits));

        // Create an ArrayAdapter from List
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, fruits_list);

        // DataBind ListView with items from ArrayAdapter
        lv.setAdapter(arrayAdapter);



        Intent intent = getIntent();
        String value = intent.getStringExtra("key");


        fruits_list.add(value);

        arrayAdapter.notifyDataSetChanged();



    }
}
