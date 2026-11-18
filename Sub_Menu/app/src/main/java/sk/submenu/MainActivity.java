package sk.submenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    // Pocet slotov na obrazovke (9 poloziek)
    private static final int SLOT_COUNT = 9;

    // Nazov suboru pre ukladanie nastaveni (SharedPreferences)
    private static final String PREFS = "submenu_settings";

    // Predvolene zobrazovane nazvy pre jednotlive sloty (ak aplikacia este nie je priradena)
    private static final String[] DEFAULT_LABELS = {
        "Nastavenie", "Chrome", "Gmail", "Telefon", "Spravy",
        "Kontakty", "Hodiny", "Kalendar", "Poznamky"
    };

    // Predvolene systemove baliky (package name) pre prve spustenie
    // Prazdne retazce "" sa doplnia automaticky vyhladavanim podla nazvu v initializeDefaults()
    private static final String[] DEFAULT_PACKAGES = {
        "com.android.settings",
        "com.android.chrome",
        "com.google.android.gm",
        "",
        "",
        "",
        "",
        "",
        ""
    };

    private SharedPreferences prefs;
    private LinearLayout content;
    private final List<AppEntry> launchableApps = new ArrayList<>();

    // Struktura pre uchovanie udajov o nainstalovanej aplikacii
    private static class AppEntry {
        String label;        // Zobrazovany nazov aplikacie
        String packageName;  // Systemovy identifikator balika
        Intent launchIntent; // Intent na spustenie aplikacie
        Drawable icon;       // Originalna ikona aplikacie

        AppEntry(String l, String p, Intent i, Drawable ic) {
            label = l;
            packageName = p;
            launchIntent = i;
            icon = ic;
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadApps();
        initializeDefaults();
        buildScreen();
    }

    // Nacita vsetky spustitelne aplikacie zo systemu a zoradi ich podla abecedy
    private void loadApps() {
        launchableApps.clear();
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = getPackageManager().queryIntentActivities(query, 0);
        for (ResolveInfo ri : found) {
            String pkg = ri.activityInfo.packageName;
            String label = ri.loadLabel(getPackageManager()).toString();
            Drawable icon = ri.loadIcon(getPackageManager());
            Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
            if (launch != null && !pkg.equals(getPackageName())) {
                launchableApps.add(new AppEntry(label, pkg, launch, icon));
            }
        }
        Collections.sort(launchableApps, Comparator.comparing(a -> a.label.toLowerCase(Locale.ROOT)));
    }

    // Pri prvom spusteni priradi predvolene aplikacie do slotov
    private void initializeDefaults() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            String key = "slot_" + i;
            if (prefs.contains(key)) continue;

            // Najprv skusi presnu zhodu podla package name
            String match = findPackage(DEFAULT_PACKAGES[i]);

            // Ak sa balik nenasiel (rozni vyrobcovia maju rozne nazvy balikov), hlada podla nazvu ikony
            if (match == null) {
                switch (i) {
                    case 0: // Nastavenie
                        match = findByLabels("Nastaveni", "Settings");
                        break;
                    case 1: // Chrome
                        match = findByLabels("Chrome");
                        break;
                    case 2: // Gmail
                        match = findByLabels("Gmail");
                        break;
                    case 3: // Telefon
                        match = findByLabels("Telefon", "Phone", "Dialer");
                        break;
                    case 4: // Spravy
                        match = findByLabels("Spravy", "Messages", "SMS");
                        break;
                    case 5: // Kontakty
                        match = findByLabels("Kontakty", "Contacts");
                        break;
                    case 6: // Hodiny
                        match = findByLabels("Hodiny", "Clock");
                        break;
                    case 7: // Kalendar
                        match = findByLabels("Kalendar", "Calendar");
                        break;
                    case 8: // Poznamky
                        match = findByLabels("Poznamky", "Notes", "Keep");
                        break;
                }
            }
            prefs.edit().putString(key, match == null ? "" : match).apply();
        }
    }

    // Vyhlada aplikaciu podla presneho nazvu balika
    private String findPackage(String pkg) {
        if (pkg == null || pkg.isEmpty()) return null;
        for (AppEntry a : launchableApps) {
            if (a.packageName.equals(pkg)) return pkg;
        }
        return null;
    }

    // Vyhlada aplikaciu podla jedneho alebo viacerych klucovych slov v nazve (ignoruje diakritiku zakladnym sposobom a velkost pismen)
    private String findByLabels(String... keywords) {
        for (String kw : keywords) {
            String lowerKw = kw.toLowerCase(Locale.ROOT);
            for (AppEntry a : launchableApps) {
                String cleanLabel = stripAccents(a.label).toLowerCase(Locale.ROOT);
                if (cleanLabel.contains(lowerKw)) {
                    return a.packageName;
                }
            }
        }
        return null;
    }

    // Odstrani zakladnu slovensku diakritiku pre spolahlivejsie vyhladavanie nazvov (napr. Spravy vs Správy)
    private String stripAccents(String s) {
        return s.replace("á", "a").replace("ä", "a").replace("č", "c").replace("ď", "d")
                .replace("é", "e").replace("í", "i").replace("ĺ", "l").replace("ľ", "l")
                .replace("ň", "n").replace("ó", "o").replace("ô", "o").replace("ŕ", "r")
                .replace("š", "s").replace("ť", "t").replace("ú", "u").replace("ý", "y")
                .replace("ž", "z")
                .replace("Á", "A").replace("Ä", "A").replace("Č", "C").replace("Ď", "D")
                .replace("É", "E").replace("Í", "I").replace("Ĺ", "L").replace("Ľ", "L")
                .replace("Ň", "N").replace("Ó", "O").replace("Ô", "O").replace("Ŕ", "R")
                .replace("Š", "S").replace("Ť", "T").replace("Ú", "U").replace("Ý", "Y")
                .replace("Ž", "Z");
    }

    // Vrati priradenu aplikaciu pre dany slot
    private AppEntry getSelected(int slot) {
        String pkg = prefs.getString("slot_" + slot, "");
        for (AppEntry a : launchableApps) {
            if (a.packageName.equals(pkg)) return a;
        }
        return null;
    }

    // Vytvori alebo prekresli celu obrazovku
    private void buildScreen() {
        getWindow().getDecorView().setBackgroundColor(Color.rgb(16, 24, 32));

        if (content == null) {
            ScrollView scroll = new ScrollView(this);
            content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            // Odsadenie celeho zoznamu od okrajov displeja (lavy, horny, pravy, dolny)
            content.setPadding(dp(16), dp(16), dp(16), dp(16));
            scroll.addView(content);
            setContentView(scroll);
        } else {
            content.removeAllViews();
        }

        // Nadpis obrazovky
        TextView heading = new TextView(this);
        heading.setText("SUBMENU");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(22);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, dp(2), 0, dp(14));
        content.addView(heading);

        // Vykreslenie vsetkych 9 slotov
        for (int i = 0; i < SLOT_COUNT; i++) {
            addSlot(i);
        }

        // Spodna napoveda
        TextView footer = new TextView(this);
        footer.setText("Vyber aplikaciu. Nastavenia zmenis cez UPRAVIT.");
        footer.setTextColor(Color.LTGRAY);
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(12), 0, dp(4));
        content.addView(footer);
    }

    // Vykresli jeden uzsi riadok (slot)
    private void addSlot(final int slot) {
        AppEntry selected = getSelected(slot);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        // Vnutorne odsadenie v ramci jedneho riadku (zmensene pre uzsi vzhlad)
        row.setPadding(dp(12), dp(6), dp(8), dp(6));
        // Farba karty slotu a zaoblenie rohov
        row.setBackground(roundRect(Color.rgb(29, 42, 54), dp(12)));

        // Vyska riadku nastavena na 54dp (povodne 72dp) a medzera pod riadkom na 6dp (povodne 10dp)
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        rowParams.bottomMargin = dp(6);
        content.addView(row, rowParams);

        // Ikona aplikacie (velkost 36x36 dp, povodne 48x48 dp)
        if (selected != null && selected.icon != null) {
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(selected.icon);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));
        } else {
            TextView icon = new TextView(this);
            icon.setText("+");
            icon.setTextColor(Color.WHITE);
            icon.setTextSize(20);
            icon.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            icon.setGravity(Gravity.CENTER);
            icon.setBackground(roundRect(Color.rgb(0, 121, 107), dp(10)));
            row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));
        }

        // Nazov aplikacie v slote
        TextView name = new TextView(this);
        name.setText(selected == null ? DEFAULT_LABELS[slot] + " (nevybrana)" : selected.label);
        name.setTextColor(Color.WHITE);
        name.setTextSize(15);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setPadding(dp(12), 0, dp(4), 0);
        row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Tlacidlo UPRAVIT
        Button edit = new Button(this);
        edit.setText("UPRAVIT");
        edit.setTextSize(10);
        edit.setTextColor(Color.rgb(128, 203, 196));
        edit.setAllCaps(true);
        edit.setBackgroundColor(Color.TRANSPARENT);
        edit.setMinimumHeight(0);
        edit.setMinHeight(0);
        edit.setPadding(dp(8), dp(4), dp(8), dp(4));
        row.addView(edit);
        edit.setOnClickListener(v -> showAppPicker(slot));

        // Kliknutie na riadok spusti aplikaciu
        row.setOnClickListener(v -> launchSlot(slot));
    }

    // Otvori dialogove okno na vyber aplikacie pre zvoleny slot
    private void showAppPicker(final int slot) {
        final List<AppEntry> choices = new ArrayList<>();
        choices.add(new AppEntry("--- Nevybrate ---", "", null, null));
        choices.addAll(launchableApps);

        String[] labels = new String[choices.size()];
        for (int i = 0; i < choices.size(); i++) {
            labels[i] = choices.get(i).label;
        }

        new AlertDialog.Builder(this)
            .setTitle("Vyber aplikaciu: " + DEFAULT_LABELS[slot])
            .setItems(labels, (dialog, which) -> {
                prefs.edit().putString("slot_" + slot, choices.get(which).packageName).apply();
                buildScreen();
            })
            .setNegativeButton("Zrusit", null)
            .show();
    }

    // Spusti zvolenu aplikaciu a ukonci Submenu
    private void launchSlot(int slot) {
        AppEntry selected = getSelected(slot);
        if (selected == null) {
            showAppPicker(slot);
            return;
        }
        Intent launch = getPackageManager().getLaunchIntentForPackage(selected.packageName);
        if (launch == null) {
            new AlertDialog.Builder(this)
                .setMessage("Aplikacia uz nie je dostupna.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(launch);
            finish();
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                .setMessage("Aplikaciu sa nepodarilo spustit.")
                .setPositiveButton("OK", null)
                .show();
        }
    }

    // Vytvori zaobleny obdlznik pre pozadie prvkov
    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    // Prepocet DP na realne pixely displeja
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}