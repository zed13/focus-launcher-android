package co.siempo.phone.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.siempo.phone.R;
import co.siempo.phone.adapters.TempoNotificationSectionAdapter;
import co.siempo.phone.app.CoreApplication;
import co.siempo.phone.event.AppInstalledEvent;
import co.siempo.phone.event.HomePressEvent;
import co.siempo.phone.helper.FirebaseHelper;
import co.siempo.phone.log.Tracer;
import co.siempo.phone.models.AppListInfo;
import co.siempo.phone.utils.PrefSiempo;
import co.siempo.phone.utils.Sorting;
import co.siempo.phone.utils.UIUtils;
import de.greenrobot.event.Subscribe;

/**
 * Created by hardik on 22/11/17.
 */

public class NotificationActivity extends CoreActivity {

    long startTime = 0;
    Toolbar toolbar;
    private RecyclerView lst_appList;
    private TextView titleActionBar;
    private ImageView imgBack;

    private List<String> pref_messengerList = new ArrayList<>();
    private ArrayList<String> pref_helpfulRobots = new ArrayList<>();
    private Set<String> pref_blockedList = new HashSet<>();

    private List<AppListInfo> messengerList = new ArrayList<>();
    private List<AppListInfo> blockedList = new ArrayList<>();
    private List<AppListInfo> helpfulRobot_List = new ArrayList<>();

    private ArrayList<String> pref_headerSectionList = new ArrayList<>();
    private List<AppListInfo> headerSectionList = new ArrayList<>();

    private List<String> systemAppList = new ArrayList<>();

    private PackageManager packageManager;

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(NotificationActivity.class.getSimpleName(), startTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempo_list_notification);

    }

    public void initView() {

        pref_messengerList = new ArrayList<>();
        pref_helpfulRobots = new ArrayList<>();
        pref_blockedList = new HashSet<>();

        blockedList = new ArrayList<>();
        messengerList = new ArrayList<>();
        helpfulRobot_List = new ArrayList<>();
        headerSectionList = new ArrayList<>();

        // Initialize components
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.select_apps_title);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lst_appList = findViewById(R.id.lst_appList);
        packageManager = getPackageManager();
        systemAppList = Arrays.asList(getResources().getStringArray(R.array.systemAppList));

        /*
          Load all preference list based on share preference
          Constants.HELPFUL_ROBOTS
          Constants.BLOCKED_APPLIST
         */

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        List<ResolveInfo> messagingResolveList = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : messagingResolveList) {
            if (!resolveInfo.activityInfo.packageName.equalsIgnoreCase(getPackageName())) {
                pref_messengerList.add(resolveInfo.activityInfo.packageName);
            }
        }
        String str_helpfulRobots = PrefSiempo.getInstance(this).read(PrefSiempo.HELPFUL_ROBOTS, "");
        if (!TextUtils.isEmpty(str_helpfulRobots)) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            pref_helpfulRobots = new Gson().fromJson(str_helpfulRobots, type);
        }
        pref_blockedList = PrefSiempo.getInstance(this).read(PrefSiempo.BLOCKED_APPLIST, new HashSet<String>());


        for (String packageName : CoreApplication.getInstance().getPackagesList()) {
            for (String blockedApp : pref_blockedList) {
                if (!packageName.equalsIgnoreCase(blockedApp)) {
                    pref_helpfulRobots.add(packageName);
                }
            }
        }

        String disableList = new Gson().toJson(pref_helpfulRobots);
        PrefSiempo.getInstance(this).write(PrefSiempo.HELPFUL_ROBOTS, disableList);
//        launcherPrefs.edit().putString(Constants.HELPFUL_ROBOTS, disableList).commit();

        String str_Header_AppList = PrefSiempo.getInstance(this).read(PrefSiempo.HEADER_APPLIST, "");
        if (!TextUtils.isEmpty(str_Header_AppList)) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            pref_headerSectionList = new Gson().fromJson(str_Header_AppList, type);
        }

        loadAndDisplayAppList();
    }

    @Subscribe
    public void appInstalledEvent(AppInstalledEvent event) {
        if (event.isAppInstalledSuccessfully()) {
            initView();
        }
    }

    /**
     * Prepare List for display based on preference list
     */
    public void loadAndDisplayAppList() {
        for (int i = 0; i < CoreApplication.getInstance().getPackagesList().size(); i++) {
            if (pref_blockedList.contains(CoreApplication.getInstance().getPackagesList().get(i))) {
                AppListInfo d = new AppListInfo();
                d.packageName = CoreApplication.getInstance().getPackagesList().get(i);
                d.ischecked = !pref_blockedList.contains(d.packageName);
                blockedList.add(d);
            } else if (pref_messengerList.contains(CoreApplication.getInstance().getPackagesList().get(i))) {
                AppListInfo d = new AppListInfo();
                d.packageName = CoreApplication.getInstance().getPackagesList().get(i);
                d.ischecked = !pref_helpfulRobots.contains(d.packageName);
                messengerList.add(d);
            } else {
                AppListInfo d = new AppListInfo();
                d.packageName = CoreApplication.getInstance().getPackagesList().get(i);
                if (!TextUtils.isEmpty(d.packageName) && !systemAppList.contains(d.packageName)) {
                    d.ischecked = !pref_helpfulRobots.contains(d.packageName);
                    helpfulRobot_List.add(d);
                }
            }
        }


        if (messengerList.size() >= 0) {
            AppListInfo d1 = new AppListInfo();
            d1.headerName = "Human direct messaging";
            d1.ischecked = !pref_headerSectionList.contains("Human direct messaging");
            headerSectionList.add(d1);
        }

        if (helpfulRobot_List.size() >= 0) {
            AppListInfo d2 = new AppListInfo();
            d2.headerName = "Helpful robots";

            d2.ischecked = !pref_headerSectionList.contains("Helpful robots");
            headerSectionList.add(d2);
        }

        if (pref_blockedList.size() >= 0) {
            AppListInfo d3 = new AppListInfo();
            d3.headerName = "All other apps";

            d3.ischecked = !pref_headerSectionList.contains("All other apps");
            headerSectionList.add(d3);
        }

        checkAppListEmpty(this, helpfulRobot_List, messengerList, blockedList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lst_appList.setLayoutManager(linearLayoutManager);
        lst_appList.setHasFixedSize(true);

        if (helpfulRobot_List.size() > 0) {
            helpfulRobot_List = Sorting.sortApplication(helpfulRobot_List);
        }
        if (messengerList.size() > 0) {
            messengerList = Sorting.sortApplication(messengerList);
        }
        if (blockedList.size() > 0) {
            blockedList = Sorting.sortApplication(blockedList);
        }

        TempoNotificationSectionAdapter adapter = new TempoNotificationSectionAdapter(this, helpfulRobot_List, messengerList, blockedList, headerSectionList);

        lst_appList.setAdapter(adapter);
    }

    public void checkAppListEmpty(Context context, List<AppListInfo> appList, List<AppListInfo> messengerList, List<AppListInfo> blockedAppList) {
        if (messengerList.size() > 1) {
            for (int i = 0; i < messengerList.size(); i++)
                if (!TextUtils.isEmpty(messengerList.get(i).errorMessage)) {
                    messengerList.remove(messengerList.get(i));
                }
        }

        if (appList.size() > 1) {
            for (int i = 0; i < appList.size(); i++)
                if (!TextUtils.isEmpty(appList.get(i).errorMessage)) {
                    appList.remove(appList.get(i));
                }
        }

        if (blockedAppList.size() > 1) {
            for (int i = 0; i < blockedAppList.size(); i++)
                if (!TextUtils.isEmpty(blockedAppList.get(i).errorMessage)) {
                    blockedAppList.remove(blockedAppList.get(i));
                }
        }

        if (messengerList.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            messengerList.add(d);
        }

        if (appList.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            appList.add(d);
        }


        if (blockedAppList.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            blockedAppList.add(d);
        }

    }


    @Subscribe
    public void homePressEvent(HomePressEvent event) {
        try {
            if (event.isVisible() && UIUtils.isMyLauncherDefault(this)) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }

        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }

}