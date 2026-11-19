package sk.menu.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int SLOT_COUNT = 12;
    private static final String PREFS_NAME = "menu_launcher_prefs";
    private static final String KEY_INITIALIZED = "initialized_v2";
    private static final String KEY_SLOT_PREFIX = "slot_pkg_";

    private SharedPreferences prefs;
    private PackageManager pm;
    private boolean editMode = false;

    private TextView btnEditMode;
    private TextView tvSubtitle;
    private final LinearLayout[] slotCards = new LinearLayout[SLOT_COUNT];
    private final ImageView[] slotIcons = new ImageView[SLOT_COUNT];
    private final TextView[] slotTitles = new TextView[SLOT_COUNT];
    private final TextView[] slotBadges = new TextView[SLOT_COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        pm = getPackageManager();

        initDefaultSlotsIfNeeded();
        setContentView(buildMainLayout());
        refreshSlotsUI();
    }

    private void initDefaultSlotsIfNeeded() {
        if (prefs.getBoolean(KEY_INITIALIZED, false)) {
            return;
        }

        List<AppItem> installed = loadLaunchableApps();

        // Preddefinovane budu iba prve 3 pozicie, zvysok ostane prazdny
        String[][] defaultQueries = new String[][]{
                {"gmail", "gm"},
                {"chrome"},
                {"setting", "settings", "nastavenia"}
        };

        SharedPreferences.Editor editor = prefs.edit();
        Set<String> usedPackages = new HashSet<>();

        for (int i = 0; i < defaultQueries.length && i < SLOT_COUNT; i++) {
            String foundPkg = findBestMatch(installed, defaultQueries[i], usedPackages);
            if (foundPkg != null) {
                editor.putString(KEY_SLOT_PREFIX + i, foundPkg);
                usedPackages.add(foundPkg);
            }
        }

        editor.putBoolean(KEY_INITIALIZED, true);
        editor.apply();
    }

    private String findBestMatch(List<AppItem> apps, String[] keywords, Set<String> used) {
        for (String kw : keywords) {
            String lowerKw = kw.toLowerCase(Locale.ROOT);
            for (AppItem app : apps) {
                if (used.contains(app.packageName)) continue;
                if (app.labelLower.contains(lowerKw) || app.packageLower.contains(lowerKw)) {
                    return app.packageName;
                }
            }
        }
        return null;
    }

    private View buildMainLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(8), dp(8), dp(8), dp(8));

        root.setBackgroundColor(Color.parseColor("#CC080E1A"));
        root.setOnClickListener(v -> finish());

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(10), dp(10), dp(12));
        panel.setClickable(true);

        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(Color.parseColor("#0F192B"));
        panelBg.setCornerRadius(dp(22));
        panelBg.setStroke(dp(2), Color.parseColor("#28F0E8"));
        panel.setBackground(panelBg);

        root.addView(panel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(4), 0, dp(4), dp(6));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("MENU");
        tvTitle.setTextColor(Color.parseColor("#E2F8FF"));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);

        tvSubtitle = new TextView(this);
        tvSubtitle.setText("Podrzte ikonu pre zmenu");
        tvSubtitle.setTextColor(Color.parseColor("#8CA3C0"));
        tvSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

        titleBox.addView(tvTitle);
        titleBox.addView(tvSubtitle);

        btnEditMode = new TextView(this);
        btnEditMode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        btnEditMode.setTypeface(Typeface.DEFAULT_BOLD);
        btnEditMode.setPadding(dp(8), dp(4), dp(8), dp(4));
        btnEditMode.setOnClickListener(v -> toggleEditMode());
        styleEditButton(false);

        header.addView(titleBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(btnEditMode);
        panel.addView(header);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setRowCount(4);

        for (int i = 0; i < SLOT_COUNT; i++) {
            final int slotIndex = i;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setPadding(dp(4), dp(6), dp(4), dp(6));

            GridLayout.LayoutParams cardParams = new GridLayout.LayoutParams();
            cardParams.width = 0;
            cardParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            cardParams.columnSpec = GridLayout.spec(i % 3, 1f);
            cardParams.rowSpec = GridLayout.spec(i / 3);
            cardParams.setMargins(dp(2), dp(2), dp(2), dp(2));

            ImageView iconView = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(36), dp(36));
            iconParams.bottomMargin = dp(2);
            iconView.setLayoutParams(iconParams);

            TextView nameView = new TextView(this);
            nameView.setTextColor(Color.parseColor("#F1F5F9"));
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            nameView.setTypeface(Typeface.DEFAULT_BOLD);
            nameView.setGravity(Gravity.CENTER);
            nameView.setMaxLines(1);
            nameView.setEllipsize(TextUtils.TruncateAt.END);

            TextView badgeView = new TextView(this);
            badgeView.setText("Zmenit");
            badgeView.setTextColor(Color.parseColor("#F59E0B"));
            badgeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
            badgeView.setTypeface(Typeface.DEFAULT_BOLD);
            badgeView.setPadding(0, dp(1), 0, 0);
            badgeView.setVisibility(View.GONE);

            card.addView(iconView);
            card.addView(nameView);
            card.addView(badgeView);

            card.setOnClickListener(v -> handleSlotClick(slotIndex));
            card.setOnLongClickListener(v -> {
                openAppPickerDialog(slotIndex);
                return true;
            });

            slotCards[i] = card;
            slotIcons[i] = iconView;
            slotTitles[i] = nameView;
            slotBadges[i] = badgeView;

            grid.addView(card, cardParams);
        }

        panel.addView(grid);
        return root;
    }

    private void toggleEditMode() {
        editMode = !editMode;
        styleEditButton(editMode);
        tvSubtitle.setText(editMode ? "Tuknite na poziciu pre vyber" : "Podrzte ikonu pre zmenu");
        refreshSlotsUI();
    }

    private void styleEditButton(boolean active) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(10));
        if (active) {
            bg.setColor(Color.parseColor("#F59E0B"));
            btnEditMode.setText("HOTOVO");
            btnEditMode.setTextColor(Color.parseColor("#0B1322"));
        } else {
            bg.setColor(Color.parseColor("#1B2D4B"));
            bg.setStroke(dp(1), Color.parseColor("#38BDF8"));
            btnEditMode.setText("UPRAVIT");
            btnEditMode.setTextColor(Color.parseColor("#38BDF8"));
        }
        btnEditMode.setBackground(bg);
    }

    private void styleCard(LinearLayout card, boolean isAssigned) {
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setCornerRadius(dp(12));
        if (editMode) {
            cardBg.setColor(Color.parseColor("#1D2B42"));
            cardBg.setStroke(dp(2), Color.parseColor("#F59E0B"));
        } else if (isAssigned) {
            cardBg.setColor(Color.parseColor("#172742"));
            cardBg.setStroke(dp(1), Color.parseColor("#2B4C7E"));
        } else {
            cardBg.setColor(Color.parseColor("#111D30"));
            cardBg.setStroke(dp(1), Color.parseColor("#334155"));
        }
        card.setBackground(cardBg);
    }

    private void refreshSlotsUI() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            String pkg = prefs.getString(KEY_SLOT_PREFIX + i, null);
            boolean assigned = false;

            if (pkg != null) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                    slotIcons[i].setImageDrawable(pm.getApplicationIcon(info));
                    slotTitles[i].setText(pm.getApplicationLabel(info));
                    slotTitles[i].setTextColor(Color.parseColor("#F1F5F9"));
                    assigned = true;
                } catch (PackageManager.NameNotFoundException e) {
                    prefs.edit().remove(KEY_SLOT_PREFIX + i).apply();
                }
            }

            if (!assigned) {
                slotIcons[i].setImageResource(android.R.drawable.ic_input_add);
                slotIcons[i].setColorFilter(Color.parseColor("#38BDF8"));
                slotTitles[i].setText("Vybrat");
                slotTitles[i].setTextColor(Color.parseColor("#94A3B8"));
            } else {
                slotIcons[i].clearColorFilter();
            }

            slotBadges[i].setVisibility(editMode ? View.VISIBLE : View.GONE);
            styleCard(slotCards[i], assigned);
        }
    }

    private void handleSlotClick(int slotIndex) {
        if (editMode) {
            openAppPickerDialog(slotIndex);
            return;
        }

        String pkg = prefs.getString(KEY_SLOT_PREFIX + slotIndex, null);
        if (pkg == null) {
            openAppPickerDialog(slotIndex);
            return;
        }

        Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
            finish();
        } else {
            Toast.makeText(this, "Aplikaciu nemožno spustit", Toast.LENGTH_SHORT).show();
            openAppPickerDialog(slotIndex);
        }
    }

    private void openAppPickerDialog(int slotIndex) {
        List<AppItem> allApps = loadLaunchableApps();
        List<AppItem> filteredApps = new ArrayList<>(allApps);

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(dp(14), dp(10), dp(14), dp(6));

        EditText searchInput = new EditText(this);
        searchInput.setHint("Hladat...");
        searchInput.setSingleLine(true);
        dialogLayout.addView(searchInput);

        ListView listView = new ListView(this);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(300)
        );
        listParams.topMargin = dp(6);
        dialogLayout.addView(listView, listParams);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Pozicia " + (slotIndex + 1))
                .setView(dialogLayout)
                .setNegativeButton("Zrusit", null)
                .setNeutralButton("Vymazat", (d, which) -> {
                    prefs.edit().remove(KEY_SLOT_PREFIX + slotIndex).apply();
                    refreshSlotsUI();
                })
                .create();

        ArrayAdapter<AppItem> adapter = new ArrayAdapter<AppItem>(this, 0, filteredApps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LinearLayout row;
                ImageView icon;
                TextView text;

                if (convertView == null) {
                    row = new LinearLayout(MainActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(6), dp(8), dp(6), dp(8));

                    icon = new ImageView(MainActivity.this);
                    LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(32), dp(32));
                    ip.rightMargin = dp(10);
                    icon.setLayoutParams(ip);
                    icon.setId(android.R.id.icon);

                    text = new TextView(MainActivity.this);
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    text.setId(android.R.id.text1);

                    row.addView(icon);
                    row.addView(text);
                } else {
                    row = (LinearLayout) convertView;
                    icon = row.findViewById(android.R.id.icon);
                    text = row.findViewById(android.R.id.text1);
                }

                AppItem item = getItem(position);
                if (item != null) {
                    if (item.cachedIcon == null) {
                        item.cachedIcon = item.resolveInfo.loadIcon(pm);
                    }
                    icon.setImageDrawable(item.cachedIcon);
                    text.setText(item.label);
                }
                return row;
            }
        };

        listView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().toLowerCase(Locale.ROOT).trim();
                filteredApps.clear();
                for (AppItem app : allApps) {
                    if (app.labelLower.contains(q) || app.packageLower.contains(q)) {
                        filteredApps.add(app);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AppItem selected = filteredApps.get(position);
            prefs.edit().putString(KEY_SLOT_PREFIX + slotIndex, selected.packageName).apply();
            refreshSlotsUI();
            dialog.dismiss();
        });

        dialog.show();
    }

    private List<AppItem> loadLaunchableApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        List<AppItem> result = new ArrayList<>(resolveInfos.size());
        Set<String> seenPackages = new HashSet<>();
        String myPackage = getPackageName();

        for (ResolveInfo ri : resolveInfos) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(myPackage)) {
                // Ponechavame unikatnost
            }
            if (seenPackages.contains(pkg)) continue;
            seenPackages.add(pkg);
            String label = ri.loadLabel(pm).toString();
            result.add(new AppItem(label, pkg, ri));
        }

        Collections.sort(result, (a, b) -> a.label.compareToIgnoreCase(b.label));
        return result;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()
        ));
    }

    private static class AppItem {
        final String label;
        final String labelLower;
        final String packageName;
        final String packageLower;
        final ResolveInfo resolveInfo;
        Drawable cachedIcon;

        AppItem(String label, String packageName, ResolveInfo resolveInfo) {
            this.label = label;
            this.labelLower = label.toLowerCase(Locale.ROOT);
            this.packageName = packageName;
            this.packageLower = packageName.toLowerCase(Locale.ROOT);
            this.resolveInfo = resolveInfo;
        }
    }
}
