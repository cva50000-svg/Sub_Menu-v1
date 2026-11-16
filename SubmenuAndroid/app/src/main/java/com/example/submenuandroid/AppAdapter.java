package com.example.submenuandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class AppAdapter extends ArrayAdapter<AppItem> {
    public AppAdapter(Context context, List<AppItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_app, parent, false);
        }
        AppItem item = getItem(position);
        ImageView iconView = convertView.findViewById(R.id.appIcon);
        TextView nameView = convertView.findViewById(R.id.appName);

        if (item != null) {
            iconView.setImageDrawable(item.getIcon());
            nameView.setText(item.getName());
        }
        return convertView;
    }
}
