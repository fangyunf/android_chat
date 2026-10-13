package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

/**
 * 本产品授权配置（对齐 qiangbao wanxunda 适配）。
 * 换产品时改 [LICENSE_SAFE_CODE] / [LICENSE_BASE_URL] 即可。
 */
object AuthProduct {
    /** 万迅达 safecode */
    const val LICENSE_SAFE_CODE = "E7ECDD8E8CD35859"
    const val LICENSE_BASE_URL = "https://ios99.vip"
    const val LICENSE_FALLBACK_BASE = "http://8.136.47.17"

    /** Debug 包可开本地免激活；正式包保持 false */
    const val LOCAL_DEBUG_AUTH = false
}
