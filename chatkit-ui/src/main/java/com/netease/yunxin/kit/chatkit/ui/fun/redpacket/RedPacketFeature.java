package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

/**
 * 内置「秒」能力开关。
 * {@code false} 时不挂载群聊悬浮球、成员爆粉浮动条等 UI，自动发抢引擎也不初始化。
 */
public final class RedPacketFeature {

    /** 改这里控制是否启用内置秒 / 浮动相关功能 */
    public static final boolean ENABLE = true;

    private RedPacketFeature() {}
}
