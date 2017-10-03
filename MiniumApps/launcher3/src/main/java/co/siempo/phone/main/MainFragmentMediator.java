package co.siempo.phone.main;

import android.content.pm.ApplicationInfo;

import java.util.ArrayList;
import java.util.List;

import co.siempo.phone.MainActivity;
import co.siempo.phone.R;
import co.siempo.phone.app.Launcher3App;
import co.siempo.phone.contact.ContactsLoader;
import co.siempo.phone.event.CreateNoteEvent;
import co.siempo.phone.event.SendSmsEvent;
import co.siempo.phone.helper.ActivityHelper;
import co.siempo.phone.model.ContactListItem;
import co.siempo.phone.model.MainListItem;
import co.siempo.phone.model.MainListItemType;
import co.siempo.phone.token.TokenItemType;
import co.siempo.phone.token.TokenRouter;
import de.greenrobot.event.EventBus;
import minium.co.core.log.Tracer;
import minium.co.core.util.UIUtils;

/**
 * Created by shahab on 2/16/17.
 */

class MainFragmentMediator {

    private MainFragment fragment;

    private List<MainListItem> items;
    private List<ContactListItem> contactItems;

    MainFragmentMediator(MainFragment mainFragment) {
        this.fragment = mainFragment;
    }

    void loadData() {
        items = new ArrayList<>();
        loadActions();
        loadContacts();
        loadDefaults();
    }

    void resetData() {
        items.clear();
        loadActions();
        loadContacts();
        loadDefaults();
        if (getAdapter() != null) {
            getAdapter().loadData(items);
            getAdapter().notifyDataSetChanged();
        }
    }

    private void loadActions() {
        new MainListItemLoader(fragment.getActivity()).loadItems(items, fragment);
    }


    private void loadAppList() {
        try {
            if (Launcher3App.getInstance().getPackagesList() != null && Launcher3App.getInstance().getPackagesList().size() > 0) {
                items.clear();
                for (ApplicationInfo applicationInfo : Launcher3App.getInstance().getPackagesList()) {
                    String appName = applicationInfo.loadLabel(fragment.getActivity().getPackageManager()).toString();
//                    items.add(new MainListItem(appName,MainListItemType.APPS,applicationInfo));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadContacts() {
        try {
            if (fragment.getManager() != null && fragment.getManager().hasCompleted(TokenItemType.CONTACT)) {
                return;
            }
            if (contactItems == null) {
                contactItems = new ContactsLoader().loadContacts(fragment.getActivity());
            }
            items.addAll(contactItems);
        } catch (Exception e) {
            Tracer.e(e, e.getMessage());
        }
    }

    private void loadDefaults() {
        try {
            if (fragment.getManager().hasCompleted(TokenItemType.CONTACT) && fragment.getManager().has(TokenItemType.DATA) && !fragment.getManager().get(TokenItemType.DATA).getTitle().isEmpty()) {
                items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.icon_sms, MainListItemType.DEFAULT));
            } else if (fragment.getManager().hasCompleted(TokenItemType.CONTACT)) {
                items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.icon_sms, MainListItemType.DEFAULT));
                items.add(new MainListItem(4, fragment.getString(R.string.title_call), R.drawable.icon_call, MainListItemType.DEFAULT));
            } else if (fragment.getManager().hasCompleted(TokenItemType.DATA)) {
                items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.icon_sms, MainListItemType.DEFAULT));
                items.add(new MainListItem(3, fragment.getString(R.string.title_createContact), R.drawable.icon_create_user, MainListItemType.DEFAULT));
                items.add(new MainListItem(2, fragment.getString(R.string.title_saveNote), R.drawable.icon_save_note, MainListItemType.DEFAULT));
            } else {
                items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.icon_sms, MainListItemType.DEFAULT));
                items.add(new MainListItem(3, fragment.getString(R.string.title_createContact), R.drawable.icon_create_user, MainListItemType.DEFAULT));
                items.add(new MainListItem(2, fragment.getString(R.string.title_saveNote), R.drawable.icon_save_note, MainListItemType.DEFAULT));
            }
        } catch (Exception e) {
            Tracer.e(e, e.getMessage());
        }
    }

    List<MainListItem> getItems() {
        return items;
    }

    private MainListAdapter getAdapter() {
        return fragment.getAdapter();
    }

    void listItemClicked(TokenRouter router, int position) {
        MainListItemType type = getAdapter().getItem(position).getItemType();

        switch (type) {
            case CONTACT:
                router.contactPicked((ContactListItem) getAdapter().getItem(position));
                break;
            case ACTION:
                if (getAdapter().getItem(position).getApplicationInfo() != null) {
                    if(fragment!=null && fragment.getActivity()!=null && fragment.getActivity() instanceof  MainActivity){
                        MainActivity mainActivity = (MainActivity)fragment.getActivity();
                        if(mainActivity!=null) {
                            mainActivity.restoreSiempoNotificationBar();
                        }
                    }
                    UIUtils.hideSoftKeyboard(fragment.getActivity(), fragment.getActivity().getWindow().getDecorView().getWindowToken());
                    new ActivityHelper(fragment.getActivity()).openGMape(getAdapter().getItem(position).getApplicationInfo().packageName);
                    MainActivity.isTextLenghGreater = "";
                } else {
                    position = getAdapter().getItem(position).getId();
                    new MainListItemLoader(fragment.getActivity()).listItemClicked(position);
                }
                break;
            case DEFAULT:
                position = getAdapter().getItem(position).getId();

                switch (position) {
                    case 1:
                        router.sendText(fragment.getActivity());
                        break;
                    case 2:
                        router.createNote(fragment.getActivity());
                        EventBus.getDefault().post(new CreateNoteEvent());
                        break;
                    case 3:
                        if(fragment!=null && fragment.getActivity()!=null && fragment.getActivity() instanceof  MainActivity){
                            MainActivity mainActivity = (MainActivity)fragment.getActivity();
                            if(mainActivity!=null) {
                                mainActivity.restoreSiempoNotificationBar();
                            }
                        }
                        router.createContact(fragment.getActivity());
                        break;
                    case 4:
                        router.call(fragment.getActivity());
                        MainActivity.isTextLenghGreater = "";
                        EventBus.getDefault().post(new SendSmsEvent(true,"",""));
                        break;
                    default:
                        UIUtils.alert(fragment.getActivity(), fragment.getString(R.string.msg_not_yet_implemented));
                        break;
                }
                break;
            case NUMBERS:
                router.contactNumberPicked(getAdapter().getItem(position));
                break;
        }
    }

    public void contactPicker() {
        items.clear();
        loadContacts();
        loadDefaults();
        getAdapter().loadData(items);
        getAdapter().notifyDataSetChanged();
    }

    public void contactNumberPicker(int selectedContactId) {
        items.clear();
        for (ContactListItem item : contactItems) {
            if (item.getContactId() == selectedContactId) {
                for (ContactListItem.ContactNumber number : item.getNumbers()) {
                    items.add(new MainListItem(selectedContactId, number.getNumber(), R.drawable.icon_call, MainListItemType.NUMBERS));
                }
            }
        }
        getAdapter().loadData(items);
        getAdapter().notifyDataSetChanged();
    }

    public void listItemClicked2(TokenRouter router, int position) {

    }

    public void defaultData() {
        items.clear();
        loadDefaults();
        getAdapter().loadData(items);
        getAdapter().notifyDataSetChanged();
    }


    public void installedApplicationList() {
        items.clear();
        loadAppList();
        getAdapter().loadData(items);
        getAdapter().notifyDataSetChanged();
    }



}
