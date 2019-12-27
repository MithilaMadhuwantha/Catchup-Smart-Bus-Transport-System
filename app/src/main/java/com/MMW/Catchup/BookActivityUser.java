package com.MMW.Catchup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.MMW.Catchup.models.Book;
import com.MMW.Catchup.models.BookingInfo;
import com.MMW.Catchup.models.BusTrip;
import com.MMW.Catchup.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookActivityUser extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener{

    TableLayout tableLayout;
    private Set<BookingInfo> busBookingInfo = new HashSet<>();
    Book book= new Book();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_user);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        passengerBookingInfo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuObj) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.goto_booking, menuObj);
        return true;
    }

    @Override
    public void onClick(View view) {

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
                startActivity(new Intent(BookActivityUser.this, ProfileActivity.class));
                break;
            case R.id.menuMap:
                startActivity(new Intent(BookActivityUser.this, MapsActivity.class));
                break;
            case R.id.tripInfo:
                startActivity(new Intent(BookActivityUser.this, ScheduleActivity.class));
                break;

        }
        return true;
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    void passengerBookingInfo(){
        tableLayout = findViewById(R.id.busBookingInfo);
        findViewById(R.id.busTripInfo).setVisibility(View.GONE);
        findViewById(R.id.editText).setVisibility(View.INVISIBLE);

        FirebaseDatabase.getInstance().getReference("booking")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                busBookingInfo.clear();
                                Set<String> busTripId = new HashSet<>();
                                Set<BusTrip> busTrips = new HashSet<>();
                                // Get user value
                                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){ //Driver Id
                                    for(DataSnapshot postSnapshot1:postSnapshot.getChildren()){ // Bus Id
                                        for(DataSnapshot postSnapshot2:postSnapshot1.getChildren()){ // Unique Id
                                            BookingInfo bookings = postSnapshot2.getValue(BookingInfo.class);
                                            if(bookings.getBookedUser().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                /*bookings.setBookedUser(postSnapshot1.getKey());
                                                busTripId.add(postSnapshot1.getKey());
                                                busTrips.add(postSnapshot1.getKey());*/
                                                busBookingInfo.add(bookings);

                                            }
                                        }
                                    }
                                }

                                /*FirebaseDatabase.getInstance().getReference("BusTrip")
                                        .addListenerForSingleValueEvent(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                                    }
                                                });*/

                                int childCount = tableLayout.getChildCount();
                                tableLayout.removeViews(1,childCount-1);

                                for (BookingInfo bookingDetail : busBookingInfo) {

                                    TableRow row = new TableRow(BookActivityUser.this);

                                    Integer seatNo = bookingDetail.getSeatNo();

                                    TextView colSeatNo = new TextView(BookActivityUser.this);
                                    colSeatNo.setTextColor(Color.YELLOW);
                                    colSeatNo.setTextSize(50);
                                    colSeatNo.setText(seatNo.toString());

                                    row.addView(colSeatNo);

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
