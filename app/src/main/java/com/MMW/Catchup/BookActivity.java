package com.MMW.Catchup;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.MMW.Catchup.models.Book;
import com.MMW.Catchup.models.BookingInfo;
import com.MMW.Catchup.models.BusTrip;
import com.MMW.Catchup.models.Route;
import com.MMW.Catchup.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener{

    private static Map<String,Integer> busSeat= new HashMap<>();
    String busType=null;
    Integer seatNo=1;
    List<Integer> allBusSeat= new ArrayList<>();
    TableLayout tableLayout;

    int selectedPosition=0;

    private Set<BookingInfo> busBookingInfo = new HashSet<>();
    private Set<Book> busTripList = new HashSet<>();
    List<String> busTripDetailIds = new ArrayList<>();

    ProgressBar progressBar;

    Spinner seatNoSelector;

    User fetchUser= new User();

    Book book= new Book();
    FirebaseDatabase database;
    DatabaseReference myRef;

    {
        busSeat.put("NORMAL",54);
        busSeat.put("SEMI LUXURY",54);
        busSeat.put("LUXURY",41);
        busSeat.put("SUPER LUXURY",41);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Button bookBtn = findViewById(R.id.book);
        Button clearBtn = findViewById(R.id.clear);
        seatNoSelector = findViewById(R.id.availableSeatNo);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("booking");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        progressBar = findViewById(R.id.progressbar);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            book = (Book) extras.getSerializable("book");
            busType= book.getBusTrip().getBusType();
            while (busSeat.get(busType)>=seatNo){
                allBusSeat.add(seatNo);
                seatNo++;
            }
            ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allBusSeat);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            seatNoSelector.setAdapter(dataAdapter);
            bookingInfo();
        }

        //Visibility of schedule add button according to the user role
        FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        fetchUser = dataSnapshot.getValue(User.class);

                        if (fetchUser.getUserType().equals("Passenger")) {
                            findViewById(R.id.availableSeatNo).setVisibility(View.VISIBLE);
                            findViewById(R.id.book).setVisibility(View.VISIBLE);
                            findViewById(R.id.busBookingInfo).setVisibility(View.GONE);
                        } else if (fetchUser.getUserType().equals("Driver")) {
                            fetchBusTripInfo();

                            findViewById(R.id.editText).setVisibility(View.INVISIBLE);
                            findViewById(R.id.availableSeatNo).setVisibility(View.VISIBLE);
                            findViewById(R.id.book).setVisibility(View.GONE);
                            findViewById(R.id.busBookingInfo).setVisibility(View.VISIBLE);
                            findViewById(R.id.clear).setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });

        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookSeat();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBookingSeatInfo(busTripDetailIds.get(selectedPosition));
            }
        });

        Button btnSeatMap = findViewById(R.id.seatMap);
        btnSeatMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    switch (busType){
                        case "NORMAL":
                            showImage(Uri.parse("android.resource://"+BuildConfig.APPLICATION_ID+"/" +R.drawable.normal_seat_maping));
                            break;
                        case "SEMI LUXURY":
                            showImage(Uri.parse("android.resource://"+BuildConfig.APPLICATION_ID+"/" +R.drawable.semi_luxury_seat_maping));
                            break;
                        case "LUXURY":
                            showImage(Uri.parse("android.resource://"+BuildConfig.APPLICATION_ID+"/" +R.drawable.luxury_seat_maping));
                            break;
                        case "SUPER LUXURY":
                            showImage(Uri.parse("android.resource://"+BuildConfig.APPLICATION_ID+"/" +R.drawable.super_luxury_seat_maping));
                            break;
                    }
            }
        });

    }

    public void showImage(Uri imageUri) {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuObj) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.goto_booking, menuObj);
        return true;
    }

    void clearBookingSeatInfo(String busId){

        FirebaseDatabase.getInstance().getReference("booking").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(busId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                            Intent refresh = new Intent(getApplicationContext(), MapsActivity.class);
                            startActivity(refresh);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "Error while loading data", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    void bookingInfo(){
        FirebaseDatabase.getInstance().getReference("booking").child(book.getId()).child(book.getBusId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            BookingInfo bookingInfo = postSnapshot.getValue(BookingInfo.class);
                            allBusSeat.remove(bookingInfo.getSeatNo());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    void fetchBusTripInfo(){
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
                                    book.setBusId(postSnapshot.getKey());
                                    busTripList.add(book);
                                }

                                List<String> busTripDetail = new ArrayList<>();
                                busTripDetailIds.clear();

                                for (Book busTrip : busTripList) {
                                    String fullDetail=busTrip.getBusTrip().getStartTime()+" "+busTrip.getBusTrip().getEndTime()+" " + busTrip.getBusTrip().getStartLocation();
                                    busTripDetailIds.add(busTrip.getBusId());
                                    busTripDetail.add(fullDetail);
                                }
                                if(busTripDetailIds.get(0)!=null) {
                                    fetchBookingInfo(busTripDetailIds.get(0));
                                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(BookActivity.this, android.R.layout.simple_spinner_item, busTripDetail);
                                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    seatNoSelector.setAdapter(dataAdapter);
                                    seatNoSelector.setOnItemSelectedListener(BookActivity.this);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            }
                        });
    }

    void fetchBookingInfo(String busId){
        tableLayout = findViewById(R.id.busBookingInfo);

        FirebaseDatabase.getInstance().getReference("booking").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(busId)
                .addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        busBookingInfo.clear();
                        // Get user value
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                            BookingInfo bookings = postSnapshot.getValue(BookingInfo.class);
                            busBookingInfo.add(bookings);
                        }

                        int childCount = tableLayout.getChildCount();
                        tableLayout.removeViews(1,childCount-1);

                        if(busBookingInfo.size()==0){
                            findViewById(R.id.clear).setVisibility(View.GONE);
                        }else {
                            findViewById(R.id.clear).setVisibility(View.VISIBLE);
                        }

                        for (BookingInfo bookingDetail : busBookingInfo) {

                            TableRow row = new TableRow(BookActivity.this);

                            Integer seatNo = bookingDetail.getSeatNo();
                            String bookedUserName =bookingDetail.getBookedUserName();

                            TextView colSeatNo = new TextView(BookActivity.this);
                            colSeatNo.setTextColor(Color.RED);
                            colSeatNo.setTextSize(30);
                            colSeatNo.setText(seatNo.toString());

                            TextView colBookedUserId = new TextView(BookActivity.this);
                            colBookedUserId.setTextColor(Color.RED);
                            colSeatNo.setTextSize(30);
                            colBookedUserId.setText(bookedUserName);


                            row.addView(colSeatNo);
                            row.addView(colBookedUserId);

                            tableLayout.addView(row);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }


    void bookSeat(){
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setSeatNo(Integer.parseInt(seatNoSelector.getSelectedItem().toString()));
        bookingInfo.setBookedUser(book.getUser().getUserId());
        bookingInfo.setBookedUserName(book.getUser().getUserName());
        String id = myRef.push().getKey();

        FirebaseDatabase.getInstance().getReference("booking").child(book.getId()).child(book.getBusId()).child(id).setValue(bookingInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Added Successfully!", Toast.LENGTH_SHORT).show();
                            Intent refresh = new Intent(getApplicationContext(), ScheduleActivity.class);
                            startActivity(refresh);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "Error while loading data", Toast.LENGTH_SHORT).show();

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
            case R.id.book:
                bookSeat();
                break;
        }
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
                startActivity(new Intent(BookActivity.this, ProfileActivity.class));
                break;
            case R.id.menuMap:
                startActivity(new Intent(BookActivity.this, MapsActivity.class));
                break;

        }
        return true;
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String busId = busTripDetailIds.get(i);
        fetchBookingInfo(busId);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    void passengerBookingInfo(){
        tableLayout = findViewById(R.id.busBookingInfo);

        findViewById(R.id.clear).setVisibility(View.GONE);
        findViewById(R.id.availableSeatNo).setVisibility(View.GONE);
        findViewById(R.id.book).setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("booking")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                busBookingInfo.clear();
                                // Get user value
                                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                                    for(DataSnapshot postSnapshot1:postSnapshot.getChildren()){
                                        for(DataSnapshot postSnapshot2:postSnapshot1.getChildren()){
                                            BookingInfo bookings = postSnapshot2.getValue(BookingInfo.class);
                                            if(bookings.getBookedUser().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                busBookingInfo.add(bookings);
                                            }
                                        }
                                    }
                                }

                                int childCount = tableLayout.getChildCount();
                                tableLayout.removeViews(1,childCount-1);

                                for (BookingInfo bookingDetail : busBookingInfo) {

                                    TableRow row = new TableRow(BookActivity.this);

                                    Integer seatNo = bookingDetail.getSeatNo();
                                    String bookedUserName =bookingDetail.getBookedUserName();

                                    TextView colSeatNo = new TextView(BookActivity.this);
                                    colSeatNo.setTextColor(Color.RED);
                                    colSeatNo.setTextSize(30);
                                    colSeatNo.setText(seatNo.toString());

                                    TextView colBookedUserId = new TextView(BookActivity.this);
                                    colBookedUserId.setTextColor(Color.RED);
                                    colSeatNo.setTextSize(30);
                                    colBookedUserId.setText(bookedUserName);


                                    row.addView(colSeatNo);
                                    row.addView(colBookedUserId);

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
