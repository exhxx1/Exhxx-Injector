package com.exhxx.injector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import android.view.View;
import java.io.*;

public class MainActivity extends Activity {
    EditText targetInput;
    EditText keywordInput;
    TextView terminal;
    ScrollView scrollView;
    Process logcatProcess;
    boolean isLogging = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("🔥 Exhxx Radar V1.3 (Live Sniffer) 🔥\nDeveloped by: Haider Adel");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 30);
        
        targetInput = new EditText(this);
        targetInput.setHint("اسم الحزمة (مثال: iq.zain.main)");
        targetInput.setHintTextColor(Color.GRAY);
        targetInput.setTextColor(Color.WHITE);
        targetInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        targetInput.setPadding(25, 25, 25, 25);

        keywordInput = new EditText(this);
        keywordInput.setHint("كلمة الفلترة (اختياري، مثلاً: http أو api)");
        keywordInput.setHintTextColor(Color.GRAY);
        keywordInput.setTextColor(Color.WHITE);
        keywordInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        keywordInput.setPadding(25, 25, 25, 25);

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        Button btnStart = new Button(this);
        btnStart.setText("🟢 بدء الصيد");
        btnStart.setBackgroundColor(Color.parseColor("#43A047"));
        btnStart.setTextColor(Color.WHITE);
        
        Button btnStop = new Button(this);
        btnStop.setText("🔴 إيقاف وحفظ");
        btnStop.setBackgroundColor(Color.parseColor("#E53935"));
        btnStop.setTextColor(Color.WHITE);

        // إعدادات شاشة الرادار (Terminal)
        scrollView = new ScrollView(this);
        terminal = new TextView(this);
        terminal.setTextColor(Color.GREEN);
        terminal.setBackgroundColor(Color.BLACK);
        terminal.setPadding(20, 20, 20, 20);
        terminal.setTextSize(12);
        terminal.setText(">> مستعد للصيد...\n");
        scrollView.addView(terminal);

        btnStart.setOnClickListener(v -> {
            String pkg = targetInput.getText().toString().trim();
            String keyword = keywordInput.getText().toString().trim();
            if(pkg.isEmpty()) {
                Toast.makeText(this, "اكتب اسم الحزمة أولاً!", Toast.LENGTH_SHORT).show();
                return;
            }
            startSniffing(pkg, keyword);
        });

        btnStop.setOnClickListener(v -> stopSniffing());

        buttonsLayout.addView(btnStart);
        buttonsLayout.addView(btnStop);

        layout.addView(title);
        layout.addView(targetInput);
        layout.addView(keywordInput);
        layout.addView(buttonsLayout);
        layout.addView(scrollView);
        
        setContentView(layout);
    }

    private void startSniffing(String pkg, String keyword) {
        if(isLogging) return;
        isLogging = true;
        terminal.setText(">> ⏳ جاري البحث عن التطبيق بالذاكرة...\n");

        new Thread(() -> {
            try {
                // تصفير السجلات القديمة
                Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();

                // سحب رقم الـ PID مال التطبيق المستهدف
                Process pidProc = Runtime.getRuntime().exec(new String[]{"su", "-c", "pidof " + pkg});
                BufferedReader pidReader = new BufferedReader(new InputStreamReader(pidProc.getInputStream()));
                String pid = pidReader.readLine();

                if (pid == null || pid.trim().isEmpty()) {
                    runOnUiThread(() -> {
                        terminal.append(">> ❌ التطبيق مو شغال! افتح التطبيق المستهدف أولاً، وبعدين اضغط بدء الصيد.\n");
                        isLogging = false;
                    });
                    return;
                }

                runOnUiThread(() -> terminal.append(">> ✅ تم اصطياد التطبيق! (PID: " + pid.trim() + ")\n>> 🔴 جاري تسجيل العمليات الحية...\n--------------------------\n"));

                // تشغيل المراقبة على التطبيق بالكامل
                String cmd = "logcat --pid=" + pid.trim();
                if (!keyword.isEmpty()) {
                    cmd = "logcat --pid=" + pid.trim() + " | grep -iE '" + keyword + "'";
                }

                logcatProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                BufferedReader reader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                String line;
                
                while (isLogging && (line = reader.readLine()) != null) {
                    final String logLine = line;
                    runOnUiThread(() -> {
                        terminal.append(logLine + "\n");
                        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> terminal.append(">> ❌ خطأ: " + e.getMessage() + "\n"));
                isLogging = false;
            }
        }).start();
    }

    private void stopSniffing() {
        if(!isLogging) return;
        isLogging = false;
        try {
            if(logcatProcess != null) logcatProcess.destroy();
            
            // حفظ السجلات بالذاكرة
            File outDir = new File("/sdcard/Exhxx_Dump");
            if (!outDir.exists()) outDir.mkdirs();
            File logFile = new File(outDir, "hunter_log.txt");
            FileWriter fw = new FileWriter(logFile);
            fw.write(terminal.getText().toString());
            fw.close();
            
            terminal.append("\n--------------------------\n>> 🛑 تم الإيقاف! السجل انحفظ بمسار Exhxx_Dump/hunter_log.txt\n");
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        } catch (Exception e) {}
    }
}
