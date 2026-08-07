package com.mcbe.client;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class OverlayViewController {

    private final Context context;
    private final View root;
    private final WindowManager wm;
    private final WindowManager.LayoutParams params;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<ModuleState> modules = new ArrayList<>();

    private boolean panelVisible = false;
    private String activeCategory = "HUD";

    // Drag state
    private float dragStartX, dragStartY;
    private int dragOriginX, dragOriginY;
    private boolean dragging = false;

    // HUD update runnable
    private Runnable hudUpdater;
    private long startTime;

    public OverlayViewController(Context ctx, View root,
                                 WindowManager wm,
                                 WindowManager.LayoutParams params) {
        this.context = ctx;
        this.root = root;
        this.wm = wm;
        this.params = params;
        initModules();
    }

    public void start() {
        startTime = System.currentTimeMillis();
        setupTouchHandling();
        setupTabButtons();
        updateModuleList();
        startHudUpdates();
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
    }

    // --------------------------------------------------------
    //  Module registry
    // --------------------------------------------------------
    private void initModules() {
        // HUD modules
        modules.add(new ModuleState("FPS Display",    "HUD",      true));
        modules.add(new ModuleState("Coordinates",    "HUD",      true));
        modules.add(new ModuleState("CPS Counter",    "HUD",      false));
        modules.add(new ModuleState("Keystrokes",     "HUD",      false));
        modules.add(new ModuleState("Ping Display",   "HUD",      false));
        modules.add(new ModuleState("Clock",          "HUD",      false));

        // Visual modules
        modules.add(new ModuleState("Fullbright",     "Visual",   false));
        modules.add(new ModuleState("Custom Sky",     "Visual",   false));
        modules.add(new ModuleState("No Fog",         "Visual",   false));
        modules.add(new ModuleState("Crosshair",      "Visual",   true));

        // Movement modules
        modules.add(new ModuleState("Sprint View",    "Movement", false));
        modules.add(new ModuleState("Step Info",      "Movement", false));

        // Utility modules
        modules.add(new ModuleState("FPS Boost",      "Utility",  true));
        modules.add(new ModuleState("Pack Changer",   "Utility",  false));
        modules.add(new ModuleState("Auto Sneak",     "Utility",  false));
    }

    // --------------------------------------------------------
    //  Touch handling: tap HUD pip to open panel, drag header
    // --------------------------------------------------------
    private void setupTouchHandling() {
        View pip     = root.findViewById(R.id.hud_pip);
        View header  = root.findViewById(R.id.panel_header);
        View panel   = root.findViewById(R.id.panel_container);

        // Tap the small HUD pip to toggle the panel
        pip.setOnClickListener(v -> {
            panelVisible = !panelVisible;
            panel.setVisibility(panelVisible ? View.VISIBLE : View.GONE);
        });

        // Drag the panel by its header
        header.setOnTouchListener((v, ev) -> {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartX  = ev.getRawX();
                    dragStartY  = ev.getRawY();
                    dragOriginX = params.x;
                    dragOriginY = params.y;
                    dragging    = true;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (dragging) {
                        params.x = dragOriginX + (int)(ev.getRawX() - dragStartX);
                        params.y = dragOriginY + (int)(ev.getRawY() - dragStartY);
                        wm.updateViewLayout(root, params);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    dragging = false;
                    return true;
            }
            return false;
        });
    }

    // --------------------------------------------------------
    //  Category tabs
    // --------------------------------------------------------
    private void setupTabButtons() {
        int[] tabIds = {
            R.id.tab_hud, R.id.tab_visual,
            R.id.tab_movement, R.id.tab_utility
        };
        String[] tabNames = { "HUD", "Visual", "Movement", "Utility" };

        for (int i = 0; i < tabIds.length; i++) {
            String cat = tabNames[i];
            View tab = root.findViewById(tabIds[i]);
            if (tab != null) {
                tab.setOnClickListener(v -> {
                    activeCategory = cat;
                    updateTabHighlight(tabIds, tabNames, cat);
                    updateModuleList();
                });
            }
        }
    }

    private void updateTabHighlight(int[] ids, String[] names, String active) {
        for (int i = 0; i < ids.length; i++) {
            View tab = root.findViewById(ids[i]);
            if (tab != null) {
                tab.setSelected(names[i].equals(active));
            }
        }
    }

    // --------------------------------------------------------
    //  Module list rendering
    // --------------------------------------------------------
    private void updateModuleList() {
        LinearLayout list = root.findViewById(R.id.module_list);
        if (list == null) return;
        list.removeAllViews();

        for (ModuleState mod : modules) {
            if (!mod.category.equals(activeCategory)) continue;

            View row = View.inflate(context, R.layout.module_row, null);

            TextView name    = row.findViewById(R.id.module_name);
            View     toggle  = row.findViewById(R.id.module_toggle);
            View     indicator = row.findViewById(R.id.toggle_indicator);

            name.setText(mod.name);
            updateToggleVisual(toggle, indicator, mod.enabled);

            row.setOnClickListener(v -> {
                mod.enabled = !mod.enabled;
                updateToggleVisual(toggle, indicator, mod.enabled);
            });

            list.addView(row);
        }
    }

    private void updateToggleVisual(View track, View knob, boolean on) {
        track.setSelected(on);
        knob.setSelected(on);
    }

    // --------------------------------------------------------
    //  Live HUD updates (FPS, coordinates, CPS, clock)
    // --------------------------------------------------------
    private void startHudUpdates() {
        hudUpdater = new Runnable() {
            private int    frameCount = 0;
            private long   lastFpsTime = System.currentTimeMillis();
            private float  displayFps  = 0f;

            @Override
            public void run() {
                frameCount++;
                long now = System.currentTimeMillis();

                // Recalculate FPS every second
                if (now - lastFpsTime >= 1000) {
                    displayFps  = frameCount * 1000f / (now - lastFpsTime);
                    frameCount  = 0;
                    lastFpsTime = now;
                }

                updateHudText(displayFps, now);
                handler.postDelayed(this, 50); // 20 times per second
            }
        };
        handler.post(hudUpdater);
    }

    private void updateHudText(float fps, long now) {
        // FPS pip (always visible)
        TextView pipFps = root.findViewById(R.id.pip_fps);
        if (pipFps != null && isEnabled("FPS Display")) {
            pipFps.setText(String.format("%.0f FPS", fps));
            pipFps.setVisibility(View.VISIBLE);
        } else if (pipFps != null) {
            pipFps.setVisibility(View.GONE);
        }

        // Coordinates (placeholder - real values come from native when you
        // have offsets; for now shows 0,0,0 until native fills them in)
        TextView coords = root.findViewById(R.id.pip_coords);
        if (coords != null && isEnabled("Coordinates")) {
            float[] pos = ClientBridge.getPlayerPosition();
            coords.setText(String.format("X: %.1f  Y: %.1f  Z: %.1f",
                pos[0], pos[1], pos[2]));
            coords.setVisibility(View.VISIBLE);
        } else if (coords != null) {
            coords.setVisibility(View.GONE);
        }

        // Clock
        TextView clock = root.findViewById(R.id.pip_clock);
        if (clock != null && isEnabled("Clock")) {
            java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("HH:mm:ss",
                    java.util.Locale.getDefault());
            clock.setText(sdf.format(new java.util.Date(now)));
            clock.setVisibility(View.VISIBLE);
        } else if (clock != null) {
            clock.setVisibility(View.GONE);
        }

        // CPS counter
        TextView cps = root.findViewById(R.id.pip_cps);
        if (cps != null && isEnabled("CPS Counter")) {
            cps.setText(ClientBridge.getCps() + " CPS");
            cps.setVisibility(View.VISIBLE);
        } else if (cps != null) {
            cps.setVisibility(View.GONE);
        }
    }

    private boolean isEnabled(String name) {
        for (ModuleState m : modules) {
            if (m.name.equals(name)) return m.enabled;
        }
        return false;
    }

    // --------------------------------------------------------
    //  Simple module state container
    // --------------------------------------------------------
    static class ModuleState {
        String  name;
        String  category;
        boolean enabled;

        ModuleState(String name, String category, boolean enabled) {
            this.name     = name;
            this.category = category;
            this.enabled  = enabled;
        }
    }
}
