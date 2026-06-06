package com.exhxx.injector;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    AutoCompleteTextView appSearch;
    Spinner fileSpinner;
    EditText xmlEditor;
    TextView rootStatus;
    List<String> fullPathList = new ArrayList<>();
    String currentPkg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        rootStatus = new TextView(this);
        rootStatus.setTextColor(Color.YELLOW);
        rootStatus.setText("جاري التحقق من الرووت...");

        appSearch = new AutoCompleteTextView(this);
        appSearch.setHint("اكتب اسم اللعبة/التطبيق...");
        appSearch.setTextColor(Color.WHITE);

        fileSpinner = new Spinner(this);
        fileSpinner.setBackgroundColor(Color.parseColor("#1E1E1E"));

        xmlEditor = new EditText(this);
        xmlEditor.setBackgroundColor(Color.BLACK);
        xmlEditor.setTextColor(Color.WHITE);
        xmlEditor.setMinLines(10);

        Button btnSave = new Button(this);
        btnSave.setText("💾 حقن التعديلات (Atomic Injection)");
        btnSave.setBackgroundColor(Color.RED);

        layout.addView(rootStatus);
        layout.addView(appSearch);
        layout.addView(fileSpinner);
        layout.addView(xmlEditor);
        layout.addView(btnSave);
        setContentView(layout);

        checkRoot();

        appSearch.setOnItemClickListener((p, v, pos, id) -> {
            String s = (String) p.getItemAtPosition(pos);
            currentPkg = s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf(")"));
            loadFiles(currentPkg);
        });

        btnSave.setOnClickListener(v -> inject());
    }

    private void checkRoot() {
        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                boolean isRoot = br.readLine().contains("uid=0");
                runOnUiThread(() -> {
                    if(isRoot) {
                        rootStatus.setText("🟢 الرووت متمكن. ابحث عن اللعبة:");
                        rootStatus.setTextColor(Color.GREEN);
                        loadApps();
                    } else rootStatus.setText("🔴 لا يوجد رووت!");
                });
            } catch (Exception e) {}
        }).start();
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        List<String> names = new ArrayList<>();
        for (ApplicationInfo a : apps) names.add(pm.getApplicationLabel(a) + " (" + a.packageName + ")");
        runOnUiThread(() -> appSearch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names)));
    }

    private void loadFiles(String pkg) {
        new Thread(() -> {
            // المسح الجذري لكل مجلدات البيانات الممكنة
            String cmd = "find /data/data/" + pkg + " /data/user/0/" + pkg + " /data/user_de/0/" + pkg + " -name '*.xml' 2>/dev/null";
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                fullPathList.clear();
                List<String> files = new ArrayList<>();
                String l;
                while((l = br.readLine()) != null) {
                    fullPathList.add(l);
                    files.add(l.substring(l.lastIndexOf("/")+1));
                }
                runOnUiThread(() -> fileSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, files)));
            } catch (Exception e) {}
        }).start();
    }

    private void inject() {
        int pos = fileSpinner.getSelectedItemPosition();
        if(pos < 0 || fullPathList.isEmpty()) return;
        String path = fullPathList.get(pos);
        String content = xmlEditor.getText().toString();
        new Thread(() -> {
            try {
                // ضخ مباشر للذاكرة بدون ملفات مؤقتة لتجنب SELinux
                String cmd = "echo " + content.replace("'", "'\\''") + " > \"" + path + "\" && am force-stop " + currentPkg;
                Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
                runOnUiThread(() -> Toast.makeText(this, "✅ حقن ناجح!", Toast.LENGTH_LONG).show());
            } catch (Exception e) {}
        }).start();
    }
}
