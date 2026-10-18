package com.nobody.ultraai;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class LogActivity extends Activity {
    private TextView tv;
    private volatile String shown = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.BLACK);
        box.setPadding(24, 64, 24, 24);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button refresh = new Button(this);
        refresh.setText("REFRESH");
        refresh.setOnClickListener(v -> load());

        Button copy = new Button(this);
        copy.setText("COPY");
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("LOGS", shown));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        });

        row.addView(refresh);
        row.addView(copy);
        box.addView(row);

        tv = new TextView(this);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(12);
        tv.setTextIsSelectable(true);

        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        box.addView(sv);

        setContentView(box);
        load();
    }

    private void load() {
        tv.setText("Loading...");
        new Thread(() -> {
            StringBuilder head = new StringBuilder();
            head.append("LOG VIEWER OK\n");
            head.append("Android ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
            head.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");

            try {
                PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                head.append("app version: ").append(pi.versionName).append("\n");
            } catch (Throwable e) {
                head.append("app info error: ").append(e).append("\n");
            }

            try {
                PackageInfo wv = WebView.getCurrentWebViewPackage();
                head.append("WebView: ").append(wv == null ? "NONE" : wv.packageName + " v" + wv.versionName).append("\n");
            } catch (Throwable e) {
                head.append("WebView info error: ").append(e).append("\n");
            }

            List<String> all = new ArrayList<>();
            List<String> key = new ArrayList<>();

            try {
                Process p = Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-v", "threadtime", "*:T", "1500"});
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    all.add(line);
                    String lc = line.toLowerCase(Locale.ROOT);
                    if (lc.contains("androidruntime") ||
                        lc.contains("fatal") ||
                        lc.contains("signal") ||
                        lc.contains("crash") ||
                        lc.contains("error") ||
                        lc.contains("anr") ||
                        lc.contains("denied") ||
                        lc.contains("killed")) {
                        key.add(line);
                    }
                }
            } catch (Throwable e) {
                key.add("logcat error: " + e);
            }

            StringBuilder out = new StringBuilder(head);
            out.append("\n=== KEY LINES (").append(key.size()).append(") ===\n");
            int ks = Math.max(0, key.size() - 250);
            for (int i = ks; i < key.size(); i++) {
                out.append(key.get(i)).append("\n");
            }

            out.append("\n=== FULL TAIL ===\n");
            int as = Math.max(0, all.size() - 250);
            for (int i = as; i < all.size(); i++) {
                out.append(all.get(i)).append("\n");
            }

            final String res = out.toString();
            shown = res;
            runOnUiThread(() -> tv.setText(res));
        }).start();
    }
}
