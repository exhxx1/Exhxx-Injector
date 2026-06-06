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
        layout.setPadding(40, 50, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("Exhxx Radar\nDev: Mohammed Adnan | Channel: @exhxx78");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 40);
        
        EditText targetInput = new EditText(this);
        targetInput.setHint("اسم التطبيق (مثال: mobi.foo.zain)");
        targetInput.setHintTextColor(Color.GRAY);
        targetInput.setTextColor(Color.WHITE);
        targetInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        targetInput.setPadding(25, 25, 25, 25);
        
        TextView space = new TextView(this);
        space.setHeight(20);

        EditText keywordInput = new EditText(this);
        keywordInput.setHint("الفلترة (اختياري، مثلاً: http|api)");
        keywordInput.setHintTextColor(Color.GRAY);
        keywordInput.setTextColor(Color.WHITE);
        keywordInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        keywordInput.setPadding(25, 25, 25, 25);

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setPadding(0, 30, 0, 0);
        
        Button btnStart = new Button(this);
        btnStart.setText("بدء الصيد");
        btnStart.setBackgroundColor(Color.parseColor("#43A047"));
        btnStart.setTextColor(Color.WHITE);
        
        Button btnStop = new Button(this);
        btnStop.setText("إيقاف");
        btnStop.setBackgroundColor(Color.parseColor("#E53935"));
        btnStop.setTextColor(Color.WHITE);

        btnStart.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, SnifferService.class);
            serviceIntent.putExtra("pkg", targetInput.getText().toString().trim());
            serviceIntent.putExtra("keyword", keywordInput.getText().toString().trim());
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "بدأ التسجيل بالخلفية...", Toast.LENGTH_SHORT).show();
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, SnifferService.class));
            Toast.makeText(this, "تم الإيقاف.", Toast.LENGTH_SHORT).show();
        });

        buttonsLayout.addView(btnStart);
        buttonsLayout.addView(btnStop);

        layout.addView(title);
        layout.addView(targetInput);
        layout.addView(space);
        layout.addView(keywordInput);
        layout.addView(buttonsLayout);
        
        setContentView(layout);
    }
}
