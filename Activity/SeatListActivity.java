package com.example.flight_booking_app.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.flight_booking_app.Adapter.SeatAdapter;
import com.example.flight_booking_app.BaseActivity;
import com.example.flight_booking_app.Model.Flight;
import com.example.flight_booking_app.Model.Seat;
import com.example.flight_booking_app.databinding.ActivitySeatListBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatListActivity extends BaseActivity {
    private ActivitySeatListBinding binding;
    private Flight flight;
    private Double price = 0.0;
    private int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initSeatList();
        setVariable();
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.confirmBtn.setOnClickListener(v -> {
            if (num > 0) {
                // Create an AlertDialog to confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(SeatListActivity.this);
                builder.setMessage("Confirm seat selection?")
                        .setPositiveButton("Confirm", (dialog, id) -> {
                            // Action on confirm button
                            flight.setPassenger(binding.numberSelectedTxt.getText().toString());
                            flight.setPrice(price);
                            Intent intent = new Intent(SeatListActivity.this, TicketDetailActivity.class);
                            intent.putExtra("flight", flight);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Do nothing on cancel
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(SeatListActivity.this, "Please select your seat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initSeatList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 7);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position % 7 == 3) ? 1 : 1;
            }
        });
        binding.seatRecyclerView.setLayoutManager(gridLayoutManager);
        List<Seat> seatList = new ArrayList<>();
        int row = 0;
        int numberSeat = flight.getNumberSeat() + (flight.getNumberSeat() / 7) + 1;

        Map<Integer, String> seatAlphabeMap = new HashMap<>();
        seatAlphabeMap.put(0, "A");
        seatAlphabeMap.put(1, "B");
        seatAlphabeMap.put(2, "C");
        seatAlphabeMap.put(4, "D");
        seatAlphabeMap.put(5, "E");
        seatAlphabeMap.put(6, "F");

        for (int i = 0; i < numberSeat; i++) {
            if (i % 7 == 0) {
                row++;
            }
            if (i % 7 == 3) {
                seatList.add(new Seat(Seat.SeatStatus.EMPTY, String.valueOf(row)));
            } else {
                String seatName = seatAlphabeMap.get(i % 7) + row;
                Seat.SeatStatus seatStatus = flight.getReservedSeats().contains(seatName) ? Seat.SeatStatus.UNAVAILABLE : Seat.SeatStatus.AVAILABLE;
                seatList.add(new Seat(seatStatus, seatName));
            }
        }

        SeatAdapter seatAdapter = new SeatAdapter(seatList, this, (selectName, num) -> {
            binding.numberSelectedTxt.setText(num + " Seat(s) Selected");
            binding.nameSeatSelectedTxt.setText(selectName);
            DecimalFormat df = new DecimalFormat("#.##");
            price = (Double.valueOf(df.format(num * flight.getPrice())));
            this.num = num;
            binding.priceTxt.setText("$" + price);
        });

        binding.seatRecyclerView.setAdapter(seatAdapter);
        binding.seatRecyclerView.setNestedScrollingEnabled(false);
    }

    private void getIntentExtra() {
        flight = (Flight) getIntent().getSerializableExtra("flight");
    }
}