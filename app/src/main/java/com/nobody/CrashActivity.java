package com.nobody.ultraai;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class CrashActivity extends Activity {

    private String trace = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String t = getIntent().getStringExtra("trace");
        if (t != null) {
            trace = t;
        }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.BLACK);
        box.setPadding(24, 64, 24, 24);

        Button copy = new Button(this);
        copy.setText("COPY ERROR");
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTrace();
            }
        });
        box.addView(copy);

        TextView tv = new TextView(this);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(11);
        tv.setTextIsSelectable(true);
        tv.setText("APP CRASHED:\n\n" + trace);

        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        box.addView(sv);
        setContentView(box);
    }

    private void copyTrace() {
        ClipboardManager cm = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("crash", trace));
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }
}
