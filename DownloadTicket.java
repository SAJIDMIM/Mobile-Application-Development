package com.example.flight_booking_app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DownloadTicket extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private List<String> selectedSeats;
    private double totalPrice;
    private Button downloadButton; // Ensure this variable is properly initialized

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details); // Ensure the correct layout is used

        // Make sure the selectedSeats and totalPrice are correctly passed via intent
        selectedSeats = getIntent().getStringArrayListExtra("selectedSeats");
        totalPrice = getIntent().getDoubleExtra("price", 0.0);

        // Initialize the downloadButton using the correct ID
        downloadButton = findViewById(R.id.confirmBtn);

        // Set click listener for the downloadButton
        downloadButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                checkStoragePermission();
            } else {
                generatePDF();
            }
        });
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            generatePDF();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePDF();
            } else {
                Toast.makeText(this, "Permission denied! Cannot save PDF.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void generatePDF() {
        // Show "PDF is saving..." message before starting the save process
        runOnUiThread(() -> Toast.makeText(this, "PDF is saving...", Toast.LENGTH_SHORT).show());

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);

        canvas.drawText("Flight Ticket", 50, 50, paint);
        canvas.drawText("Selected Seats: " + selectedSeats, 50, 100, paint);
        canvas.drawText("Total Price: $" + totalPrice, 50, 150, paint);

        pdfDocument.finishPage(page);

        Log.d("PDF_GENERATE", "PDF document created, checking version.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePdfToDownloadsAndroid10AndAbove(pdfDocument);
        } else {
            savePdfToDownloadsBelowAndroid10(pdfDocument);
        }
    }

    private void savePdfToDownloadsAndroid10AndAbove(PdfDocument pdfDocument) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Flight_Ticket.pdf");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver contentResolver = getContentResolver();
        Uri uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

        Log.d("PDF_SAVE", "URI: " + uri);

        if (uri == null) {
            runOnUiThread(() -> Toast.makeText(this, "Error creating file", Toast.LENGTH_LONG).show());
            return;
        }

        try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {
            if (outputStream != null) {
                pdfDocument.writeTo(outputStream);
                Log.d("PDF_SAVE", "PDF saved successfully.");
                runOnUiThread(() -> Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show());
            } else {
                Log.e("PDF_ERROR", "Error opening output stream.");
                runOnUiThread(() -> Toast.makeText(this, "Error opening output stream", Toast.LENGTH_LONG).show());
            }
        } catch (IOException e) {
            Log.e("PDF_ERROR", "Error saving PDF: ", e);
            runOnUiThread(() -> Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } finally {
            pdfDocument.close();
        }
    }

    private void savePdfToDownloadsBelowAndroid10(PdfDocument pdfDocument) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, "Flight_Ticket.pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            Log.d("PDF_SAVE", "PDF saved successfully.");
            runOnUiThread(() -> Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            Log.e("PDF_ERROR", "Error saving PDF: ", e);
            runOnUiThread(() -> Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}
