package com.tu.listpackage;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
  private List<Map<String, Object>> data;
  Map<String, Object> item;
  private ListView listView = null;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    listView = new ListView(this);
    data = new ArrayList<Map<String, Object>>();
    listPackages();
    SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2,
        new String[] { "appname", "pname" }, new int[] { android.R.id.text1, android.R.id.text2, });
    listView.setAdapter(adapter);
    setContentView(listView);
    getUsageApps();
  }

  class PInfo {
    private String appname = "";
    private String pname = "";
    private String versionName = "";
    private int versionCode = 0;
    private Drawable icon;

    private void prettyPrint() {
      Log.i("taskmanger", appname + "\t" + pname + "\t" + versionName + "\t" + versionCode + "\t");
    }
  }

  private void listPackages() {
    ArrayList<PInfo> apps = getInstalledApps(false);
    final int max = apps.size();
    for (int i = 0; i < max; i++) {
      apps.get(i).prettyPrint();
      item = new HashMap<String, Object>();
      item.put("appname", apps.get(i).appname);
      item.put("pname", apps.get(i).pname);
      data.add(item);
    }
  }

  private ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {
    ArrayList<PInfo> res = new ArrayList<PInfo>();
    List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
    for (int i = 0; i < packs.size(); i++) {
      PackageInfo p = packs.get(i);
      if ((!getSysPackages) && (p.versionName == null)) {
        continue;
      }
      PInfo newInfo = new PInfo();
      newInfo.appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
      newInfo.pname = p.packageName;
      newInfo.versionName = p.versionName;
      newInfo.versionCode = p.versionCode;
      newInfo.icon = p.applicationInfo.loadIcon(getPackageManager());
      res.add(newInfo);
    }
    return res;
  }

  private void getUsageApps() {

    String topPackageName;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      String permission = "android.permission.PACKAGE_USAGE_STATS";
      AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
      int mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(),
          getPackageName());
      if (mode == AppOpsManager.MODE_ALLOWED) {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
        long currentTime = System.currentTimeMillis();
        // get usage stats for the last 10 seconds
        List<UsageStats> stats =
            mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                currentTime - 1000 * 10, currentTime);
        // search for app with most recent last used time
        if (stats != null) {
          long lastUsedAppTime = 0;
          for (UsageStats usageStats : stats) {
            if (usageStats.getLastTimeUsed() > lastUsedAppTime) {
              topPackageName = usageStats.getPackageName();
              lastUsedAppTime = usageStats.getLastTimeUsed();
              Date date = new Date(lastUsedAppTime);
              System.out.println(date.toString());
            }
          }
        }
      } else {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
      }
    }
  }
}
