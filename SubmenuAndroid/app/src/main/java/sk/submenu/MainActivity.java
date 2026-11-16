package sk.submenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int SLOT_COUNT = 5;
    private static final String PREFS = "submenu_settings";
    private static final String[] DEFAULT_LABELS = {
        "Chrome", "CG Transit", "Gmail", "Hodiny", "Kalendár"
    };
    private static final String[] DEFAULT_PACKAGES = {
        "com.android.chrome", "", "com.google.android.gm", "", ""
    };

    private SharedPreferences prefs;
    private LinearLayout content;
    private final List<AppEntry> launchableApps = new ArrayList<>();

    private static class AppEntry {
        String label;
        String packageName;
        Intent launchIntent;
        AppEntry(String l, String p, Intent i) {
            label = l; packageName = p; launchIntent = i;
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadApps();
        initializeDefaults();
        buildScreen();
    }

    private void loadApps() {
        launchableApps.clear();
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = getPackageManager().queryIntentActivities(query, 0);
        for (ResolveInfo ri : found) {
            String pkg = ri.activityInfo.packageName;
            String label = ri.loadLabel(getPackageManager()).toString();
            Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
            if (launch != null && !pkg.equals(getPackageName())) {
                launchableApps.add(new AppEntry(label, pkg, launch));
            }
        }
        Collections.sort(launchableApps, Comparator.comparing(a -> a.label.toLowerCase(Locale.ROOT)));
    }

    private void initializeDefaults() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            String key = "slot_" + i;
            if (prefs.contains(key)) continue;
            String wanted = DEFAULT_PACKAGES[i];
            String match = findPackage(wanted);
            if (i == 1) match = findByLabel("CG Transit");
            if (i == 3) match = findByLabel("Hodiny");
            if (i == 4) {
                match = findByLabel("Kalendár");
                if (match == null) match = findByLabel("Calendar");
            }
            prefs.edit().putString(key, match == null ? "" : match).apply();
        }
    }

    private String findPackage(String pkg) {
        if (pkg == null || pkg.isEmpty()) return null;
        for (AppEntry a : launchableApps) if (a.packageName.equals(pkg)) return pkg;
        return null;
    }

    private String findByLabel(String wanted) {
        for (AppEntry a : launchableApps) {
            if (a.label.toLowerCase(Locale.ROOT).contains(wanted.toLowerCase(Locale.ROOT)))
                return a.packageName;
        }
        return null;
    }

    private AppEntry getSelected(int slot) {
        String pkg = prefs.getString("slot_" + slot, "");
        for (AppEntry a : launchableApps) if (a.packageName.equals(pkg)) return a;
        return null;
    }

    private void buildScreen() {
        getWindow().getDecorView().setBackgroundColor(Color.rgb(16, 24, 32));
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(24), dp(20), dp(24));
        scroll.addView(content);
        setContentView(scroll);

        TextView heading = new TextView(this);
        heading.setText("SUBMENU");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(25);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, dp(4), 0, dp(20));
        content.addView(heading);

        for (int i = 0; i < SLOT_COUNT; i++) addSlot(i);

        TextView footer = new TextView(this);
        footer.setText("Vyber aplikaciu. Nastavenia zmenis cez UPRAVIT.");
        footer.setTextColor(Color.LTGRAY);
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(18), 0, 0);
        content.addView(footer);
    }

    private void addSlot(final int slot) {
        AppEntry selected = getSelected(slot);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(14), dp(10), dp(10), dp(10));
        row.setBackground(roundRect(Color.rgb(29, 42, 54), dp(14)));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(72));
        rowParams.bottomMargin = dp(10);
        content.addView(row, rowParams);

        TextView icon = new TextView(this);
        icon.setText(selected == null ? "＋" : selected.label.substring(0, 1).toUpperCase(Locale.ROOT));
        icon.setTextColor(Color.WHITE);
        icon.setTextSize(25);
        icon.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(roundRect(Color.rgb(0, 121, 107), dp(12)));
        row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView name = new TextView(this);
        name.setText(selected == null ? DEFAULT_LABELS[slot] + " (nevybrana)" : selected.label);
        name.setTextColor(Color.WHITE);
        name.setTextSize(17);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setPadding(dp(14), 0, dp(4), 0);
        row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));

        Button edit = new Button(this);
        edit.setText("UPRAVIT");
        edit.setTextSize(10);
        edit.setTextColor(Color.rgb(128, 203, 196));
        edit.setAllCaps(true);
        edit.setBackgroundColor(Color.TRANSPARENT);
        row.addView(edit);
        edit.setOnClickListener(v -> showAppPicker(slot));

        row.setOnClickListener(v -> launchSlot(slot));
    }

    private void showAppPicker(final int slot) {
        final List<AppEntry> choices = new ArrayList<>();
        choices.add(new AppEntry("— Nevybraté —", "", null));
        choices.addAll(launchableApps);
        String[] labels = new String[choices.size()];
        for (int i = 0; i < choices.size(); i++) labels[i] = choices.get(i).label;
        new AlertDialog.Builder(this)
            .setTitle("Vyber aplikaciu: " + DEFAULT_LABELS[slot])
            .setItems(labels, (dialog, which) -> {
                prefs.edit().putString("slot_" + slot, choices.get(which).packageName).apply();
                buildScreen();
            })
            .setNegativeButton("Zrusit", null)
            .show();
    }

    private void launchSlot(int slot) {
        AppEntry selected = getSelected(slot);
        if (selected == null) {
            showAppPicker(slot);
            return;
        }
        Intent launch = getPackageManager().getLaunchIntentForPackage(selected.packageName);
        if (launch == null) {
            new AlertDialog.Builder(this).setMessage("Aplikacia uz nie je dostupna.")
                .setPositiveButton("OK", null).show();
            return;
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(launch);
            finish(); // Po spusteni cielovej aplikacie sa Submenu zatvori.
        } catch (Exception e) {
            new AlertDialog.Builder(this).setMessage("Aplikaciu sa nepodarilo spustit.")
                .setPositiveButton("OK", null).show();
        }
    }

    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
