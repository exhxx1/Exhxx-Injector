package com.exhxx.injector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class SnifferService extends Service {
    private Process process;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String keyword = intent.getStringExtra("keyword");
        if (keyword == null) keyword = "";

        // صناعة الإشعار الثابت (مثل التيرموكس)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("sniffer", "Exhxx Radar", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
            new Notification.Builder(this, "sniffer") : new Notification.Builder(this);
        
        Notification notification = builder
                .setContentTitle("🔥 Exhxx Radar")
                .setContentText("المراقبة الشاملة تعمل في الخلفية الآن...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);

        // تشغيل المراقبة وجعل الرووت هو من يكتب الملف (لتخطي حماية الأندرويد)
        String finalKeyword = keyword;
        new Thread(() -> {
            try {
                Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();
                String cmd = "mkdir -p /sdcard/Exhxx_Dump && logcat";
                if (!finalKeyword.isEmpty()) cmd += " | grep -iE '" + finalKeyword + "'";
                cmd += " > /sdcard/Exhxx_Dump/live_hunter_log.txt";
                
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            } catch (Exception e) {}
        }).start();

        return START_STICKY; // هذا الأمر يمنع الأندرويد من إيقاف الخدمة
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (process != null) process.destroy();
        try { Runtime.getRuntime().exec(new String[]{"su", "-c", "killall logcat"}); } catch (Exception e) {}
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
