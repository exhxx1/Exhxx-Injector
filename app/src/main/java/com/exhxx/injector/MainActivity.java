package com.exhxx.injector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import java.io.*;

public class MainActivity extends Activity {
    EditText pkgInput, fileInput, xmlEditor;
    TextView fileListText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("Exhxx Prefs Injector 💉\nDev: Mohammed Adnan | @exhxx78");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 40);
        
        pkgInput = new EditText(this);
        pkgInput.setHint("اسم التطبيق (مثال: com.kiloo.subwaysurf)");
        pkgInput.setHintTextColor(Color.GRAY);
        pkgInput.setTextColor(Color.WHITE);
        pkgInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        pkgInput.setPadding(25, 25, 25, 25);

        Button btnList = new Button(this);
        btnList.setText("🔍 1. جلب ملفات الحفظ (XML)");
        btnList.setBackgroundColor(Color.parseColor("#1E88E5"));
        btnList.setTextColor(Color.WHITE);

        fileListText = new TextView(this);
        fileListText.setTextColor(Color.GREEN);
        fileListText.setPadding(10, 20, 10, 20);
        fileListText.setText("الملفات المتوفرة ستظهر هنا...");

        fileInput = new EditText(this);
        fileInput.setHint("اسم الملف (مثال: com.kiloo.subwaysurf_preferences.xml)");
        fileInput.setHintTextColor(Color.GRAY);
        fileInput.setTextColor(Color.WHITE);
        fileInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        fileInput.setPadding(25, 25, 25, 25);

        Button btnRead = new Button(this);
        btnRead.setText("📖 2. قراءة الملف");
        btnRead.setBackgroundColor(Color.parseColor("#FBC02D"));
        btnRead.setTextColor(Color.BLACK);

        xmlEditor = new EditText(this);
        xmlEditor.setHint("محتوى الـ XML سيظهر هنا للتعديل...");
        xmlEditor.setHintTextColor(Color.GRAY);
        xmlEditor.setTextColor(Color.WHITE);
        xmlEditor.setBackgroundColor(Color.parseColor("#000000"));
        xmlEditor.setPadding(25, 25, 25, 25);
        xmlEditor.setMinLines(15);
        xmlEditor.setGravity(android.view.Gravity.TOP | android.view.Gravity.LEFT);

        Button btnSave = new Button(this);
        btnSave.setText("💾 3. حفظ وحقن التعديلات");
        btnSave.setBackgroundColor(Color.parseColor("#43A047"));
        btnSave.setTextColor(Color.WHITE);

        btnList.setOnClickListener(v -> listFiles());
        btnRead.setOnClickListener(v -> readFile());
        btnSave.setOnClickListener(v -> saveFile());

        layout.addView(title);
        layout.addView(pkgInput);
        layout.addView(btnList);
        layout.addView(fileListText);
        layout.addView(fileInput);
        layout.addView(btnRead);
        layout.addView(xmlEditor);
        layout.addView(btnSave);
        
        scroll.addView(layout);
        setContentView(scroll);
    }

    private void listFiles() {
        String pkg = pkgInput.getText().toString().trim();
        if (pkg.isEmpty()) {
            Toast.makeText(this, "اكتب اسم التطبيق أولاً!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // استخدام الرووت لجلب أسماء الملفات من المجلد السري
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "ls /data/data/" + pkg + "/shared_prefs/"});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            
            if (sb.length() == 0) fileListText.setText("❌ لم يتم العثور على ملفات أو التطبيق غير موجود!");
            else fileListText.setText("الملفات المتوفرة:\n" + sb.toString());
        } catch (Exception e) { fileListText.setText("خطأ: " + e.getMessage()); }
    }

    private void readFile() {
        String pkg = pkgInput.getText().toString().trim();
        String file = fileInput.getText().toString().trim();
        if (pkg.isEmpty() || file.isEmpty()) {
            Toast.makeText(this, "اكتب اسم التطبيق واسم الملف!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // قراءة محتوى الملف وعرضه بالمحرر
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat /data/data/" + pkg + "/shared_prefs/" + file});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            
            xmlEditor.setText(sb.toString());
            Toast.makeText(this, "تمت قراءة الملف!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {}
    }

    private void saveFile() {
        String pkg = pkgInput.getText().toString().trim();
        String file = fileInput.getText().toString().trim();
        String content = xmlEditor.getText().toString();
        if (pkg.isEmpty() || file.isEmpty() || content.isEmpty()) return;
        
        try {
            // 1. حفظ المحتوى بملف مؤقت داخل ذاكرة التطبيق لتجنب مشاكل الأقواس بالاكواد
            File tempFile = new File(getCacheDir(), "temp.xml");
            FileWriter fw = new FileWriter(tempFile);
            fw.write(content);
            fw.close();

            // 2. استخدام الرووت لنقل المحتوى واستبدال الملف الأصلي (هذه الطريقة تحافظ على الأذونات الأصلية للملف)
            String cmd = "cat " + tempFile.getAbsolutePath() + " > /data/data/" + pkg + "/shared_prefs/" + file;
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            p.waitFor();
            
            // 3. إجبار التطبيق المستهدف على الإغلاق حتى يقهر نفسه ويقرأ البيانات الجديدة عند فتحه
            Runtime.getRuntime().exec(new String[]{"su", "-c", "am force-stop " + pkg});
            
            Toast.makeText(this, "✅ تم حقن التعديلات بنجاح!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ حدث خطأ أثناء الحفظ!", Toast.LENGTH_SHORT).show();
        }
    }
}
