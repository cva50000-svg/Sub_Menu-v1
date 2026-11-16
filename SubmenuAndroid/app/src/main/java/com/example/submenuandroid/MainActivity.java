package com.example.submenuandroid;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);
        PackageManager pm = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        final List<AppItem> appList = new ArrayList<>();
        int limit = Math.min(resolveInfos.size(), 7);
        for (int i = 0; i < limit; i++) {
            ResolveInfo info = resolveInfos.get(i);
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName())) {
                if (limit < resolveInfos.size()) {
                    limit++;
                }
                continue;
            }
            CharSequence label = info.loadLabel(pm);
            Drawable icon = info.loadIcon(pm);
            appList.add(new AppItem(label.toString(), icon, pkg));
        }

        AppAdapter adapter = new AppAdapter(this, appList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppItem selected = appList.get(position);
                Intent launchIntent = pm.getLaunchIntentForPackage(selected.getPackageName());
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            }
        });
    }
}
