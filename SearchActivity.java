package com.example.flight_booking_app;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flight_booking_app.Adapter.FlightAdapter;
import com.example.flight_booking_app.Model.Flight;
import com.example.flight_booking_app.databinding.ActivitySearchBinding; // Ensure you reference the correct binding class
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class SearchActivity extends BaseActivity {

    private ActivitySearchBinding binding; // Fixed typo here
    private String from, to, date;
    private int numPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
        setVariable();

    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v ->{
            finish();
        });
    }

    private void initList() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Flights");
        ArrayList<Flight> list = new ArrayList<>();
        Query query = myRef.orderByChild("from").equalTo(from);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Flight flight = issue.getValue(Flight.class);
                        if (flight.getTo().equals(to)) {
                            list.add(flight);
                        }
                    }

                    if (!list.isEmpty()) {
                        binding.searchView.setLayoutManager(new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.VERTICAL, false));
                        binding.searchView.setAdapter(new FlightAdapter(list)); // Make sure FlightAdapter is defined correctly
                    } else {
                        binding.searchView.setVisibility(View.GONE);

                    }

                    binding.progressBarSearch.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error here (optional)
                binding.progressBarSearch.setVisibility(View.GONE);
                // Optionally show an error message to the user
            }
        });
    }

    private void getIntentExtra() {
        from = getIntent().getStringExtra("from");
        to = getIntent().getStringExtra("to");
        date = getIntent().getStringExtra("date");
        numPassenger = getIntent().getIntExtra("numPassenger", 0);
    }
}