package com.exhxx.injector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import java.io.*;

public class MainActivity extends Activity {
    EditText targetInput;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("🔥 Exhxx Auto-Injector V1.1 🔥\nDeveloped by: Haider Adel");
        title.setTextColor(Color.CYAN);
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 50);
        
        targetInput = new EditText(this);
        targetInput.setHint("اكتب حزمة التطبيق (مثال: com.whatsapp)");
        targetInput.setHintTextColor(Color.GRAY);
        targetInput.setTextColor(Color.WHITE);
        targetInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        targetInput.setPadding(30, 30, 30, 30);

        Button btnRoot = new Button(this);
        btnRoot.setText("1. فحص وتفعيل الرووت (SU)");
        btnRoot.setBackgroundColor(Color.parseColor("#1E88E5"));
        btnRoot.setTextColor(Color.WHITE);

        Button btnFrida = new Button(this);
        btnFrida.setText("2. تنصيب وتشغيل Frida تلقائياً");
        btnFrida.setBackgroundColor(Color.parseColor("#43A047"));
        btnFrida.setTextColor(Color.WHITE);

        Button btnDump = new Button(this);
        btnDump.setText("3. استخراج التطبيق (APK/Dex Dumper)");
        btnDump.setBackgroundColor(Color.parseColor("#E53935"));
        btnDump.setTextColor(Color.WHITE);

        btnRoot.setOnClickListener(v -> executeRootCommand("echo Root Granted", "تم تفعيل الرووت بنجاح!"));
        
        btnFrida.setOnClickListener(v -> installAndRunFrida());
        
        btnDump.setOnClickListener(v -> {
            String pkg = targetInput.getText().toString().trim();
            if(pkg.isEmpty()) {
                Toast.makeText(this, "اكتب اسم الحزمة أولاً يا هندسة!", Toast.LENGTH_SHORT).show();
                return;
            }
            dumpApp(pkg);
        });

        layout.addView(title);
        layout.addView(targetInput);
        layout.addView(btnRoot);
        layout.addView(btnFrida);
        layout.addView(btnDump);
        setContentView(layout);
    }

    private void installAndRunFrida() {
        Toast.makeText(this, "جاري تنصيب وتشغيل فريدا بالخلفية...", Toast.LENGTH_LONG).show();
        new Thread(() -> {
            try {
                // استخراج السيرفر من داخل التطبيق نفسه
                InputStream in = getAssets().open("frida-server");
                File outFile = new File(getCacheDir(), "frida-server");
                FileOutputStream out = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int read;
                while((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close(); out.close();
                
                // نقل السيرفر لجذور النظام وتشغيله كمسؤول
                String path = outFile.getAbsolutePath();
                executeRootCommand("cp " + path + " /data/local/tmp/frida-server && chmod 755 /data/local/tmp/frida-server && /data/local/tmp/frida-server -D", "✅ فريدا شغالة هسه ومستعدة للهجوم!");
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "صار خطأ بنقل السيرفر!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void dumpApp(String pkg) {
        String outDir = "/sdcard/Exhxx_Dump";
        String cmd = "mkdir -p " + outDir + " && " +
                     "APK_PATH=$(pm path " + pkg + " | cut -d':' -f2) && " +
                     "cp $APK_PATH " + outDir + "/" + pkg + ".apk";
        executeRootCommand(cmd, "🔥 تم السحب! تلكاه بمجلد Exhxx_Dump بالذاكرة الرئيسية");
    }

    private void executeRootCommand(String command, String successMsg) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            runOnUiThread(() -> Toast.makeText(this, successMsg, Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "تأكد من إعطاء صلاحية الرووت للتطبيق!", Toast.LENGTH_LONG).show());
        }
    }
}
