package com.MMW.Catchup;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.MMW.Catchup.models.Book;
import com.MMW.Catchup.models.Route;
import com.MMW.Catchup.modules.Routes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.MMW.Catchup.models.BusTrip;
import com.MMW.Catchup.models.User;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {

    ProgressBar progressBar;
    TableLayout tableLayout;

    private Set<Book> busTripList = new HashSet<>();

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;

    User fetchUser= new User();

    Spinner busTypeSelection;
    private static final String[] busTypes = new String[]{"ALL TYPES","NORMAL", "SEMI LUXURY","LUXURY","SUPER LUXURY"};

    private String busType=null;
    Route route = new Route();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        progressBar = findViewById(R.id.progressbar);

        //Visibility of schedule add button according to the user role
        FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        fetchUser = dataSnapshot.getValue(User.class);

                        if (fetchUser.getUserType().equals("Passenger")) {
                            findViewById(R.id.buttonAdd).setVisibility(View.GONE);
                        } else if (fetchUser.getUserType().equals("Driver")) {
                            fetchBusType();
                            findViewById(R.id.buttonAdd).setVisibility(View.VISIBLE);
                            findViewById(R.id.busType).setVisibility(View.GONE);
                            findViewById(R.id.id_cad_details_dialog_booking).setVisibility(View.GONE);
                        }
                        getTripInfo(fetchUser.getUserType(),"ALL");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });



        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("BusTrip");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        findViewById(R.id.buttonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                saveTripInfo();
            }
        });


//        tableLayout.removeAllViews();


        busTypeSelection=findViewById(R.id.busType);
        busTypes();
    }



    void fetchBusType(){
        //Checking bus type
        FirebaseDatabase.getInstance().getReference("Routes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        route = dataSnapshot.getValue(Route.class);
                        busType=route.getBusType();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    private void busTypes(){
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, busTypes);
        //set the spinners adapter to the previously created one.
        busTypeSelection.setAdapter(adapter);
        busTypeSelection.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        switch (position) {
            case 0:
                // Whatever you want to happen when the first item gets selected
                FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                getTripInfo(fetchUser.getUserType(),"ALL");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            }
                        });
                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get user value
                                fetchUser = dataSnapshot.getValue(User.class);
                                getTripInfo(fetchUser.getUserType(),"NORMAL");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            }
                        });

                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get user value
                                fetchUser = dataSnapshot.getValue(User.class);
                                getTripInfo(fetchUser.getUserType(),"SEMI LUXURY");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            }
                        });

                break;
            case 3:
                // Whatever you want to happen when the thrid item gets selected
                FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get user value
                                fetchUser = dataSnapshot.getValue(User.class);
                                getTripInfo(fetchUser.getUserType(),"LUXURY");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            }
                        });

                break;
            case 4:
                // Whatever you want to happen when the thrid item gets selected
                FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get user value
                                fetchUser = dataSnapshot.getValue(User.class);
                                getTripInfo(fetchUser.getUserType(),"SUPER LUXURY");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            }
                        });

                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuObj) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.goto_schedule, menuObj);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.menuProfile:
                startActivity(new Intent(ScheduleActivity.this, ProfileActivity.class));
                break;
            case R.id.menuMap:
                startActivity(new Intent(ScheduleActivity.this, MapsActivity.class));
                break;
            case R.id.booking:
                startActivity(new Intent(ScheduleActivity.this, BookActivity.class));
                break;
        }
        return true;
    }

    public void saveTripInfo() {
        LinearLayout createBusSchedule = new LinearLayout(this);
        createBusSchedule.setOrientation(LinearLayout.VERTICAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Schedule");

        // Set up the input
        final EditText routeNo = new EditText(this);
        final EditText startTime = new EditText(this);
        final EditText endTime = new EditText(this);
        final EditText startLocation = new EditText(this);

        routeNo.setHint("Enter Routes Number");
        startTime.setHint("Select Start Time");
        endTime.setHint("Select End Time");
        startLocation.setHint("Enter Start Location");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startTime.setShowSoftInputOnFocus(false);
        }

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        startTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        endTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        routeNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        startLocation.setInputType(InputType.TYPE_CLASS_TEXT);

        createBusSchedule.addView(routeNo);
        createBusSchedule.addView(startTime);
        createBusSchedule.addView(endTime);
        createBusSchedule.addView(startLocation);

        builder.setView(createBusSchedule);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String assignRouteNo = routeNo.getText().toString();
                String assignStartTime = startTime.getText().toString();
                String assignEndTime = endTime.getText().toString();
                String assignStartLocation = startLocation.getText().toString();

                BusTrip busTrip = new BusTrip(assignRouteNo, assignStartTime, assignEndTime, assignStartLocation,busType);

                //Save info
                saveTripInfo(busTrip);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public synchronized void getTripInfo(String userRole, final String busType) {

        tableLayout = findViewById(R.id.busScheduleInfo);



        if(userRole.equals("Driver")){
            FirebaseDatabase.getInstance().getReference("BusTrip")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                        BusTrip busTrip = postSnapshot.getValue(BusTrip.class);
                                        Book book = new Book();
                                        book.setBusTrip(busTrip);
                                        busTripList.add(book);
                                    }

                                    for (Book busTrip : busTripList) {

                                        TableRow row = new TableRow(ScheduleActivity.this);

                                        String busNo = busTrip.getBusTrip().getBusNo();
                                        String startTime = busTrip.getBusTrip().getStartTime();
                                        String endTime = busTrip.getBusTrip().getEndTime();
                                        String startLocation = busTrip.getBusTrip().getStartLocation();

                                        TextView colBusNo = new TextView(ScheduleActivity.this);
                                        colBusNo.setText(busNo);

                                        TextView colStartTime = new TextView(ScheduleActivity.this);
                                        colStartTime.setText(startTime);

                                        TextView colEndTime = new TextView(ScheduleActivity.this);
                                        colEndTime.setText(endTime);

                                        TextView colStartLocation = new TextView(ScheduleActivity.this);
                                        colStartLocation.setText(startLocation);

                                        if(!fetchUser.getUserType().equals("Driver")){
                                            TextView colBook = new TextView(ScheduleActivity.this);
                                            colBook.setText("Book");
                                            row.addView(colBook);
                                        }

                                        row.addView(colBusNo);
                                        row.addView(colStartTime);
                                        row.addView(colEndTime);
                                        row.addView(colStartLocation);

                                        tableLayout.addView(row);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                }
                            });

        }else if(userRole.equals("Passenger")){
            FirebaseDatabase.getInstance().getReference("BusTrip")
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    busTripList.clear();
                                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                        String userId= postSnapshot.getKey();

                                        for(DataSnapshot childSnapshot:postSnapshot.getChildren()){
                                            Book book = new Book();
                                            String busTripId = childSnapshot.getKey();
                                            BusTrip busTrip = childSnapshot.getValue(BusTrip.class);

                                            book.setId(userId);
                                            book.setBusId(busTripId);
                                            book.setBusTrip(busTrip);
                                            book.setUser(fetchUser);


                                            if(busType.equals("ALL")){
                                                busTripList.add(book);
                                            }else{
                                                if(busType.equals(busTrip.getBusType()))
                                                    busTripList.add(book);
                                            }
                                        }
                                    }

                                    int childCount = tableLayout.getChildCount();
                                    tableLayout.removeViews(1,childCount-1);

                                    for (Book busTrip : busTripList) {
                                        TableRow row = new TableRow(ScheduleActivity.this);

                                        String busNo = busTrip.getBusTrip().getBusNo();
                                        String startTime = busTrip.getBusTrip().getStartTime();
                                        String endTime = busTrip.getBusTrip().getEndTime();
                                        String startLocation = busTrip.getBusTrip().getStartLocation();

                                        TextView colBusNo = new TextView(ScheduleActivity.this);
                                        colBusNo.setText(busNo);

                                        TextView colStartTime = new TextView(ScheduleActivity.this);
                                        colStartTime.setText(startTime);

                                        TextView colEndTime = new TextView(ScheduleActivity.this);
                                        colEndTime.setText(endTime);

                                        TextView colStartLocation = new TextView(ScheduleActivity.this);
                                        colStartLocation.setText(startLocation);

                                        Button book = new Button(ScheduleActivity.this);
                                        book.setText("Book");
                                        book.setClickable(true);



                                        row.addView(colBusNo);
                                        row.addView(colStartTime);
                                        row.addView(colEndTime);
                                        row.addView(colStartLocation);

                                        row.addView(book);

                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("book",(Serializable) busTrip);


                                        final Intent intent = new Intent(getApplicationContext(),BookActivity.class);
                                        intent.putExtras(bundle);

                                        book.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                startActivity(intent);
                                            }
                                        });
                                        tableLayout.addView(row);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                }
                            });

        }


    }

    public void saveTripInfo(BusTrip busTrip) {
        String id = myRef.push().getKey();

        FirebaseDatabase.getInstance().getReference("BusTrip")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(id).setValue(busTrip)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Added Successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ScheduleActivity.this, ScheduleActivity.class));
                        } else {
                            progressBar.setVisibility(View.GONE);
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "Error while loading data", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ScheduleActivity.this, ScheduleActivity.class));
                            } else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {

    }

    private void busType(){
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, busTypes);
        //set the spinners adapter to the previously created one.
        busTypeSelection.setAdapter(adapter);
        busTypeSelection.setOnItemSelectedListener(this);
    }
}