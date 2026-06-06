package com.exhxx.injector;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import android.view.View;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    Spinner appSpinner, fileSpinner;
    EditText xmlEditor;
    Button btnRead, btnSave;
    
    List<String> pkgList = new ArrayList<>();
    List<String> fullPathList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("Exhxx Prefs Injector V2.0 💉\nDev: Mohammed Adnan | @exhxx78");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 40);
        
        TextView lbl1 = new TextView(this);
        lbl1.setText("1. اختر التطبيق المراد تعديله:");
        lbl1.setTextColor(Color.WHITE);
        
        appSpinner = new Spinner(this);
        appSpinner.setBackgroundColor(Color.parseColor("#1E1E1E"));
        appSpinner.setPadding(0, 20, 0, 40);

        TextView lbl2 = new TextView(this);
        lbl2.setText("2. اختر ملف البيانات (XML):");
        lbl2.setTextColor(Color.WHITE);
        lbl2.setPadding(0, 20, 0, 0);

        fileSpinner = new Spinner(this);
        fileSpinner.setBackgroundColor(Color.parseColor("#1E1E1E"));
        fileSpinner.setPadding(0, 20, 0, 40);

        btnRead = new Button(this);
        btnRead.setText("📖 قراءة الملف المحدد");
        btnRead.setBackgroundColor(Color.parseColor("#FBC02D"));
        btnRead.setTextColor(Color.BLACK);

        xmlEditor = new EditText(this);
        xmlEditor.setHint("محتوى الملف سيظهر هنا...");
        xmlEditor.setHintTextColor(Color.GRAY);
        xmlEditor.setTextColor(Color.WHITE);
        xmlEditor.setBackgroundColor(Color.parseColor("#000000"));
        xmlEditor.setPadding(25, 25, 25, 25);
        xmlEditor.setMinLines(15);
        xmlEditor.setGravity(android.view.Gravity.TOP | android.view.Gravity.LEFT);

        btnSave = new Button(this);
        btnSave.setText("💾 حفظ وحقن التعديلات");
        btnSave.setBackgroundColor(Color.parseColor("#43A047"));
        btnSave.setTextColor(Color.WHITE);

        layout.addView(title);
        layout.addView(lbl1);
        layout.addView(appSpinner);
        layout.addView(lbl2);
        layout.addView(fileSpinner);
        layout.addView(btnRead);
        layout.addView(xmlEditor);
        layout.addView(btnSave);
        
        scroll.addView(layout);
        setContentView(scroll);

        loadInstalledApps();

        appSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    loadAppFiles(pkgList.get(position));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnRead.setOnClickListener(v -> readFile());
        btnSave.setOnClickListener(v -> saveFile());
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pm));
        
        List<String> displayNames = new ArrayList<>();
        displayNames.add("-- اختر تطبيقاً --");
        pkgList.add("");

        for (ApplicationInfo app : apps) {
            // جلب تطبيقات المستخدم (يمكن إزالة الشرط لجلب تطبيقات النظام أيضاً)
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                displayNames.add(app.loadLabel(pm).toString() + " (" + app.packageName + ")");
                pkgList.add(app.packageName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayNames);
        appSpinner.setAdapter(adapter);
    }

    private void loadAppFiles(String pkg) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "find /data/data/" + pkg + " -type f -name '*.xml'"});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            
            List<String> displayFiles = new ArrayList<>();
            fullPathList.clear();
            
            String line;
            while ((line = br.readLine()) != null) {
                fullPathList.add(line);
                displayFiles.add(line.replace("/data/data/" + pkg + "/", "")); // إخفاء المسار الطويل للجمالية
            }
            
            if (displayFiles.isEmpty()) {
                displayFiles.add("-- لا توجد ملفات XML --");
                fullPathList.add("");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayFiles);
            fileSpinner.setAdapter(adapter);
        } catch (Exception e) {}
    }

    private void readFile() {
        int pos = fileSpinner.getSelectedItemPosition();
        if (pos < 0 || fullPathList.isEmpty() || fullPathList.get(pos).isEmpty()) {
            Toast.makeText(this, "لم يتم تحديد ملف صالح!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String fullPath = fullPathList.get(pos);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat " + fullPath});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            
            xmlEditor.setText(sb.toString());
            Toast.makeText(this, "تمت القراءة بنجاح!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {}
    }

    private void saveFile() {
        int appPos = appSpinner.getSelectedItemPosition();
        int filePos = fileSpinner.getSelectedItemPosition();
        String content = xmlEditor.getText().toString();
        
        if (appPos <= 0 || filePos < 0 || fullPathList.isEmpty() || fullPathList.get(filePos).isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "بيانات غير مكتملة!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String pkg = pkgList.get(appPos);
        String fullPath = fullPathList.get(filePos);
        
        try {
            File tempFile = new File(getCacheDir(), "temp.xml");
            FileWriter fw = new FileWriter(tempFile);
            fw.write(content);
            fw.close();

            String cmd = "cat " + tempFile.getAbsolutePath() + " > " + fullPath;
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            p.waitFor();
            
            Runtime.getRuntime().exec(new String[]{"su", "-c", "am force-stop " + pkg});
            
            Toast.makeText(this, "✅ تم حقن البيانات وإعادة تشغيل التطبيق!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ فشل الحفظ!", Toast.LENGTH_SHORT).show();
        }
    }
}
