# =============================================================================
# MachineSecond SDK — 库自身 R8 混淆规则（release 打 AAR 时生效）
# 目标：混淆内部引擎/实现，保留宿主可见 API 与 Manifest 组件
# =============================================================================

# --- 通用 ---
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose
-allowaccessmodification
-repackageclasses 'ms.i'

# --- 混淆字典：反编译显示为长词/难读名（相对本文件目录） ---
-obfuscationdictionary dictionary-member.txt
-classobfuscationdictionary dictionary-class.txt
-packageobfuscationdictionary dictionary-package.txt


# 保留行号便于崩溃还原（配合 mapping.txt）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*,Exceptions

# --- 对外公开 API（宿主依赖，禁止混淆） ---
-keep public class com.machinesecond.api.MachineSecond {
    public static <fields>;
    public static <methods>;
}
-keep public interface com.machinesecond.host.HostBridge { *; }
-keep public class com.machinesecond.host.RedInbound { *; }
-keep public class com.machinesecond.host.FanUser { *; }

# 兼容旧调用：MsSdk 若被宿主直接引用则保留公开方法
-keep public class com.machinesecond.MsSdk {
    public <methods>;
}

# --- Manifest / 组件（类名不可改） ---
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.fragment.app.Fragment {
    public <init>(...);
}

# View 构造（浮动层等）
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# --- Gson / 反射持久化模型 ---
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.machinesecond.util.GrabLogStore$Entry { *; }
-keep class com.machinesecond.util.DiagLogStore$** { *; }
-keepclassmembers class com.machinesecond.config.MsConfig {
    <fields>;
}

# 枚举 name()/valueOf
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Kotlin ---
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$DefaultImpls { *; }
-keepclassmembers class **$Companion {
    public <methods>;
    public <fields>;
}

# --- 依赖库告警压制（SDK 内用到） ---
-dontwarn androidx.security.**
-dontwarn com.google.crypto.tink.**
-dontwarn org.bouncycastle.**
-dontwarn javax.annotation.**

# EncryptedSharedPreferences / MasterKey
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# --- Parcelable / Serializable ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# R8 全量模式：不要删掉被反射/字符串间接引用的路径常量所在类
-keep class com.machinesecond.net.RedApi { *; }

# 标题解析与配置是核心业务，保留类名便于诊断日志对照（字段仍可被优化）
-keep class com.machinesecond.title.MineTitleParser { *; }
-keep class com.machinesecond.config.MsConfig { public <methods>; }
