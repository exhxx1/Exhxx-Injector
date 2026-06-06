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
    Spinner appSpinner, fileSpinner;
    EditText xmlEditor;
    Button btnRead, btnSave;
    TextView rootStatus;
    
    List<String> pkgList = new ArrayList<>();
    List<String> fullPathList = new ArrayList<>();
    boolean hasRoot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("Exhxx Prefs Injector V2.4 💉\n(Flawless Edition) - Dev: Mohammed Adnan");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 20);

        rootStatus = new TextView(this);
        rootStatus.setText("⏳ جاري تهيئة النظام والمحرك...");
        rootStatus.setTextColor(Color.YELLOW);
        rootStatus.setPadding(0, 0, 0, 40);
        
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
        btnSave.setText("💾 حقن التعديلات (تخطي الحماية)");
        btnSave.setBackgroundColor(Color.parseColor("#43A047"));
        btnSave.setTextColor(Color.WHITE);

        layout.addView(title);
        layout.addView(rootStatus);
        layout.addView(lbl1);
        layout.addView(appSpinner);
        layout.addView(lbl2);
        layout.addView(fileSpinner);
        layout.addView(btnRead);
        layout.addView(xmlEditor);
        layout.addView(btnSave);
        
        scroll.addView(layout);
        setContentView(scroll);

        // وضع نصوص مبدئية حتى لا تظهر القائمة فارغة
        appSpinner.setAdapter(createCustomAdapter(Arrays.asList("⏳ جاري الفحص...")));
        fileSpinner.setAdapter(createCustomAdapter(Arrays.asList("...")));

        checkRootAndLoadApps();

        appSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && hasRoot && !pkgList.isEmpty()) {
                    loadAppFiles(pkgList.get(position));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnRead.setOnClickListener(v -> readFile());
        btnSave.setOnClickListener(v -> saveFile());
    }

    private ArrayAdapter<String> createCustomAdapter(List<String> list) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(14);
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

    // تم دمج فحص الرووت وتحميل التطبيقات بخيط واحد لضمان الأداء 100%
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
                List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pm));
                
                List<String> displayNames = new ArrayList<>();
                List<String> tempPkgList = new ArrayList<>();
                displayNames.add("-- اختر تطبيقاً --");
                tempPkgList.add("");

                for (ApplicationInfo app : apps) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        displayNames.add(app.loadLabel(pm).toString() + " (" + app.packageName + ")");
                        tempPkgList.add(app.packageName);
                    }
                }
                
                runOnUiThread(() -> {
                    pkgList.clear();
                    pkgList.addAll(tempPkgList);
                    rootStatus.setText("🟢 الرووت متوفر. المحرك جاهز!");
                    rootStatus.setTextColor(Color.GREEN);
                    appSpinner.setAdapter(createCustomAdapter(displayNames));
                });
            } else {
                runOnUiThread(() -> {
                    rootStatus.setText("🔴 الرووت غير متوفر أو مرفوض من Magisk!");
                    rootStatus.setTextColor(Color.RED);
                    appSpinner.setAdapter(createCustomAdapter(Arrays.asList("❌ فشل التحميل")));
                });
            }
        }).start();
    }

    private void loadAppFiles(String pkg) {
        fileSpinner.setAdapter(createCustomAdapter(Arrays.asList("⏳ جاري البحث المجهري...")));
        
        new Thread(() -> {
            try {
                // استخدام علامات التنصيص لتجنب أخطاء المسارات
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "find \"/data/data/" + pkg + "\" -type f -name '*.xml'"});
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
                
                List<String> displayFiles = new ArrayList<>();
                List<String> tempFullPathList = new ArrayList<>();
                
                String line;
                while ((line = br.readLine()) != null) {
                    tempFullPathList.add(line);
                    displayFiles.add(line.replace("/data/data/" + pkg + "/", ""));
                }
                
                runOnUiThread(() -> {
                    fullPathList.clear();
                    fullPathList.addAll(tempFullPathList);
                    if (displayFiles.isEmpty()) {
                        displayFiles.add("-- لا توجد ملفات XML --");
                        fullPathList.add("");
                    }
                    fileSpinner.setAdapter(createCustomAdapter(displayFiles));
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
        int appPos = appSpinner.getSelectedItemPosition();
        int filePos = fileSpinner.getSelectedItemPosition();
        String content = xmlEditor.getText().toString();
        
        if (appPos <= 0 || filePos < 0 || fullPathList.isEmpty() || fullPathList.get(filePos).isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "بيانات غير مكتملة!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String pkg = pkgList.get(appPos);
        String fullPath = fullPathList.get(filePos);
        
        new Thread(() -> {
            try {
                File tempFile = new File(getCacheDir(), "exhxx_temp.xml");
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8");
                osw.write(content);
                osw.close();

                // جعل الملف متاح للرووت لتخطي الـ SELinux
                tempFile.setReadable(true, false);

                // أمر السحر المدرع بالكامل (محمي بعلامات تنصيص)
                String magicCmd = 
                    "APP_OWNER=$(stat -c '%U:%G' \"/data/data/" + pkg + "\") && " +
                    "cat \"" + tempFile.getAbsolutePath() + "\" > \"" + fullPath + "\" && " +
                    "chown $APP_OWNER \"" + fullPath + "\" && " +
                    "chmod 660 \"" + fullPath + "\" && " +
                    "restorecon \"" + fullPath + "\" && " +
                    "am force-stop " + pkg;

                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", magicCmd});
                p.waitFor();
                
                // تنظيف الملف المؤقت
                tempFile.delete();
                
                runOnUiThread(() -> Toast.makeText(this, "✅ تمت العملية! (حقن ناجح 100%)", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "❌ فشل الحفظ!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
