package com.example.flight_booking_app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.flight_booking_app.BaseActivity;
import com.example.flight_booking_app.ChatActivity;
import com.example.flight_booking_app.EditAccount;
import com.example.flight_booking_app.MainActivity;
import com.example.flight_booking_app.Model.Flight;
import com.example.flight_booking_app.databinding.ActivityTicketDetailsBinding;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TicketDetailActivity extends BaseActivity {
    private ActivityTicketDetailsBinding binding;
    private Flight flight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        setVariable();
        setupNavigationButtons();
    }

    private void getIntentExtra() {
        flight = (Flight) getIntent().getSerializableExtra("flight");
    }

    private void setVariable() {
        binding.backbtn.setOnClickListener(v -> finish());
        binding.fromTxt.setText(flight.getFromShort());
        binding.fromSmallTxt.setText(flight.getFrom());
        binding.toTxt.setText(flight.getTo());
        binding.toShortTxt.setText(flight.getToShort());
        binding.toSmallTxt.setText(flight.getTo());
        binding.dateTxt.setText(flight.getDate());
        binding.timeTxt.setText(flight.getTime());
        binding.arrivalTxt.setText(flight.getArriveTime());
        binding.classTxt.setText(flight.getClassSeat());
        binding.priceTxt.setText("$" + flight.getPrice());
        binding.airlinesTxt.setText(flight.getAirlineName());
        binding.seatsTxt.setText(flight.getPassenger());

        // Load the airline logo using AsyncTask
        new LoadImageTask(binding.logo).execute(flight.getAirlineLogo());
    }

    private void setupNavigationButtons() {
        binding.homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TicketDetailActivity.this, MainActivity.class);
            startActivity(intent);
        });

        binding.chatBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TicketDetailActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        binding.locationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TicketDetailActivity.this, ActivityTicketDetailsBinding.class);
            startActivity(intent);
        });

        binding.editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TicketDetailActivity.this, EditAccount.class);
            startActivity(intent);
        });
    }

    // AsyncTask to load image from URL
    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                URL imageUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
