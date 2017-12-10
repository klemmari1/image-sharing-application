package com.example.chris.mcc_2017_g19;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinner, spinnerwifi;
    SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        TextView imagequality = (TextView) findViewById(R.id.iq);
        // Spinner element
        spinner = (Spinner) findViewById(R.id.spinnerlte);
        spinnerwifi = (Spinner) findViewById(R.id.spinnerwifi);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        spinnerwifi.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> size = new ArrayList<String>();
        size.add("full (original size)");
        size.add("high (1280x960)");
        size.add("low (640x480)");

        // Spinner-wifi Drop down elements
        List<String> sizewifi = new ArrayList<String>();
        sizewifi.add("full (original size)");
        sizewifi.add("high (1280x960)");
        sizewifi.add("low (640x480)");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, size);
        ArrayAdapter<String> dataAdapterwifi = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sizewifi);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapterwifi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinnerwifi.setAdapter(dataAdapterwifi);


        //Check if the values of the spinner are already saved. If yes, load the values. If not, fill with full size
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getString("LTEpicturevalue","") != null && prefs.getString("WIFIpicturevalue","") != null )
        {
            loadspinnervalue();
        } else{
            uploaddefaultspinnervalue();
        }

    }


    public void loadspinnervalue(){
        String LTEpicturevalue =PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString("LTEpicturevalue","");
        String WIFIpicturevalue =PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString("WIFIpicturevalue","");

        for(int i=0;i<3;i++)
            if(LTEpicturevalue.equals(spinner.getItemAtPosition(i).toString())){
                spinner.setSelection(i);
                break;
            }

        for(int i=0;i<3;i++)
            if(WIFIpicturevalue.equals(spinnerwifi.getItemAtPosition(i).toString())){
                spinnerwifi.setSelection(i);
                break;
            }
    }


    public void uploaddefaultspinnervalue(){
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString("LTEpicturevalue", spinner.getSelectedItem().toString());
        prefEditor.putString("WIFIpicturevalue", spinnerwifi.getSelectedItem().toString());

        prefEditor.apply();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString("LTEpicturevalue", spinner.getSelectedItem().toString());
        prefEditor.putString("WIFIpicturevalue", spinnerwifi.getSelectedItem().toString());

        prefEditor.apply();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


}