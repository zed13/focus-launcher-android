package co.siempo.phone.fragments;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import co.siempo.phone.R;
import co.siempo.phone.activities.ChooseBackgroundActivity;
import co.siempo.phone.event.NotifyBackgroundChange;
import co.siempo.phone.event.ThemeChangeEvent;
import co.siempo.phone.helper.FirebaseHelper;
import co.siempo.phone.utils.PermissionUtil;
import co.siempo.phone.utils.PrefSiempo;
import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_tempo_home)
public class TempoHomeFragment extends CoreFragment {

    @ViewById
    Toolbar toolbar;

    @ViewById
    Switch switchDisableIntentionsControls;

    @ViewById
    RelativeLayout relAllowSpecificApps;

    @ViewById
    Switch switchCustomBackground;

    @ViewById
    RelativeLayout relCustomBackground;

    @ViewById
    Switch switchDarkTheme;

    @ViewById
    RelativeLayout relDarkTheme;
    private PermissionUtil permissionUtil;

    public TempoHomeFragment() {
        // Required empty public constructor
    }


    @AfterViews
    void afterViews() {
        // Download siempo images
        permissionUtil = new PermissionUtil(getActivity());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.homescreen);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });
        switchDisableIntentionsControls.setChecked(PrefSiempo.getInstance(context).read(PrefSiempo.IS_INTENTION_ENABLE, false));
        switchDisableIntentionsControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .IS_INTENTION_ENABLE, isChecked);
                FirebaseHelper.getInstance().logIntention_IconBranding_Randomize(FirebaseHelper.INTENTIONS, isChecked ? 1 : 0);

            }
        });

        if (PrefSiempo.getInstance(context).read(PrefSiempo
                .IS_DARK_THEME, false)) {
            switchDarkTheme.setChecked(true);
        } else {
            switchDarkTheme.setChecked(false);
        }

        switchDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .IS_DARK_THEME, isChecked);
                EventBus.getDefault().postSticky(new ThemeChangeEvent(true));
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finish();
                    }
                }, 60);
            }
        });

        switchCustomBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strImage = PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_BAG, "");
                boolean isEnable = PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_BAG_ENABLE, false);
                if (isEnable
                        && !TextUtils.isEmpty(strImage)) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_BAG_ENABLE, false);
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_BAG, "");
                    EventBus.getDefault().postSticky(new NotifyBackgroundChange(true));
                    switchCustomBackground.setChecked(false);
                } else if (!isEnable && TextUtils.isEmpty(strImage)) {
                    //    startActivity(new Intent(context, ChooseBackgroundActivity.class));
                    switchCustomBackground.setChecked(false);
                    checkPermissionsForBackground();

                } else if (!isEnable && !TextUtils.isEmpty(strImage)) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_BAG_ENABLE, true);
                    EventBus.getDefault().postSticky(new NotifyBackgroundChange(true));
                    switchCustomBackground.setChecked(true);
                }

            }
        });
        //   CoreApplication.getInstance().downloadSiempoImages();
    }

    @Click
    void relAllowSpecificApps() {
        switchDisableIntentionsControls.performClick();
    }

    @Click
    void relCustomBackground() {
        checkPermissionsForBackground();
        //startActivity(new Intent(context, ChooseBackgroundActivity.class));
    }

    private void checkPermissionsForBackground() {
        //permissionUtil = new PermissionUtil(context);
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionUtil.hasGiven
                (PermissionUtil.WRITE_EXTERNAL_STORAGE_PERMISSION))) {
            try {
                TedPermission.with(context)
                        .setPermissionListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                startActivity(new Intent(context, ChooseBackgroundActivity.class));
                            }

                            @Override
                            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                            }
                        })
                        .setDeniedMessage(R.string.msg_permission_denied)
                        .setPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest
                                        .permission
                                        .READ_EXTERNAL_STORAGE,})
                        .check();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            startActivity(new Intent(context, ChooseBackgroundActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String strImage = PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_BAG, "");
        boolean isEnable = PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_BAG_ENABLE, false);
        if (isEnable
                && !TextUtils.isEmpty(strImage)) {
            switchCustomBackground.setChecked(true);
        } else if (!isEnable && TextUtils.isEmpty(strImage)) {
            switchCustomBackground.setChecked(false);
            PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_BAG_ENABLE, false);
        }
    }

    @Click
    void relDarkTheme() {
        switchDarkTheme.performClick();
    }


}
