package com.mcbe.client;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.View;
import androidx.core.app.NotificationCompat;

public class OverlayService extends Service {

    private static final String CHANNEL_ID = "client_overlay";
    private WindowManager windowManager;
    private View overlayView;
    private OverlayViewController controller;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        showOverlay();
    }

    private void showOverlay() {
        overlayView = LayoutInflater.from(this)
            .inflate(R.layout.overlay_hud, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 16;
        params.y = 60;

        windowManager.addView(overlayView, params);

        // Wire up the controller (handles drag, module toggling, HUD updates)
        controller = new OverlayViewController(
            this, overlayView, windowManager, params);
        controller.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (controller != null) controller.stop();
        if (overlayView != null) windowManager.removeView(overlayView);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        NotificationChannel ch = new NotificationChannel(
            CHANNEL_ID, "Client Overlay", NotificationManager.IMPORTANCE_LOW);
        ch.setDescription("Keeps the in-game HUD running");
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Client running")
            .setContentText("HUD active - open Minecraft")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }
}
