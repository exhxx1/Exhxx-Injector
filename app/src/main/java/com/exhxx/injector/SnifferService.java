package com.exhxx.injector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import java.io.*;

public class SnifferService extends Service {
    public static boolean isRunning = false;
    private Process process;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("sniffer", "Exhxx Radar", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification notification = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
            new Notification.Builder(this, "sniffer") : new Notification.Builder(this))
                .setContentTitle("Exhxx Radar")
                .setContentText("المراقبة تعمل الآن...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);
        isRunning = true;
        sendBroadcast(new Intent("EXHXX_UPDATE_UI"));

        new Thread(() -> {
            try {
                // إعداد مجلد الحفظ
                File dir = new File("/sdcard/Exhxx_Dump");
                if (!dir.exists()) dir.mkdirs();

                // تشغيل logcat -v process يطبع اسم التطبيق (أو الـ PID) بكل سطر
                // هذا الأمر خفيف جداً وما يستهلك المعالج
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -v process"});
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                String line;
                while (isRunning && (line = reader.readLine()) != null) {
                    // فرز ذكي وسريع: كل سطر يحتوي على اسم التطبيق أو الـ PID
                    // راح نكتبه في ملف عام، وMT Manager راح يفرزه لك بلمح البصر
                    FileWriter fw = new FileWriter(new File(dir, "live_all_logs.txt"), true);
                    fw.write(line + "\n");
                    fw.flush();
                    fw.close();
                }
            } catch (Exception e) {}
        }).start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (process != null) process.destroy();
        sendBroadcast(new Intent("EXHXX_UPDATE_UI"));
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
