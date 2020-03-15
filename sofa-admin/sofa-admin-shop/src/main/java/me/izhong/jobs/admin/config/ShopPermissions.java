package me.izhong.jobs.admin.config;

public class ShopPermissions {

    public static final String PERM_PREFIX = "ext:shop:";

    public static class User {
        public static final String PREFIX = PERM_PREFIX + "user:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class GoodsCategory {
        public static final String PREFIX = PERM_PREFIX + "category:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

}
