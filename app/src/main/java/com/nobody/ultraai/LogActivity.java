package com.nobody.ultraai;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load();
            }
        });

        Button copy = new Button(this);
        copy.setText("COPY");
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyLog();
            }
        });

        row.addView(refresh);
        row.addView(copy);
        box.addView(row);

        tv = new TextView(this);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(10);
        tv.setTextIsSelectable(true);

        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        box.addView(sv);
        setContentView(box);

        load();
    }

    private void copyLog() {
        ClipboardManager cm = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("log", shown));
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }

    private void load() {
        tv.setText("loading...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String res = collect();
                shown = res;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(res);
                    }
                });
            }
        }, "log-reader").start();
    }

    private boolean isKey(String line) {
        String l = line.toLowerCase(Locale.ROOT);
        String[] words = {
            "fatal", "androidruntime", "exception", "signal",
            "anr", "died", "nobody", "ultraai", "error",
            "crash", "denied", "killed"
        };
        for (String w : words) {
            if (l.contains(w)) {
                return true;
            }
        }
        return false;
    }

    private String header() {
        StringBuilder h = new StringBuilder();
        h.append("LOG VIEWER OK\n");
        h.append("Android ").append(Build.VERSION.RELEASE);
        h.append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        h.append("Device: ").append(Build.MANUFACTURER);
        h.append(" ").append(Build.MODEL).append("\n");
        try {
            PackageInfo pi = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            h.append("App version: ").append(pi.versionName).append("\n");
        } catch (Throwable e) {
            h.append("pkg info err: ").append(e).append("\n");
        }
        try {
            PackageInfo wv = android.webkit.WebView
                    .getCurrentWebViewPackage();
            if (wv == null) {
                h.append("WebView: NONE\n");
            } else {
                h.append("WebView: ").append(wv.packageName);
                h.append(" v").append(wv.versionName).append("\n");
            }
        } catch (Throwable e) {
            h.append("WebView info err: ").append(e).append("\n");
        }
        return h.toString();
    }

    private String collect() {
        List<String> all = new ArrayList<String>();
        List<String> key = new ArrayList<String>();
        try {
            String[] cmd = {
                "logcat", "-d", "-v", "threadtime", "-t", "1500"
            };
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                all.add(line);
                if (isKey(line)) {
                    key.add(line);
                }
            }
        } catch (Throwable e) {
            key.add("logcat error: " + e);
        }

        StringBuilder out = new StringBuilder(header());
        out.append("\n=== KEY LINES (").append(key.size());
        out.append(") ===\n");
        int ks = Math.max(0, key.size() - 250);
        for (int i = ks; i < key.size(); i++) {
            out.append(key.get(i)).append("\n");
        }
        out.append("\n=== FULL TAIL ===\n");
        int as = Math.max(0, all.size() - 250);
        for (int i = as; i < all.size(); i++) {
            out.append(all.get(i)).append("\n");
        }
        return out.toString();
    }
}
