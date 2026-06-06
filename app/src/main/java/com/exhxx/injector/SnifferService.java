package com.exhxx.injector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SnifferService extends Service {
    public static boolean isRunning = false;
    private Process logcatProcess;
    private ConcurrentHashMap<String, String> pidMap = new ConcurrentHashMap<>();
    private HashMap<String, FileWriter> fileMap = new HashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String keyword = intent.getStringExtra("keyword");
        if (keyword == null) keyword = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("sniffer", "Exhxx Radar", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
            new Notification.Builder(this, "sniffer") : new Notification.Builder(this);
        
        Notification notification = builder
                .setContentTitle("Exhxx Radar")
                .setContentText("المراقبة الذكية تعمل بالخلفية الآن 🔴")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);
        isRunning = true;
        
        // إخبار الواجهة بتغيير لون الزر
        sendBroadcast(new Intent("EXHXX_UPDATE_UI"));

        String finalKeyword = keyword;
        
        // المحرك الأول: تحديث خريطة التطبيقات بهدوء كل 3 ثواني (حتى لا يختنق المعالج)
        new Thread(() -> {
            while (isRunning) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "ps -A"});
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while((line = br.readLine()) != null) {
                        String[] parts = line.trim().split("\\s+");
                        if(parts.length >= 8) {
                            String pid = parts[1];
                            String name = parts[parts.length - 1];
                            if(name.contains(".") && !name.startsWith("/")) {
                                pidMap.put(pid, name);
                            }
                        }
                    }
                    Thread.sleep(3000);
                } catch (Exception e) {}
            }
        }).start();

        // المحرك الثاني: تسجيل السجلات وفرزها للملفات بسرعة البرق
        new Thread(() -> {
            try {
                // إجبار الرووت على خلق المجلد وإعطاء صلاحيات كاملة
                Runtime.getRuntime().exec(new String[]{"su", "-c", "mkdir -p /sdcard/Exhxx_Dump && chmod -R 777 /sdcard/Exhxx_Dump"}).waitFor();
                Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();
                
                File dir = new File("/sdcard/Exhxx_Dump");

                String cmd = "logcat -v threadtime";
                if (!finalKeyword.isEmpty()) {
                    cmd += " | grep -iE '" + finalKeyword + "'";
                }
                
                logcatProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                BufferedReader reader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                String line;
                
                while (isRunning && (line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if(parts.length < 5) continue;
                    
                    String pid = parts[2]; // الـ PID يكون عادة الكلمة الثالثة بالسجل
                    String pkg = pidMap.get(pid);
                    
                    if (pkg == null) pkg = "System_Events"; // إذا التطبيق مجهول
                    
                    FileWriter fw = fileMap.get(pkg);
                    if (fw == null) {
                        fw = new FileWriter(new File(dir, pkg + "_log.txt"), true);
                        fileMap.put(pkg, fw);
                    }
                    fw.write(line + "\n");
                    fw.flush(); // حفظ فوري!
                }
            } catch (Exception e) {}
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        sendBroadcast(new Intent("EXHXX_UPDATE_UI"));
        
        if (logcatProcess != null) logcatProcess.destroy();
        try { Runtime.getRuntime().exec(new String[]{"su", "-c", "killall logcat"}); } catch (Exception e) {}
        
        // إغلاق الملفات
        for (FileWriter fw : fileMap.values()) {
            try { fw.close(); } catch (Exception e) {}
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
