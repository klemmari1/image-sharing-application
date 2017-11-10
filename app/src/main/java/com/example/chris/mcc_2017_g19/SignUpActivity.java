package com.example.chris.mcc_2017_g19;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {


    //inits
    EditText etName;
    EditText etEmail;
    EditText etPassword;
    Button btnRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //inits2
        btnRegister = (Button) findViewById(R.id.btnRegister);
        etName = (EditText) findViewById(R.id.editTextName);
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etPassword = (EditText) findViewById(R.id.editTextPassword);

        //TODO: wait for a replay from cloud that registeration is succesful I guess?

        //NOTES from the project description

    }
}
