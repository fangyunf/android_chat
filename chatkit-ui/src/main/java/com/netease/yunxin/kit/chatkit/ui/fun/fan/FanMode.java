package com.netease.yunxin.kit.chatkit.ui.fun.fan;

public enum FanMode {
    Normal,
    Precise,
    Custom;

    String storageKey() {
        switch (this) {
            case Precise:
                return "Fan_precise";
            case Custom:
                return "Fan_custom";
            case Normal:
            default:
                return "Fan_normal";
        }
    }
}
