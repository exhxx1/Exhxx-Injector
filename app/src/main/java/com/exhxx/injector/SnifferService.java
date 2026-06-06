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

public class SnifferService extends Service {
    private Process process;
    private boolean isRunning = false;

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
                .setContentTitle("Exhxx Radar (Auto-Sorter)")
                .setContentText("المراقبة الشاملة وفرز الملفات يعمل بالخلفية...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);

        String finalKeyword = keyword;
        isRunning = true;
        
        new Thread(() -> {
            try {
                // تصفير السجل القديم
                Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();
                
                File dir = new File("/sdcard/Exhxx_Dump");
                if (!dir.exists()) dir.mkdirs();

                // خرائط الذاكرة لربط الكود بالتطبيق الخاص به
                HashMap<String, String> pidMap = new HashMap<>();
                HashMap<String, FileWriter> fileMap = new HashMap<>();
                
                // جلب قائمة العمليات المفتوحة مسبقاً للسرعة
                Process pPs = Runtime.getRuntime().exec(new String[]{"su", "-c", "ps -A"});
                BufferedReader brPs = new BufferedReader(new InputStreamReader(pPs.getInputStream()));
                String psLine;
                while((psLine = brPs.readLine()) != null) {
                    String[] parts = psLine.trim().split("\\s+");
                    if(parts.length >= 8) {
                        String pid = parts[1];
                        String name = parts[parts.length - 1];
                        if(name.contains(".") && !name.startsWith("/")) {
                            pidMap.put(pid, name);
                        }
                    }
                }

                // تشغيل المراقبة بصيغة ThreadTime حتى نسحب الـ PID
                String cmd = "logcat -v threadtime";
                if (!finalKeyword.isEmpty()) {
                    cmd += " | grep -iE '" + finalKeyword + "'";
                }
                
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                
                // المحرك الذكي: يقرأ الكود، يسحب الـ PID، يصنع الملف، ويكتب!
                while (isRunning && (line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if(parts.length < 5) continue;
                    
                    String pid = parts[2];
                    String pkg = pidMap.get(pid);
                    
                    // إذا كان التطبيق جديد انفتح هسه، نسأل النظام عن اسمه
                    if (pkg == null) {
                        try {
                            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat /proc/" + pid + "/cmdline"});
                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            String cmdline = br.readLine();
                            if (cmdline != null && cmdline.contains(".")) {
                                pkg = cmdline.trim().replace("\0", "").replaceAll("[^a-zA-Z0-9._-]", "");
                            } else {
                                pkg = "System_Events"; // أحداث النظام العامة
                            }
                            pidMap.put(pid, pkg); // نحفظه حتى ما نسأل عنه مرة ثانية
                        } catch (Exception e) {
                            pkg = "System_Events";
                            pidMap.put(pid, pkg);
                        }
                    }
                    
                    FileWriter fw = fileMap.get(pkg);
                    if (fw == null) {
                        fw = new FileWriter(new File(dir, pkg + "_log.txt"), true);
                        fileMap.put(pkg, fw);
                    }
                    fw.write(line + "\n");
                    fw.flush();
                }
                
                // إغلاق كل الملفات عند الإيقاف
                for (FileWriter fw : fileMap.values()) {
                    try { fw.close(); } catch (Exception e) {}
                }
                
            } catch (Exception e) {}
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (process != null) process.destroy();
        try { Runtime.getRuntime().exec(new String[]{"su", "-c", "killall logcat"}); } catch (Exception e) {}
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
