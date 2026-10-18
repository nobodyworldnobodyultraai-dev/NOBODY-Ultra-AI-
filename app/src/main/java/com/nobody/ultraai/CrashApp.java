package com.nobody.ultraai;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 28) {
            String p = Application.getProcessName();
            if (p != null && p.endsWith(":crash")) {
                return;
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                report(t, e);
            }
        });
    }

    private void report(Thread t, Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String text = "Thread: " + t.getName() + "\n\n" + sw;
            if (text.length() > 20000) {
                text = text.substring(0, 20000);
            }
            Intent i = new Intent(this, CrashActivity.class);
            i.putExtra("trace", text);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
