package co.siempo.phone.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import co.siempo.phone.MainActivity;
import co.siempo.phone.R;
import co.siempo.phone.adapters.MainListAdapter;
import co.siempo.phone.app.CoreApplication;
import co.siempo.phone.event.CreateNoteEvent;
import co.siempo.phone.event.SendSmsEvent;
import co.siempo.phone.fragments.PaneFragment;
import co.siempo.phone.helper.ActivityHelper;
import co.siempo.phone.helper.FirebaseHelper;
import co.siempo.phone.log.Tracer;
import co.siempo.phone.models.MainListItem;
import co.siempo.phone.models.MainListItemType;
import co.siempo.phone.token.TokenItemType;
import co.siempo.phone.token.TokenManager;
import co.siempo.phone.token.TokenRouter;
import co.siempo.phone.utils.ContactsLoader;
import co.siempo.phone.utils.PackageUtil;
import co.siempo.phone.utils.PrefSiempo;
import co.siempo.phone.utils.Sorting;
import co.siempo.phone.utils.UIUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by shahab on 2/16/17.
 */

public class MainFragmentMediator {

    private final Context context;
    private PaneFragment fragment;

    private List<MainListItem> items;
    private List<MainListItem> contactItems;

    public MainFragmentMediator(PaneFragment paneFragment) {
        this.fragment = paneFragment;
        this.context = this.fragment.getActivity();
    }

    public void loadData() {
        if (TextUtils.isEmpty(PrefSiempo.getInstance(context).read(PrefSiempo.SERACH_LIST, ""))) {
            items = new ArrayList<>();
            contactItems = new ArrayList<>();
            loadActions();
            loadContacts();
            loadDefaults();
            items = Sorting.sortList(items);
            PackageUtil.storeSearchList(items, context);
        } else {
            items = PackageUtil.getSearchList(context);
        }
    }

    public void resetData() {
        items.clear();

        if (TextUtils.isEmpty(PrefSiempo.getInstance(context).read(PrefSiempo.SERACH_LIST, ""))) {
            items = new ArrayList<>();
            contactItems = new ArrayList<>();
            loadActions();
            loadContacts();
            loadDefaults();
            items = Sorting.sortList(items);
            PackageUtil.storeSearchList(items, context);
        } else {
            items = PackageUtil.getSearchList(context);
            items = Sorting.sortList(items);
        }
        if (getAdapter() != null) {
            getAdapter().loadData(items);
            getAdapter().notifyDataSetChanged();
        }
    }

    private void loadActions() {
        new MainListItemLoader(fragment.getActivity()).loadItems(items, fragment);
    }


    private void loadContacts() {
        try {
            if (fragment != null && fragment.getManager() != null && fragment.getManager().hasCompleted(TokenItemType.CONTACT)) {
                return;
            }
            if (fragment != null && contactItems.size() == 0) {
                contactItems = new ContactsLoader().loadContacts(fragment.getActivity());
            }
            items.addAll(contactItems);
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }

    private void loadDefaults() {
        try {
            if (fragment != null) {

                if (fragment.getManager().hasCompleted(TokenItemType.CONTACT) && fragment.getManager().has(TokenItemType.DATA) && !fragment.getManager().get(TokenItemType.DATA).getTitle().isEmpty()) {
                    items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.ic_messages_tool, MainListItemType.DEFAULT));
                } else if (fragment.getManager().hasCompleted(TokenItemType.CONTACT)) {
                    items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.ic_messages_tool, MainListItemType.DEFAULT));
                    items.add(new MainListItem(4, fragment.getString(R
                            .string.title_call), R.drawable.ic_call_filter,
                            MainListItemType.DEFAULT));
                } else if (fragment.getManager().hasCompleted(TokenItemType.DATA)) {
                    items.add(new MainListItem(1, fragment.getString(R
                            .string.title_sendAsSMS), R.drawable.ic_messages_tool,
                            MainListItemType.DEFAULT));
                    items.add(new MainListItem(2, fragment.getString(R
                            .string.title_saveNote), R.drawable.ic_notes_tool,
                            MainListItemType.DEFAULT));
                    items.add(new MainListItem(3, fragment.getString(R.string.title_swipe), R.drawable.ic_default_swipe, MainListItemType.DEFAULT));
                } else {
                    items.add(new MainListItem(4, fragment.getString(R
                            .string.title_call), R.drawable.ic_call_filter,
                            MainListItemType.NUMBERS));
                    items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.ic_messages_tool, MainListItemType.DEFAULT));
                    items.add(new MainListItem(2, fragment.getString(R.string.title_saveNote), R.drawable.ic_notes_tool, MainListItemType.DEFAULT));
                    items.add(new MainListItem(3, fragment.getString(R.string.title_swipe), R.drawable.ic_default_swipe, MainListItemType.DEFAULT));
                }
            }
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }

    public List<MainListItem> getItems() {
        return items;
    }

    private MainListAdapter getAdapter() {
        return fragment.getAdapter();
    }

    public void listItemClicked(TokenRouter router, int position, String data) {
        MainListItemType type;
        type = getAdapter().getItem(position).getItemType();
        if (type != null)
            switch (type) {
                case CONTACT:
                    if (router != null) {
                        router.contactPicked(getAdapter().getItem(position));
                        FirebaseHelper.getIntance().logIFAction(FirebaseHelper.ACTION_CONTACT_PICK, "", data);
                    }
                    break;
                case ACTION:
                    if (getAdapter() != null && getAdapter().getItem(position).getApplicationInfo() == null) {
                        //On Click of item , storing the timestamp
                        items.get(position).setDate(Calendar.getInstance().getTime());
                        items.set(position, items.get(position));
                        items = Sorting.sortList(items);
                        PackageUtil.storeSearchList(items, context);
                        position = getAdapter().getItem(position).getId();
                        new MainListItemLoader(fragment.getActivity()).listItemClicked(position);
                    } else {
                        if (fragment != null) {
                            MainActivity.isTextLenghGreater = "";
                            UIUtils.hideSoftKeyboard(fragment.getActivity(), fragment.getActivity().getWindow().getDecorView().getWindowToken());
                            boolean status = new ActivityHelper(fragment.getActivity()).openAppWithPackageName(getAdapter().getItem(position).getApplicationInfo().packageName);
                            FirebaseHelper.getIntance().logIFAction(FirebaseHelper.ACTION_APPLICATION_PICK, getAdapter().getItem(position).getApplicationInfo().packageName, "");
                            if (status) {
                                items.get(position).setDate(Calendar.getInstance().getTime());
                                items.set(position, items.get(position));
                                items = Sorting.sortList(items);
                                PackageUtil.storeSearchList(items, context);
                            }
                        }
                    }
                    break;
                case DEFAULT:
                    position = getAdapter().getItem(position).getId();
                    switch (position) {
                        case 1:
                            if (router != null && fragment != null) {
                                router.sendText(fragment.getActivity());
                                FirebaseHelper.getIntance().logIFAction(FirebaseHelper.ACTION_SMS, "", data);
                            }
                            break;
                        case 2:
                            if (router != null && fragment != null) {
                                router.createNote(fragment.getActivity());
                                FirebaseHelper.getIntance().logIFAction(FirebaseHelper.ACTION_SAVE_NOTE, "", data);
                                EventBus.getDefault().post(new CreateNoteEvent());
                            }
                            break;
                        case 3:
                            break;
                        case 4:
                            if (router != null && fragment != null) {
                                router.call(fragment.getActivity());
                                MainActivity.isTextLenghGreater = "";
                                FirebaseHelper.getIntance().logIFAction(FirebaseHelper.ACTION_CALL, "", data);
                                EventBus.getDefault().post(new SendSmsEvent(true, "", ""));
                            }
                            break;
                        default:
                            UIUtils.alert(fragment.getActivity(), fragment.getString(R.string.msg_not_yet_implemented));
                            break;
                    }
                    break;
                case NUMBERS:
                    if (getAdapter().getItem(position).getTitle().trim().equalsIgnoreCase("call") && getAdapter().getItem(position).getId() == 4) {
                        try {
                            fragment.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + TokenManager.getInstance().getCurrent().getExtra2())));
                            MainActivity.isTextLenghGreater = "";
                            FirebaseHelper.getIntance().logIFAction(FirebaseHelper.ACTION_CALL, "", data);
                            EventBus.getDefault().post(new SendSmsEvent(true, "", ""));
                        } catch (Exception e) {
                            CoreApplication.getInstance().logException(e);
                            e.printStackTrace();
                        }
                    } else {
                        if (router != null) {
                            router.contactNumberPicked(getAdapter().getItem(position));
                            FirebaseHelper.getIntance().logIFAction(FirebaseHelper.ACTION_CONTACT_PICK, "", data);
                        }
                    }
                    break;
                default:
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
        if (contactItems != null) {
            for (MainListItem item : contactItems) {
                if (item != null && item.getContactId() == selectedContactId) {
                    for (MainListItem.ContactNumber number : item.getNumbers()) {
                        items.add(new MainListItem(selectedContactId, number
                                .getNumber(), R.drawable.ic_call_filter,
                                MainListItemType.NUMBERS));
                    }
                }
            }
            getAdapter().loadData(items);
            getAdapter().notifyDataSetChanged();
        }
    }

    public void defaultData() {
        items.clear();
        loadDefaults();
        getAdapter().loadData(items);
        getAdapter().notifyDataSetChanged();
    }

}
