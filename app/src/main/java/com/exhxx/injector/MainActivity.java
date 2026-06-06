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
        title.setText("🔥 Exhxx Auto-Injector V1.2 🔥\nDeveloped by: Haider Adel");
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
        btnFrida.setText("2. تنصيب وتشغيل أدوات Frida");
        btnFrida.setBackgroundColor(Color.parseColor("#43A047"));
        btnFrida.setTextColor(Color.WHITE);

        Button btnDump = new Button(this);
        btnDump.setText("3. استخراج الـ APK الخام");
        btnDump.setBackgroundColor(Color.parseColor("#E53935"));
        btnDump.setTextColor(Color.WHITE);

        Button btnMonitor = new Button(this);
        btnMonitor.setText("4. 🔴 مراقبة العمليات (Live Monitor)");
        btnMonitor.setBackgroundColor(Color.parseColor("#8E24AA"));
        btnMonitor.setTextColor(Color.WHITE);

        btnRoot.setOnClickListener(v -> executeRootCommand("echo Root Granted", "تم تفعيل الرووت بنجاح!"));
        
        btnFrida.setOnClickListener(v -> installFridaTools());
        
        btnDump.setOnClickListener(v -> {
            String pkg = targetInput.getText().toString().trim();
            if(!pkg.isEmpty()) dumpApp(pkg);
            else Toast.makeText(this, "اكتب اسم الحزمة أولاً!", Toast.LENGTH_SHORT).show();
        });

        btnMonitor.setOnClickListener(v -> {
            String pkg = targetInput.getText().toString().trim();
            if(!pkg.isEmpty()) monitorApp(pkg);
            else Toast.makeText(this, "اكتب اسم الحزمة أولاً!", Toast.LENGTH_SHORT).show();
        });

        layout.addView(title);
        layout.addView(targetInput);
        layout.addView(btnRoot);
        layout.addView(btnFrida);
        layout.addView(btnDump);
        layout.addView(btnMonitor);
        setContentView(layout);
    }

    private void installFridaTools() {
        Toast.makeText(this, "جاري تجهيز أدوات الحقن بالخلفية...", Toast.LENGTH_LONG).show();
        new Thread(() -> {
            try {
                extractAsset("frida-server");
                extractAsset("frida-inject");
                executeRootCommand("/data/local/tmp/frida-server -D", "✅ فريدا شغالة وجاهزة للحقن!");
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "خطأ بنقل الأدوات!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void extractAsset(String name) throws Exception {
        InputStream in = getAssets().open(name);
        File outFile = new File(getCacheDir(), name);
        FileOutputStream out = new FileOutputStream(outFile);
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        in.close(); out.close();
        executeRootCommand("cp " + outFile.getAbsolutePath() + " /data/local/tmp/" + name + " && chmod 755 /data/local/tmp/" + name, "");
    }

    private void dumpApp(String pkg) {
        String outDir = "/sdcard/Exhxx_Dump";
        String cmd = "mkdir -p " + outDir + " && " +
                     "APK_PATH=$(pm path " + pkg + " | grep 'base.apk' | head -n 1 | cut -d':' -f2) && " +
                     "if [ -z \"$APK_PATH\" ]; then APK_PATH=$(pm path " + pkg + " | head -n 1 | cut -d':' -f2); fi && " +
                     "cp \"$APK_PATH\" \"" + outDir + "/" + pkg + ".apk\" && chmod 777 \"" + outDir + "/" + pkg + ".apk\"";
        executeRootCommand(cmd, "🔥 تم سحب التطبيق للمجلد!");
    }

    private void monitorApp(String pkg) {
        try {
            // صناعة سكربت المراقبة (Hook.js) بالذاكرة
            File dumpDir = new File("/sdcard/Exhxx_Dump");
            if (!dumpDir.exists()) dumpDir.mkdirs();
            File hookFile = new File(dumpDir, "hook.js");
            FileWriter fw = new FileWriter(hookFile);
            fw.write("Java.perform(function() {\n");
            fw.write("  var File = Java.use('java.io.File');\n");
            fw.write("  File.$init.overload('java.lang.String').implementation = function(path) {\n");
            fw.write("    console.log('[MONITOR] التطبيق يقرأ الملف: ' + path);\n");
            fw.write("    return this.$init(path);\n");
            fw.write("  };\n");
            fw.write("});\n");
            fw.close();

            // تنفيذ سكربت المراقبة باستخدام frida-inject
            String logPath = "/sdcard/Exhxx_Dump/monitor_log.txt";
            String cmd = "nohup /data/local/tmp/frida-inject -f " + pkg + " -s " + hookFile.getAbsolutePath() + " > " + logPath + " 2>&1 &";
            executeRootCommand(cmd, "🔴 بدأت المراقبة! راجع ملف monitor_log.txt بالمجلد.");
        } catch (Exception e) {
            Toast.makeText(this, "خطأ بصناعة السكربت!", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeRootCommand(String command, String successMsg) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            if(!successMsg.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, successMsg, Toast.LENGTH_LONG).show());
            }
        } catch (Exception e) {}
    }
}
