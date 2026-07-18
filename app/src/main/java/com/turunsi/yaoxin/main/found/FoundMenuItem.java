package com.turunsi.yaoxin.main.found;

public class FoundMenuItem {

    public static final int TYPE_MENU = 0;
    public static final int TYPE_GAP = 1;

    public static final int ACTION_DEFAULT = 0;
    public static final int ACTION_SCAN = 1;
    public static final int ACTION_ADD_FRIEND = 2;

    public final int type;
    public final String title;
    public final int iconRes;
    public final int action;

    private FoundMenuItem(int type, String title, int iconRes, int action) {
        this.type = type;
        this.title = title;
        this.iconRes = iconRes;
        this.action = action;
    }

    public static FoundMenuItem menu(String title, int iconRes) {
        return new FoundMenuItem(TYPE_MENU, title, iconRes, ACTION_DEFAULT);
    }

    public static FoundMenuItem menu(String title, int iconRes, int action) {
        return new FoundMenuItem(TYPE_MENU, title, iconRes, action);
    }

    public static FoundMenuItem gap() {
        return new FoundMenuItem(TYPE_GAP, null, 0, ACTION_DEFAULT);
    }
}
