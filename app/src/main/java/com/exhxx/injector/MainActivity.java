package com.exhxx.injector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import java.io.DataOutputStream;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("🔥 Exhxx Auto-Injector 🔥\nDeveloped by: Haider Adel");
        title.setTextColor(Color.CYAN);
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 50);

        Button btnRoot = new Button(this);
        btnRoot.setText("1. فحص وتفعيل الرووت (SU)");
        btnRoot.setBackgroundColor(Color.parseColor("#1E88E5"));
        btnRoot.setTextColor(Color.WHITE);

        Button btnFrida = new Button(this);
        btnFrida.setText("2. تشغيل سيرفر Frida بالخلفية");
        btnFrida.setBackgroundColor(Color.parseColor("#43A047"));
        btnFrida.setTextColor(Color.WHITE);

        Button btnDump = new Button(this);
        btnDump.setText("3. استخراج Dex (Live Dumper)");
        btnDump.setBackgroundColor(Color.parseColor("#E53935"));
        btnDump.setTextColor(Color.WHITE);

        btnRoot.setOnClickListener(v -> executeRootCommand("su -c 'echo Root Granted'", "تم تفعيل الرووت بنجاح!"));
        btnFrida.setOnClickListener(v -> executeRootCommand("su -c '/data/local/tmp/frida-server &'", "تم تشغيل Frida Server!"));
        btnDump.setOnClickListener(v -> Toast.makeText(this, "جاري سحب الكود من الذاكرة...", Toast.LENGTH_SHORT).show());

        layout.addView(title);
        layout.addView(btnRoot);
        layout.addView(btnFrida);
        layout.addView(btnDump);
        setContentView(layout);
    }

    private void executeRootCommand(String command, String successMsg) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: تأكد من وجود صلاحية الرووت!", Toast.LENGTH_LONG).show();
        }
    }
}
