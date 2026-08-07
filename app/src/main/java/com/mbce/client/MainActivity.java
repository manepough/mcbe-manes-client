package com.mcbe.client;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mcbe.client.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_OVERLAY = 1001;
    private ActivityMainBinding binding;

    // Minecraft Bedrock package name
    private static final String MC_PACKAGE = "com.mojang.minecraftpe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLaunch.setOnClickListener(v -> handleLaunch());
        binding.btnSettings.setOnClickListener(v ->
            startActivity(new Intent(this, SettingsActivity.class)));

        updateMinecraftStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMinecraftStatus();
    }

    private void handleLaunch() {
        // Step 1: check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this,
                "Grant overlay permission to show the HUD",
                Toast.LENGTH_LONG).show();
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_OVERLAY);
            return;
        }

        // Step 2: check Minecraft is installed
        if (!isMinecraftInstalled()) {
            Toast.makeText(this,
                "Minecraft is not installed",
                Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 3: start overlay service
        Intent service = new Intent(this, OverlayService.class);
        startForegroundService(service);

        // Step 4: launch Minecraft
        launchMinecraft();
    }

    private void launchMinecraft() {
        Intent intent = getPackageManager()
            .getLaunchIntentForPackage(MC_PACKAGE);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private boolean isMinecraftInstalled() {
        try {
            getPackageManager().getPackageInfo(MC_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void updateMinecraftStatus() {
        boolean installed = isMinecraftInstalled();
        boolean overlayOk = Settings.canDrawOverlays(this);

        binding.tvMinecraftStatus.setText(
            installed ? "Minecraft: Installed" : "Minecraft: Not found");
        binding.tvOverlayStatus.setText(
            overlayOk ? "Overlay: Granted" : "Overlay: Not granted");
        binding.btnLaunch.setEnabled(installed);
    }

    @Override
    protected void onActivityResult(int req, int result, Intent data) {
        super.onActivityResult(req, result, data);
        if (req == REQ_OVERLAY) updateMinecraftStatus();
    }
}
