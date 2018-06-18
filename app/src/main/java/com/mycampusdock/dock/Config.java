package com.mycampusdock.dock;

public class Config {
    public static final String REQ_URL_SIGNIN = "https://mycampusdock.com/android/signin";
    public static final String REQ_URL_VERIFY = "https://mycampusdock.com/android/signin/verify";
    public static final String REQ_URL_HIERARCHY = "https://mycampusdock.com/hierarchy";
    public static final String REQ_URL_EVENT_ENROLL = "https://mycampusdock.com/android/event/enroll";
    public static final String REQ_URL_EVENT_UNENROLL = "https://mycampusdock.com/android/event/unenroll";
    public static final String REQ_URL_EVENT_FEEDBACK = "https://mycampusdock.com/android/feedback";
    public static final String REQ_URL_EVENT_CHECK_ENROLL = "https://mycampusdock.com/android/event/check-enrolled";
    public static final String REQ_URL_UPDATE_USER_DATA = "https://mycampusdock.com/android/update-user-data";
    public static final String REQ_URL_SCOPE_VALIDATE = "https://mycampusdock.com/android/check-scope-validity";
    public static final String REQ_URL_DATA_FROM_SCOPE_LIST = "https://mycampusdock.com/android/get-data-for-scope-list";
    public static final String REQ_URL_REACH = "https://mycampusdock.com/android/reach";
    public static final String SHARED_PREF_NAME = "OGIL";
    public static final int REACH_TYPE_EVENT = 101;
    public static final int REACH_TYPE_BULLETIN = 102;
    public static final int REACH_TYPE_NOTIFICATION = 103;
    public static final int REQ_TIME_OUT = 30000;
    public static final String FLAG_REG_COMPLETE = "firebase_res_complete";
    public static final String URL_CHAT_SERVER = "https://dock-chat.herokuapp.com";
    public static final String URL_BASE_MEDIA = "https://mycampusdock.com/media/";

    public static final String EMAIL_HELP_STRING = "1. Please make sure that the email can be in spam folder." +
            "\n\n2. Check the email entered is correct or not." +
            "\n\n3. Wait for the email sometimes is takes a few minutes to reach." +
            "\n\n4. Make sure your internet is working." +
            "\n\n5. If still you have trouble in login, please write at help@campusdock.com.\n\n Thanks for being patient, it matters!";

    public static final String HELP_STRING = "1. If your college is not listed please let us know at our email." +
            "\n\n2. Wrong details are filled, no problem write to help@campusdock.com" +
            "\n\n3. Want to know, how to advertise with us, drop a mail sales@campusdock.com";
}
