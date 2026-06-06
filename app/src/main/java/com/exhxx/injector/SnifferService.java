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
        String pkg = intent.getStringExtra("pkg");
        String keyword = intent.getStringExtra("keyword");
        
        if (pkg == null) pkg = "";
        if (keyword == null) keyword = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("sniffer", "Exhxx Radar", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION.CODES.O ? 
            new Notification.Builder(this, "sniffer") : new Notification.Builder(this);
        
        Notification notification = builder
                .setContentTitle("Exhxx Radar")
                .setContentText("المراقبة تعمل بالخلفية الآن...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);

        String finalPkg = pkg;
        String finalKeyword = keyword;
        
        new Thread(() -> {
            try {
                // تصفير السجل القديم
                Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();
                
                // تحديد اسم الملف بناءً على اسم الحزمة
                String fileName = finalPkg.isEmpty() ? "global_log.txt" : finalPkg + "_log.txt";
                String outPath = "/sdcard/Exhxx_Dump/" + fileName;
                
                // بناء الأمر الذكي بالرووت
                StringBuilder cmd = new StringBuilder();
                cmd.append("mkdir -p /sdcard/Exhxx_Dump && ");
                
                if (!finalPkg.isEmpty()) {
                    cmd.append("PID=$(pidof ").append(finalPkg).append(") && ");
                    cmd.append("if [ -z \"$PID\" ]; then echo 'App not running! Please open the app first.' > ").append(outPath).append("; exit; fi && ");
                    cmd.append("logcat --pid=$PID");
                } else {
                    cmd.append("logcat");
                }

                if (!finalKeyword.isEmpty()) {
                    cmd.append(" | grep -iE '").append(finalKeyword).append("'");
                }
                
                cmd.append(" > ").append(outPath);
                
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd.toString()});
            } catch (Exception e) {}
        }).start();

        return START_STICKY;
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
