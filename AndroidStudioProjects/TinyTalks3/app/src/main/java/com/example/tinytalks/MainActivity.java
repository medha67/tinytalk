package com.example.tinytalks;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button startButton;
    private Button stopButton;
    private LinearLayout rootLayout;
    private CryDetector detector;
    private int consecutiveHits = 0;

    private final Handler flashHandler = new Handler(Looper.getMainLooper());
    private Runnable flashRunnable;
    private boolean isFlashing = false;
    private boolean flashOn = false;

    private static final int REQUEST_CODE_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        rootLayout = findViewById(R.id.rootLayout);

        detector = new CryDetector(this, cryDetected -> runOnUiThread(() -> {
            if (cryDetected) {
                consecutiveHits++;
            } else {
                consecutiveHits = 0;
            }

            if (consecutiveHits >= 2) {
                statusText.setText("Cry Detected");
                startFlashing();
            } else if (consecutiveHits == 0) {
                statusText.setText("Listening");
                stopFlashing();
            }
        }));

        startButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_RECORD_AUDIO);
            } else {
                startMonitoring();
            }
        });

        stopButton.setOnClickListener(v -> stopMonitoring());
    }

    private void startFlashing() {
        if (isFlashing) return;
        isFlashing = true;

        flashRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFlashing) return;
                flashOn = !flashOn;
                rootLayout.setBackgroundColor(flashOn ? Color.RED : Color.WHITE);
                flashHandler.postDelayed(this, 400);
            }
        };
        flashHandler.post(flashRunnable);
    }

    private void stopFlashing() {
        isFlashing = false;
        flashOn = false;
        if (flashRunnable != null) {
            flashHandler.removeCallbacks(flashRunnable);
        }
        rootLayout.setBackgroundColor(Color.WHITE);
    }

    private void startMonitoring() {
        consecutiveHits = 0;
        statusText.setText("Listening");
        stopFlashing();
        detector.start();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopMonitoring() {
        detector.stop();
        stopFlashing();
        statusText.setText("Not Listening");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);git
        if (requestCode == REQUEST_CODE_RECORD_AUDIO
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMonitoring();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopFlashing();
        detector.stop();
    }
}