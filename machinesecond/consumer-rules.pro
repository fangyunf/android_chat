# =============================================================================
# MachineSecond SDK — 宿主 App 混淆时自动合并（consumerProguardFiles）
# 当宿主开启 minify，保证 SDK 公开面与组件不被二次破坏
# =============================================================================

# 公开入口
-keep public class com.machinesecond.api.MachineSecond { public *; }
-keep public class com.machinesecond.MsSdk { public *; }

# 宿主必须实现 / 使用的模型
-keep public interface com.machinesecond.host.HostBridge { *; }
-keep public class com.machinesecond.host.RedInbound { *; }
-keep public class com.machinesecond.host.FanUser { *; }

# Manifest 注册的 Activity / Fragment
-keep class com.machinesecond.ui.**Activity { *; }
-keep class com.machinesecond.ui.**Fragment { *; }
-keep class com.machinesecond.ui.**Fragment$* { *; }

# View 自定义控件
-keep class com.machinesecond.ui.floating.** { *; }
-keep class com.machinesecond.ui.mass.AddressBookMassEntry { *; }

# Gson
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.machinesecond.util.GrabLogStore$Entry { *; }

# 枚举
-keepclassmembers enum com.machinesecond.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 安全存储
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-dontwarn com.google.crypto.tink.**

# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
