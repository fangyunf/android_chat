package com.turunsi.yaoxin.main.found;

public class FoundMenuItem {

    public static final int TYPE_MENU = 0;
    public static final int TYPE_GAP = 1;

    public final int type;
    public final String title;
    public final int iconRes;

    private FoundMenuItem(int type, String title, int iconRes) {
        this.type = type;
        this.title = title;
        this.iconRes = iconRes;
    }

    public static FoundMenuItem menu(String title, int iconRes) {
        return new FoundMenuItem(TYPE_MENU, title, iconRes);
    }

    public static FoundMenuItem gap() {
        return new FoundMenuItem(TYPE_GAP, null, 0);
    }
}
