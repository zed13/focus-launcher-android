package co.siempo.phone.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "TABLE_NOTIFICATION_SMS".
 */
@Entity
public class TableNotificationSms implements Serializable {
    static final long serialVersionUID = 42L;
    @Id
    private Long id;
    private String _contact_title;
    private String _message;
    private java.util.Date _date;
    private Integer _contact_id;
    private Integer _sms_id;
    private Long _snooze_time;
    private Boolean _is_read;

    private int app_icon;
    private byte[] user_icon;
    private Integer notification_type;
    private String packageName;
    private int content_type;
    private int notification_id;

    public long getNotification_date() {
        return notification_date;
    }

    public void setNotification_date(long notification_date) {
        this.notification_date = notification_date;
    }

    private long notification_date;

    @Generated(hash = 920815365)
    public TableNotificationSms() {
    }

    public TableNotificationSms(Long id) {
        this.id = id;
    }

    @Generated(hash = 584408565)
    public TableNotificationSms(Long id, String _contact_title, String _message, java.util.Date _date, Integer _contact_id, Integer _sms_id, Long _snooze_time, Boolean _is_read,
            int app_icon, byte[] user_icon, Integer notification_type, String packageName, int content_type, int notification_id, long notification_date) {
        this.id = id;
        this._contact_title = _contact_title;
        this._message = _message;
        this._date = _date;
        this._contact_id = _contact_id;
        this._sms_id = _sms_id;
        this._snooze_time = _snooze_time;
        this._is_read = _is_read;
        this.app_icon = app_icon;
        this.user_icon = user_icon;
        this.notification_type = notification_type;
        this.packageName = packageName;
        this.content_type = content_type;
        this.notification_id = notification_id;
        this.notification_date = notification_date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String get_contact_title() {
        return _contact_title;
    }

    public void set_contact_title(String _contact_title) {
        this._contact_title = _contact_title;
    }

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }

    public java.util.Date get_date() {
        return _date;
    }

    public void set_date(java.util.Date _date) {
        this._date = _date;
    }

    public Integer get_contact_id() {
        return _contact_id;
    }

    public void set_contact_id(Integer _contact_id) {
        this._contact_id = _contact_id;
    }

    public Integer get_sms_id() {
        return _sms_id;
    }

    public void set_sms_id(Integer _sms_id) {
        this._sms_id = _sms_id;
    }

    public Long get_snooze_time() {
        return _snooze_time;
    }

    public void set_snooze_time(Long _snooze_time) {
        this._snooze_time = _snooze_time;
    }

    public Boolean get_is_read() {
        return _is_read;
    }

    public void set_is_read(Boolean _is_read) {
        this._is_read = _is_read;
    }

    public int getApp_icon() {
        return app_icon;
    }

    public void setApp_icon(int app_icon) {
        this.app_icon = app_icon;
    }

    public byte[] getUser_icon() {
        return user_icon;
    }

    public void setUser_icon(byte[] user_icon) {
        this.user_icon = user_icon;
    }

    public Integer getNotification_type() {
        return this.notification_type;
    }

    public void setNotification_type(Integer notification_type) {
        this.notification_type = notification_type;
    }

    public int getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(int notification_id) {
        this.notification_id = notification_id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getContent_type() {
        return content_type;
    }

    public void setContent_type(int content_type) {
        this.content_type = content_type;
    }
    

}
