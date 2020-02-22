package me.izhong.jobs.admin.config;

public class ShopPermissions {

    public static final String MONITOR_PREFIX = "shop:";

    public static class User {
        public static final String PREFIX = MONITOR_PREFIX + ":user:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String OPERATE = PREFIX + "operate";
        public static final String ADD = PREFIX + "add";
        public static final String CODE_VIEW = PREFIX + "code:view";
        public static final String CODE_EDIT = PREFIX + "code:edit";
        public static final String LOG_VIEW = PREFIX + "log:view";
        public static final String LOG_CLEAN = PREFIX + "log:clean";
    }


}
