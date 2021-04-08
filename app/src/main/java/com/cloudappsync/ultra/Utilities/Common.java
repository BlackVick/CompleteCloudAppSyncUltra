package com.cloudappsync.ultra.Utilities;

public class Common {

    //app details
    public static final String USER_NAME = "UserName";
    public static final String COMPANY_ID = "CompanyId";
    public static final String LICENCE_ID = "LicenceId";
    public static final String CURRENT_DB_LICENCE = "CurrentLicenceDb";

    //new master domain algorithm
    public static final String CURRENT_MASTER_DOMAIN = "MasterDomain";
    public static final String MASTER_DOMAINS = "MasterDomainList";
    public static final String DEFAULT_DOMAIN_FILE = "DefaultDomains.csv";
    public static final String CUSTOM_DOMAINS_FILE = "CustomDomains.csv";
    public static final String VERIFICATION_FILE = "UserVerification.csv";

    //visual mode
    public static final String VISUAL_STYLE = "VisualStyle";
    public static final String NIGHT_MODE = "NightMode";
    public static final String DAY_MODE = "DayMode";

    //important static app details
    public static final String LOAD_STYLE = "LoadStyle";

    //load styles
    public static final String LOAD_FROM_ONLINE = "LoadFromOnline";
    public static final String LOAD_FROM_LOCAL = "LoadFromLocal";
    public static final String LOAD_FROM_SERVER = "LoadFromServer";

    //licenced string values
    public static final String LICENCED_PAGE_URL = "LicencedPageUrl";
    public static final String LICENCED_LOCAL_DIR = "LicencedLocalDir";

    //scheduled redirect
    public static final String SAVED_SCHEDULES = "SavedSchedules";
    public static final String CURRENT_DAY = "CurrentToday";
    public static final String SCHEDULE_INTENT_ID = "ScheduleId";
    public static final String SCHEDULE_INTENT_URL = "ScheduleUrl";
    public static final String SCHEDULE_INTENT_END = "ScheduleEnd";
    public static final String SCHEDULE_INTENT_TYPE = "ScheduleType";
    public static final String SCHEDULE_INTENT_DURATION = "ScheduleDuration";

    //string values
    public static final String CAMPAIGN_EVERYDAY = "everyday";
    public static final String CAMPAIGN_SELECTED = "selected";
    public static final String DAY_SUNDAY = "Sunday";
    public static final String DAY_MONDAY = "Monday";
    public static final String DAY_TUESDAY = "Tuesday";
    public static final String DAY_WEDNESDAY = "Wednesday";
    public static final String DAY_THURSDAY = "Thursday";
    public static final String DAY_FRIDAY = "Friday";
    public static final String DAY_SATURDAY = "Saturday";
    public static final String SCHEDULE_SCOPE_DAILY = "Daily";
    public static final String SCHEDULE_SCOPE_WEEKLY = "Weekly";
    public static final String SCHEDULE_TYPE_NORMAL = "Normal Schedule";
    public static final String SCHEDULE_TYPE_SPECIFIC = "Specific Schedule";

    //string intent keys
    public static final String EXIT_APP_INTENT = "ExitApp";
    public static final String WEB_PAGE_INTENT = "WebPageUrl";
    public static final String WEB_PAGE_TYPE = "WebPageType";
    public static final String WEB_PAGE_MODE = "WebPageMode";
    public static final String LICENCED_DB_INTENT = "LicencedDbType";
    public static final String SYNC_TYPE_INTENT = "SyncTypeIntent";

    //webpage mode string values
    public static final String PAGE_TEST_MODE = "Test Mode";
    public static final String PAGE_NORMAL_MODE = "Normal Mode";
    public static final String PAGE_NO_SYNC_MODE = "No-Sync Mode";

    //shared preference keys
    public static final String PAGE_TIMEOUT_VALUE = "PageTimeout";
    public static final String PAGE_RESYNC_VALUE = "PageResync";
    public static final String SPLASH_TYPE = "SplashType";

    //splash type strings
    public static final String SPLASH_TYPE_IMAGE = "image";
    public static final String SPLASH_TYPE_VIDEO = "video";
    public static final String SPLASH_IMAGE_PORTRAIT = "PortraitSplash.png";
    public static final String SPLASH_IMAGE_LANDSCAPE = "LandscapeSplash.png";
    public static final String SPLASH_VIDEO = "Splash.mp4";

    //file directories
    public static final String BASE_FOLDER_NAME = "CloudAppSync";
    public static final String CRASH_FOLDER_NAME = "Crash Reports";
    public static final String SPLASH_FOLDER_NAME = "Splash";
    public static final String LICENCE_FOLDER_DOWNLOAD_NAME = "LicencedDownload";
    public static final String LICENCED_FOLDER_NAME = "Licenced Files";
    public static final String USER_CONFIG_FOLDER = "Config";
    public static final String USER_WEBPAGE_FOLDER = "Application";
    public static final String USER_SCHEDULE_FOLDER = "Schedules";

    //priority string values
    public static final String PRIORITY_NORMAL = "Normal";
    public static final String PRIORITY_HIGH = "High";

    //password setting string values
    public static final String PASSWORD_REQUEST_TYPE = "PasswordRequestType";
    public static final String PASSWORD_STATUS = "PasswordStatus";

    //password key string values
    public static final String PASSWORD_REQUEST_ONCE = "Once";
    public static final String PASSWORD_REQUEST_ALWAYS = "Always";
    public static final String PASSWORD_PROVIDED = "true";
    public static final String PASSWORD_NOT_PROVIDED = "false";

    //notification
    public static final String DEFAULT_NOTIFICATION_CHANNEL = "com.example.moore.DEFAULT_NOTIFICATION";

    //ftp
    public static final String FTP_LOGIN_STATUS = "FTPStatus";
    public static final String FTP_HOST = "FTPHostName";
    public static final String FTP_PORT = "FTPPort";
    public static final String FTP_USERNAME = "FTPUsername";
    public static final String FTP_PASSWORD = "FTPPassword";
    public static final String FTP_PREVIOUS_TIME = "FTPPreviousTime";

    //app sync
    public static final String APP_SYNC_INTERVAL = "AppSychInterval";
    public static final String APP_SYNC_TYPE = "AppSyncType";
    public static final String APP_SYNC_CHANGE = "AppSyncChange";
    public static final String APP_SYNC_DOWNLOAD = "AppSyncDownload";

    //broadcast register
    public static final String START_REDIRECT = "START_REDIRECT";

    //custom url visit
    public static final String CUSTOM_VISIT_URL = "CustomVisitUrl";
    public static final String CUSTOM_VISIT_IMAGE = "CustomVisitImage";

    //online indicator settings string values
    public static final String ONLINE_INDICATOR_STATE = "OnlineIndicatorStatus";
    public static final String ONLINE_INDICATOR_SHOW = "ShowIndicator";
    public static final String ONLINE_INDICATOR_HIDE = "HideIndicator";

    //schedule pickup location
    public static final String SCHEDULE_LOCATION_CHOICE = "ScheduleLocationChoice";
    public static final String SCHEDULE_LOCATION_ONLINE = "ScheduleLocationOnline";
    public static final String SCHEDULE_LOCATION_LOCAL = "ScheduleLocationLocal";
    public static final String ONLINE_SCHEDULE_FILE = "/onlineSchedules.csv";
    public static final String LOCAL_SCHEDULE_FILE = "/localSchedules.csv";

    //custom background
    public static final String CUSTOM_APP_BACKGROUND = "CustomAppBackground";

    //sync types
    public static final String CURRENT_SYNC_TYPE = "CurrentSyncType";
    public static final String SYNC_TYPE_FTP_FOLDER = "FTP Folder";
    public static final String SYNC_TYPE_FTP_ZIP = "FTP Zip";
    public static final String SYNC_TYPE_URL_ZIP = "URL Zip";
    public static final String SYNC_TYPE_API = "Api";
    public static final String SYNC_TYPE_INDEX = "Index Change";
    public static final String SYNC_TYPE_PARSE = "Parsing";

    //test mode toggle
    public static final String CURRENT_TEST_MODE = "CurrentTestMode";
    public static final String TEST_ONE_OFF = "One-Off";
    public static final String TEST_PERSISTENT = "Persistent";
    public static final String IS_IN_TEST_MODE = "TestModeStatus";
    public static final String YES = "Yes";
    public static final String NO = "No";

    //start mode
    public static final String CURRENT_START_MODE = "CurrentStartMode";
    public static final String START_MODE_BOOT = "Start at Boot";
    public static final String START_MODE_NORMAL = "Start Normally";

    //demo values
    public static final String DEMO_COMPANY = "Demo";
    public static final String DEMO_LICENCE = "LDD_ZD2_20190621";
    public static final String DEMO_USERNAME = "Appmaster123456";
    public static final String DEMO_PASSWORD = "Appmaster123456";

    //change background
    public static final String APP_BACKGROUND = "CustomAppBackground";

    //file status
    public static final String FILE_STATUS_DOWNLOADED = "Downloaded";
    public static final String FILE_STATUS_PENDING = "Downloading";

    //user agent
    public static final String USER_AGENT_MOBILE = "Mobile";
    public static final String USER_AGENT_DESKTOP = "Desktop";
    public static final String CURRENT_USER_AGENT = "UserAgent";

    //custom link
    public static final String CUSTOM_ONLINE_LINK_STATUS = "CustomLinkStatus";
    public static final String CUSTOM_ONLINE_LINK = "CustomLink";
    public static final String CUSTOM_ONLINE_LINK_ACTIVE = "Link Active";
    public static final String CUSTOM_ONLINE_LINK_INACTIVE = "Link Inactive";

    //refresh toggle
    public static final String PAGE_REFRESH_STATUS = "RefreshStatus";
    public static final String PAGE_REFRESH_ACTIVE = "Refresh Active";
    public static final String PAGE_REFRESH_INACTIVE = "Refresh Inactive";

    //user verifications
    public static final String CURRENT_USER_TYPE = "UserType";
    public static final String USER_TYPE_BASIC = "Basic";
    public static final String USER_TYPE_ULTRA = "Ultra";
}
