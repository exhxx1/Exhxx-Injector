package com.exhxx.injector;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    AutoCompleteTextView appSearch;
    Spinner fileSpinner;
    EditText xmlEditor;
    Button btnRead, btnSave;
    TextView rootStatus;
    
    List<String> fullPathList = new ArrayList<>();
    boolean hasRoot = false;
    String currentPkg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("Exhxx Prefs Injector V3.0 💉\n(Atomic Edition) - Dev: Mohammed Adnan");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 20);

        rootStatus = new TextView(this);
        rootStatus.setText("⏳ جاري تهيئة النظام والمحرك...");
        rootStatus.setTextColor(Color.YELLOW);
        rootStatus.setPadding(0, 0, 0, 40);
        
        TextView lbl1 = new TextView(this);
        lbl1.setText("1. ابحث عن التطبيق (اكتب اسمه):");
        lbl1.setTextColor(Color.WHITE);
        
        // مربع بحث ذكي بدل القائمة الطويلة
        appSearch = new AutoCompleteTextView(this);
        appSearch.setHint("🔍 اكتب حرفين من اسم التطبيق...");
        appSearch.setHintTextColor(Color.GRAY);
        appSearch.setTextColor(Color.WHITE);
        appSearch.setBackgroundColor(Color.parseColor("#1E1E1E"));
        appSearch.setPadding(25, 25, 25, 25);
        appSearch.setThreshold(1); // يبدأ البحث من أول حرف

        TextView lbl2 = new TextView(this);
        lbl2.setText("2. اختر ملف البيانات (XML):");
        lbl2.setTextColor(Color.WHITE);
        lbl2.setPadding(0, 40, 0, 0);

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
        btnSave.setText("💾 حقن التعديلات (تخطي الحماية)");
        btnSave.setBackgroundColor(Color.parseColor("#43A047"));
        btnSave.setTextColor(Color.WHITE);

        layout.addView(title);
        layout.addView(rootStatus);
        layout.addView(lbl1);
        layout.addView(appSearch);
        layout.addView(lbl2);
        layout.addView(fileSpinner);
        layout.addView(btnRead);
        layout.addView(xmlEditor);
        layout.addView(btnSave);
        
        scroll.addView(layout);
        setContentView(scroll);

        fileSpinner.setAdapter(createSpinnerAdapter(Arrays.asList("...")));

        checkRootAndLoadApps();

        // عند اختيار تطبيق من البحث
        appSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            int start = selected.lastIndexOf("(");
            int end = selected.lastIndexOf(")");
            if (start != -1 && end != -1) {
                currentPkg = selected.substring(start + 1, end);
                loadAppFiles(currentPkg);
            }
        });

        btnRead.setOnClickListener(v -> readFile());
        btnSave.setOnClickListener(v -> saveFile());
    }

    private ArrayAdapter<String> createSpinnerAdapter(List<String> list) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                return tv;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#1E1E1E"));
                tv.setPadding(30, 30, 30, 30);
                return tv;
            }
        };
    }

    private void checkRootAndLoadApps() {
        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo root_ok"});
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String res = br.readLine();
                hasRoot = "root_ok".equals(res);
            } catch (Exception e) { hasRoot = false; }
            
            if (hasRoot) {
                PackageManager pm = getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(0); // جلب كل التطبيقات بدون استثناء
                List<String> displayNames = new ArrayList<>();

                for (ApplicationInfo app : apps) {
                    displayNames.add(app.loadLabel(pm).toString() + " (" + app.packageName + ")");
                }
                
                runOnUiThread(() -> {
                    rootStatus.setText("🟢 الرووت متوفر. المحرك جاهز!");
                    rootStatus.setTextColor(Color.GREEN);
                    
                    // محول خاص لمربع البحث حتى يكون النص أسود وواضح
                    ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, displayNames) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView tv = (TextView) super.getView(position, convertView, parent);
                            tv.setTextColor(Color.BLACK);
                            tv.setPadding(30, 30, 30, 30);
                            return tv;
                        }
                    };
                    appSearch.setAdapter(searchAdapter);
                });
            } else {
                runOnUiThread(() -> {
                    rootStatus.setText("🔴 الرووت غير متوفر أو مرفوض من Magisk!");
                    rootStatus.setTextColor(Color.RED);
                });
            }
        }).start();
    }

    private void loadAppFiles(String pkg) {
        fileSpinner.setAdapter(createSpinnerAdapter(Arrays.asList("⏳ جاري المسح المزدوج...")));
        
        new Thread(() -> {
            try {
                // السر الأكبر: مسح مسار البيانات العادي + مسار البيانات المشفرة (Device Encrypted)
                String findCmd = "find /data/data/" + pkg + " /data/user_de/0/" + pkg + " -type f -name '*.xml' 2>/dev/null";
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", findCmd});
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
                
                List<String> displayFiles = new ArrayList<>();
                List<String> tempFullPathList = new ArrayList<>();
                
                String line;
                while ((line = br.readLine()) != null) {
                    tempFullPathList.add(line);
                    // تنظيف المسار للجمالية
                    String cleanName = line.replace("/data/data/" + pkg + "/", "").replace("/data/user_de/0/" + pkg + "/", "");
                    displayFiles.add(cleanName);
                }
                
                runOnUiThread(() -> {
                    fullPathList.clear();
                    fullPathList.addAll(tempFullPathList);
                    if (displayFiles.isEmpty()) {
                        displayFiles.add("-- لا توجد ملفات XML --");
                        fullPathList.add("");
                    }
                    fileSpinner.setAdapter(createSpinnerAdapter(displayFiles));
                });
            } catch (Exception e) {}
        }).start();
    }

    private void readFile() {
        int pos = fileSpinner.getSelectedItemPosition();
        if (pos < 0 || fullPathList.isEmpty() || fullPathList.get(pos).isEmpty()) return;
        
        String fullPath = fullPathList.get(pos);
        xmlEditor.setText("⏳ جاري سحب البيانات...");
        
        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat \"" + fullPath + "\""});
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
                
                runOnUiThread(() -> xmlEditor.setText(sb.toString()));
            } catch (Exception e) {}
        }).start();
    }

    private void saveFile() {
        int filePos = fileSpinner.getSelectedItemPosition();
        String content = xmlEditor.getText().toString();
        
        if (currentPkg.isEmpty() || filePos < 0 || fullPathList.isEmpty() || fullPathList.get(filePos).isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "بيانات غير مكتملة!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String fullPath = fullPathList.get(filePos);
        
        new Thread(() -> {
            try {
                // 1. الضخ المباشر عبر أنبوب الرووت (يتخطى كل حمايات التخزين)
                Process writeProc = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat > /data/local/tmp/exhxx_temp.xml"});
                OutputStream os = writeProc.getOutputStream();
                os.write(content.getBytes("UTF-8"));
                os.flush();
                os.close();
                writeProc.waitFor();

                // 2. أمر الحقن والتزوير الذري
                String magicCmd = 
                    "APP_OWNER=$(stat -c '%U:%G' \"/data/data/" + currentPkg + "\") && " +
                    "if [ -z \"$APP_OWNER\" ]; then APP_OWNER=$(stat -c '%U:%G' \"/data/user_de/0/" + currentPkg + "\"); fi && " +
                    "cat /data/local/tmp/exhxx_temp.xml > \"" + fullPath + "\" && " +
                    "chown $APP_OWNER \"" + fullPath + "\" && " +
                    "chmod 660 \"" + fullPath + "\" && " +
                    "restorecon \"" + fullPath + "\" && " +
                    "rm /data/local/tmp/exhxx_temp.xml && " +
                    "am force-stop " + currentPkg;

                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", magicCmd});
                p.waitFor();
                
                runOnUiThread(() -> Toast.makeText(this, "✅ تمت العملية! (حقن ناجح 100%)", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "❌ فشل الحفظ!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
