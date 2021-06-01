package com.example.aplicatieocr_corectaretestegrila;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private EditText mEmail, mPassword, mConfirmPass;
    private Button mRegister, mBack;
    private Spinner mUser;
    private String selectedUser;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        mEmail = findViewById(R.id.emailreg);
//        mPassword = findViewById(R.id.passwordreg);
//        mConfirmPass = findViewById(R.id.confirmpass);
//        mRegister = findViewById(R.id.backlogin);
//        mBack = findViewById(R.id.backlogin);
//        mAuth = FirebaseAuth.getInstance();
//
//        mUser = findViewById(R.id.userreg);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUser.setAdapter(adapter);
        mUser.setOnItemSelectedListener(this);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if (mPassword.getText().toString().equals(mConfirmPass.getText().toString())) {
                    try {
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Eroare la înregistrare", Toast.LENGTH_SHORT).show();
                                } else {
                                    String user_id = mAuth.getCurrentUser().getUid();
                                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
                                    current_user_db.setValue(true);
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Parolele introduse nu corespund", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedUser = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
