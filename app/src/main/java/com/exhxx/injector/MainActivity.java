package com.exhxx.injector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import android.view.View;
import java.io.*;

public class MainActivity extends Activity {
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
        title.setText("🔥 Exhxx Radar V1.4 (Real-Time) 🔥\nDeveloped by: Haider Adel");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 30);
        
        keywordInput = new EditText(this);
        keywordInput.setHint("كلمات الفلترة (مثلاً: http|api|zain)");
        keywordInput.setHintTextColor(Color.GRAY);
        keywordInput.setTextColor(Color.WHITE);
        keywordInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        keywordInput.setPadding(25, 25, 25, 25);

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        Button btnStart = new Button(this);
        btnStart.setText("🟢 بدء المراقبة");
        btnStart.setBackgroundColor(Color.parseColor("#43A047"));
        btnStart.setTextColor(Color.WHITE);
        
        Button btnStop = new Button(this);
        btnStop.setText("🔴 إيقاف");
        btnStop.setBackgroundColor(Color.parseColor("#E53935"));
        btnStop.setTextColor(Color.WHITE);

        scrollView = new ScrollView(this);
        terminal = new TextView(this);
        terminal.setTextColor(Color.GREEN);
        terminal.setBackgroundColor(Color.BLACK);
        terminal.setPadding(20, 20, 20, 20);
        terminal.setTextSize(12);
        terminal.setText(">> جاهز للمراقبة الشاملة (مثل التيرموكس)...\n");
        scrollView.addView(terminal);

        btnStart.setOnClickListener(v -> {
            String keyword = keywordInput.getText().toString().trim();
            startSniffing(keyword);
        });

        btnStop.setOnClickListener(v -> stopSniffing());

        buttonsLayout.addView(btnStart);
        buttonsLayout.addView(btnStop);

        layout.addView(title);
        layout.addView(keywordInput);
        layout.addView(buttonsLayout);
        layout.addView(scrollView);
        
        setContentView(layout);
    }

    private void startSniffing(String keyword) {
        if(isLogging) return;
        isLogging = true;
        terminal.setText(">> ⏳ جاري تهيئة الملف والمراقبة...\n");

        new Thread(() -> {
            try {
                // تصفير السجلات القديمة
                Runtime.getRuntime().exec(new String[]{"su", "-c", "logcat -c"}).waitFor();

                // 1. إنشاء الملف فوراً قبل ما نبدأ
                File outDir = new File("/sdcard/Exhxx_Dump");
                if (!outDir.exists()) outDir.mkdirs();
                File logFile = new File(outDir, "live_hunter_log.txt");
                FileWriter fw = new FileWriter(logFile, false); // false يعني يمسح القديم ويبدأ جديد

                runOnUiThread(() -> terminal.append(">> ✅ تم إنشاء الملف: " + logFile.getName() + "\n>> 🔴 المراقبة الشاملة بدأت...\n--------------------------\n"));

                // 2. أمر المراقبة (بالضبط مثل التيرموكس)
                String cmd = "logcat";
                if (!keyword.isEmpty()) {
                    cmd = "logcat | grep -iE '" + keyword + "'";
                }

                logcatProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                BufferedReader reader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                String line;
                
                // 3. السر هنا: نكتب السطر بالملف ونحفظه (flush) قبل لا نعرضه بالشاشة
                while (isLogging && (line = reader.readLine()) != null) {
                    final String logLine = line;
                    
                    fw.write(logLine + "\n");
                    fw.flush(); // هذا الإيعاز يجبر النظام يحفظ الداتا بالذاكرة فوراً!

                    runOnUiThread(() -> {
                        terminal.append(logLine + "\n");
                        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    });
                }
                fw.close();
            } catch (Exception e) {
                // إذا طلعت بالخلفية والنظام قطع الاتصال بالشاشة، ماكو مشكلة لأن الداتا انحفظت بفضل الـ flush
                runOnUiThread(() -> terminal.append("\n>> ⚠️ توقف العرض (بسبب الخروج للخلفية).\n>> لا تقلق! كل شيء تم حفظه بملف live_hunter_log.txt\n"));
                isLogging = false;
            }
        }).start();
    }

    private void stopSniffing() {
        if(!isLogging) return;
        isLogging = false;
        try {
            if(logcatProcess != null) logcatProcess.destroy();
            terminal.append("\n--------------------------\n>> 🛑 تم إيقاف المراقبة يدوياً.\n");
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        } catch (Exception e) {}
    }
}
