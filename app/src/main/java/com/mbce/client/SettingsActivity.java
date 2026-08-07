package com.mcbe.client;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mcbe.client.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        setupFpsSlider();
    }

    private void setupFpsSlider() {
        binding.seekFps.setMax(3); // 0=30, 1=60, 2=90, 3=120
        binding.seekFps.setProgress(1); // default 60
        updateFpsLabel(60);

        binding.seekFps.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar s, int p, boolean user) {
                    int fps = progressToFps(p);
                    updateFpsLabel(fps);
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
    }

    private int progressToFps(int p) {
        switch (p) {
            case 0: return 30;
            case 1: return 60;
            case 2: return 90;
            default: return 120;
        }
    }

    private void updateFpsLabel(int fps) {
        binding.tvFpsValue.setText("FPS Cap: " + fps);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
