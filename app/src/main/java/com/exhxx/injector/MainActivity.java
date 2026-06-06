package com.exhxx.injector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;

public class MainActivity extends Activity {
    private Button btnToggle;
    private EditText keywordInput;

    // تم إصلاح اسم الدالة هنا إلى onReceive
    private BroadcastReceiver uiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 80, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("Exhxx Radar\nDev: Mohammed Adnan | Channel: @exhxx78");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 60);
        
        keywordInput = new EditText(this);
        keywordInput.setHint("الكلمة المفتاحية (اختياري، مثلاً: http)");
        keywordInput.setHintTextColor(Color.GRAY);
        keywordInput.setTextColor(Color.WHITE);
        keywordInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        keywordInput.setPadding(25, 25, 25, 25);

        btnToggle = new Button(this);
        btnToggle.setPadding(0, 40, 0, 40);
        
        btnToggle.setOnClickListener(v -> {
            if (SnifferService.isRunning) {
                stopService(new Intent(this, SnifferService.class));
            } else {
                Intent serviceIntent = new Intent(this, SnifferService.class);
                serviceIntent.putExtra("keyword", keywordInput.getText().toString().trim());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }
        });

        layout.addView(title);
        layout.addView(keywordInput);
        
        TextView space = new TextView(this);
        space.setHeight(40);
        layout.addView(space);
        
        layout.addView(btnToggle);
        
        setContentView(layout);
        
        registerReceiver(uiReceiver, new IntentFilter("EXHXX_UPDATE_UI"));
        updateUI();
    }

    private void updateUI() {
        if (SnifferService.isRunning) {
            btnToggle.setText("🔴 جاري التسجيل بالخلفية (اضغط للإيقاف)");
            btnToggle.setBackgroundColor(Color.parseColor("#D32F2F")); // أحمر
            btnToggle.setTextColor(Color.WHITE);
        } else {
            btnToggle.setText("🟢 بدء الصيد الشامل");
            btnToggle.setBackgroundColor(Color.parseColor("#388E3C")); // أخضر
            btnToggle.setTextColor(Color.WHITE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(uiReceiver);
    }
}
