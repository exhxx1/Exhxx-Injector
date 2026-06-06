package com.exhxx.injector;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("🔥 Exhxx Radar V1.5 (Termux Mode) 🔥\nDeveloped by: Haider Adel");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 30);
        
        EditText keywordInput = new EditText(this);
        keywordInput.setHint("كلمات الفلترة (مثلاً: http|api|zain)");
        keywordInput.setHintTextColor(Color.GRAY);
        keywordInput.setTextColor(Color.WHITE);
        keywordInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        keywordInput.setPadding(25, 25, 25, 25);

        Button btnStart = new Button(this);
        btnStart.setText("🟢 بدء المراقبة بالخلفية");
        btnStart.setBackgroundColor(Color.parseColor("#43A047"));
        btnStart.setTextColor(Color.WHITE);
        
        Button btnStop = new Button(this);
        btnStop.setText("🔴 إيقاف الإشعار والمراقبة");
        btnStop.setBackgroundColor(Color.parseColor("#E53935"));
        btnStop.setTextColor(Color.WHITE);

        TextView terminal = new TextView(this);
        terminal.setTextColor(Color.GREEN);
        terminal.setPadding(20, 40, 20, 20);
        terminal.setTextSize(14);
        terminal.setText(">> وضع التيرموكس جاهز!\n\n1. اضغط بدء.\n2. سيظهر إشعار في شريط الإشعارات أعلى الشاشة.\n3. يمكنك إغلاق هذا التطبيق بالكامل، وسيستمر التسجيل بالخلفية.\n4. تجد الصيدة في مسار:\n/sdcard/Exhxx_Dump/live_hunter_log.txt");

        btnStart.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, SnifferService.class);
            serviceIntent.putExtra("keyword", keywordInput.getText().toString().trim());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "✅ المراقبة بدأت! راقب شريط الإشعارات.", Toast.LENGTH_LONG).show();
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, SnifferService.class));
            Toast.makeText(this, "🛑 تم الإيقاف.", Toast.LENGTH_SHORT).show();
        });

        layout.addView(title);
        layout.addView(keywordInput);
        layout.addView(btnStart);
        layout.addView(btnStop);
        layout.addView(terminal);
        
        setContentView(layout);
    }
}
