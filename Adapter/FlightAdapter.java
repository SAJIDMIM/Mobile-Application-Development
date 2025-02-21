package com.example.flight_booking_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.Activity.SeatListActivity;
import com.example.flight_booking_app.Model.Flight;
import com.example.flight_booking_app.databinding.ViewholderFlightBinding;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.Viewholder> {
    private final ArrayList<Flight> flights;
    private Context context;

    public FlightAdapter(ArrayList<Flight> flights) {
        this.flights = flights;
    }

    @NonNull
    @Override
    public FlightAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderFlightBinding binding = ViewholderFlightBinding.inflate(LayoutInflater.from(context), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightAdapter.Viewholder holder, int position) {
        Flight flight = flights.get(position);

        // Load image from URL using AsyncTask
        new LoadImageTask(holder.binding.logo).execute(flight.getAirlineLogo());

        holder.binding.fromTxt.setText(flight.getFrom());
        holder.binding.fromShortTxt.setText(flight.getFromShort());
        holder.binding.toTxt.setText(flight.getTo());
        holder.binding.toShortTxt.setText(flight.getToShort());
        holder.binding.arrivalTxt.setText(flight.getArriveTime());
        holder.binding.classTxt.setText(flight.getClassSeat());
        holder.binding.priceTxt.setText("$" + flight.getPrice());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SeatListActivity.class);
            intent.putExtra("flight", flight);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            try {
                // Open a connection to the URL
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                // Download the image
                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Set the downloaded image into the ImageView
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderFlightBinding binding;

        public Viewholder(ViewholderFlightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
