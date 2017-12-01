package com.example.chris.mcc_2017_g19;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Settings extends Activity implements AdapterView.OnItemSelectedListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        Spinner spinnerwifi = (Spinner) findViewById(R.id.spinnerwifi);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        spinnerwifi.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> size = new ArrayList<String>();
        size.add("size 1");
        size.add("size 2");
        size.add("size 3");

        // Spinner-wifi Drop down elements
        List<String> sizewifi = new ArrayList<String>();
        sizewifi.add("size 1 wifi");
        sizewifi.add("size 2 wifi");
        sizewifi.add("size 3 wifi");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, size);
        ArrayAdapter<String> dataAdapterwifi = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sizewifi);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapterwifi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinnerwifi.setAdapter(dataAdapterwifi);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // TODO: On selecting a spinner item we change the one setted on the db
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
}