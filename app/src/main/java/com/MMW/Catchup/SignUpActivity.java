package com.MMW.Catchup;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.FirebaseDatabase;
import com.MMW.Catchup.models.Route;
import com.MMW.Catchup.models.User;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {

    ProgressBar progressBar;
    EditText editTextEmail, editTextPassword,editTextBusNo,editTextRouteNumber,editTextOrigin,editTextDestination,editTextRoadName;

    private FirebaseAuth mAuth;

    Spinner userTypeSelection;
    private static final String[] userTypes = new String[]{"Passenger", "Driver"};

    Spinner busTypeSelection;
    private static final String[] busTypes = new String[]{"NORMAL", "SEMI LUXURY","LUXURY","SUPER LUXURY"};

    //Store Selected User Type
    private int selectedUserType=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.progressbar);
        editTextBusNo = findViewById(R.id.editBusNo);
        editTextRouteNumber= findViewById(R.id.editRouteNumber);
        editTextOrigin= findViewById(R.id.editOrigin);
        editTextDestination= findViewById(R.id.editDestination);
        editTextRoadName= findViewById(R.id.editRoadName);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.buttonSignUp).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);

        userTypeSelection=findViewById(R.id.userType);

        userTypes();

        busTypeSelection=findViewById(R.id.busType);
        busTypes();
    }

    private void busTypes(){
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, busTypes);
        //set the spinners adapter to the previously created one.
        busTypeSelection.setAdapter(adapter);
        busTypeSelection.setOnItemSelectedListener(this);
    }

    private void userTypes(){
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userTypes);
        //set the spinners adapter to the previously created one.
        userTypeSelection.setAdapter(adapter);
        userTypeSelection.setOnItemSelectedListener(this);
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        final String busNo = editTextBusNo.getText().toString().trim();
        final String origin = editTextOrigin.getText().toString().trim();
        final String destination = editTextDestination.getText().toString().trim();
        final String roadName = editTextRoadName.getText().toString().trim();

        final String routeNumber = editTextRouteNumber.getText().toString().trim();

        switch (selectedUserType){
            case -1:
                break;
            case 0:
                break;
            case 1:
                if (busNo.isEmpty()) {
                    editTextBusNo.setError("Bus Number is required");
                    editTextBusNo.requestFocus();
                    return;
                }

                if (origin.isEmpty()) {
                    editTextOrigin.setError("Starting Location is required");
                    editTextOrigin.requestFocus();
                    return;
                }

                if (destination.isEmpty()) {
                    editTextDestination.setError("Ending Location is required");
                    editTextDestination.requestFocus();
                    return;
                }

                if (routeNumber.isEmpty()) {
                    editTextRouteNumber.setError("Routes Number is required");
                    editTextRouteNumber.requestFocus();
                    return;
                }

                if (roadName.isEmpty()) {
                    editTextRoadName.setError("RoadName is required");
                    editTextRoadName.requestFocus();
                    return;
                }
                break;
        }

        // Validations
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Minimum length of password should be 6");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {

                    User user = new User(FirebaseAuth.getInstance().getCurrentUser().getUid(),userTypes[selectedUserType]);

                    FirebaseDatabase.getInstance().getReference("User")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if(selectedUserType==0){
                                            finish();   // After Sign Up Moving to Profile Activity.
                                            //startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                            startActivity(new Intent(SignUpActivity.this, ProfileActivity.class));

                                        }else if(selectedUserType==1){
                                            Route route = new Route(roadName,origin,destination,busNo,false, Integer.parseInt(routeNumber),busTypeSelection.getSelectedItem().toString());
                                            FirebaseDatabase.getInstance().getReference("Routes")
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(route)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            finish();   // After Sign Up Moving to Profile Activity.
                                                            //startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                            startActivity(new Intent(SignUpActivity.this, ProfileActivity.class));
                                                        }
                                            });
                                        }

                                    }
                                }
                            });


                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, ProfileActivity.class));
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSignUp:
                registerUser();
                break;

            case R.id.textViewLogin:
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                // Whatever you want to happen when the first item gets selected
                selectedUserType=0;
                editTextBusNo.setVisibility(View.GONE);
                editTextRouteNumber.setVisibility(View.GONE);
                editTextOrigin.setVisibility(View.GONE);
                editTextDestination.setVisibility(View.GONE);
                editTextRoadName.setVisibility(View.GONE);
                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                selectedUserType=1;
                editTextBusNo.setVisibility(View.VISIBLE);
                editTextRouteNumber.setVisibility(View.VISIBLE);
                editTextOrigin.setVisibility(View.VISIBLE);
                editTextDestination.setVisibility(View.VISIBLE);
                editTextRoadName.setVisibility(View.VISIBLE);
                busTypeSelection.setVisibility(View.VISIBLE);
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }
}
