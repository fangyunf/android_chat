package com.yaoxin.appbase.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceUtils {
    public static final String TAG = "DeviceUtils";
    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_SMARTISAN = "SMARTISAN";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_QIKU = "QIKU";
    private static final String DEVICE_ID = "DEVICE_ID";
    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";
    private static String sName;
    private static String sVersion;

    public static boolean isXiaomiMobilePhone() {
        return "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isVivoMobilePhone() {
        return "vivo".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isHuaweiMobilePhone() {
        return "huawei".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isMeizuMobilePhone() {
        return "meizu".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isLocationNeedGPSDevice() {
        return isHuaweiMobilePhone() || isVivoMobilePhone();
    }

    // 检查手机上是否安装了指定的软件
    public static boolean isAvilible(Context context, String packageName) {
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getWindowWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getWindowHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static boolean isEmui() {
        return check(ROM_EMUI);
    }

    public static boolean isMiui() {
        return check(ROM_MIUI);
    }

    public static boolean isVivo() {
        return check(ROM_VIVO);
    }

    public static boolean isOppo() {
        return check(ROM_OPPO);
    }

    public static boolean isFlyme() {
        return check(ROM_FLYME);
    }

    public static boolean is360() {
        return check(ROM_QIKU) || check("360");
    }

    public static boolean isSmartisan() {
        return check(ROM_SMARTISAN);
    }

    public static String getName() {
        if (sName == null) {
            check("");
        }
        return sName;
    }

    public static String getVersion() {
        if (sVersion == null) {
            check("");
        }
        return sVersion;
    }

    public static boolean check(String rom) {
        if (sName != null) {
            return sName.equals(rom);
        }

        if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_EMUI))) {
            sName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_OPPO))) {
            sName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_VIVO))) {
            sName = ROM_VIVO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_SMARTISAN))) {
            sName = ROM_SMARTISAN;
        } else {
            sVersion = Build.DISPLAY;
            if (sVersion.toUpperCase().contains(ROM_FLYME)) {
                sName = ROM_FLYME;
            } else {
                sVersion = Build.UNKNOWN;
                sName = Build.MANUFACTURER.toUpperCase();
            }
        }
        return sName.equals(rom);
    }

    public static String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            //Log.e(TAG, "Unable to read prop " + name, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    public static boolean isEMUI3_1() {
        if ("EmotionUI_3.1".equals(getEmuiVersion())) {
            return true;
        }
        return false;
    }

    private static String getEmuiVersion() {
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            return (String) getMethod.invoke(classType, "ro.build.version.emui");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return "";
    }

    public static boolean isMui5() {
        boolean isMui5 = false;
        String product = android.os.Build.PRODUCT;
        if (product.equals("meizu_15_CN")) {
            isMui5 = true;
        }
        return isMui5;
    }

    /**
     * 获取设备 ANDROID_ID
     * 设备首次启动系统随机生成，恢复出厂设置 重置
     *
     * @return android id
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取设备唯一标识
     * 基于Android系统真实标识生成32位唯一标识，优先从本地存储读取
     *
     * @param context 上下文
     * @return 32位唯一标识
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        // 本地存储的key
        final String DEVICE_ID_KEY = "DEVICE_ID_KEY";

        // 先从本地存储读取
        String deviceId = com.orhanobut.hawk.Hawk.get(DEVICE_ID_KEY, "");

        // 如果本地没有存储，则生成新的基于Android系统的32位标识
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = generateAndroidBasedDeviceId(context);
            // 存储到本地
            com.orhanobut.hawk.Hawk.put(DEVICE_ID_KEY, deviceId);
        }

        return deviceId;
    }

    /**
     * 基于Android系统信息生成32位唯一标识
     * 结合硬件信息和系统信息，确保唯一性和稳定性
     *
     * @param context 上下文
     * @return 32位唯一标识
     */
    private static String generateAndroidBasedDeviceId(Context context) {
        StringBuilder deviceInfo = new StringBuilder();

        // 1. 获取ANDROID_ID（系统级唯一标识）
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(androidId)) {
            deviceInfo.append(androidId);
        }
        // 2. 添加硬件信息（确保硬件级别的唯一性）
        deviceInfo.append(Build.MANUFACTURER);  // 制造商
        deviceInfo.append(Build.MODEL);         // 设备型号
        deviceInfo.append(Build.PRODUCT);       // 产品名称
        deviceInfo.append(Build.DEVICE);        // 设备名称
        deviceInfo.append(Build.BOARD);         // 主板
        deviceInfo.append(Build.BRAND);         // 品牌

        // 3. 添加系统信息
        deviceInfo.append(Build.VERSION.SDK_INT);  // API级别
        deviceInfo.append(Build.VERSION.RELEASE);  // 系统版本

        // 4. 添加屏幕信息（设备特征）
        int screenWidth = getWindowWidth(context);
        int screenHeight = getWindowHeight(context);
        deviceInfo.append(screenWidth).append(screenHeight);

        // 5. 如果信息不足，添加UUID作为补充
        if (deviceInfo.length() < 16) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            deviceInfo.append(uuid);
        }

        // 6. 生成32位唯一标识
        return generate32BitHash(deviceInfo.toString());
    }

    /**
     * 将设备信息字符串转换为32位唯一标识
     * 使用智能哈希算法确保32位长度和唯一性，完全避免用0补齐
     *
     * @param deviceInfo 设备信息字符串
     * @return 32位唯一标识
     */
    private static String generate32BitHash(String deviceInfo) {
        if (TextUtils.isEmpty(deviceInfo)) {
            return generate32BitUniqueId();
        }

        // 使用多个哈希算法组合，生成更丰富的标识
        StringBuilder result = new StringBuilder();

        // 1. 主哈希值（基于字符串的hashCode）
        int hash1 = deviceInfo.hashCode();
        String hex1 = Integer.toHexString(Math.abs(hash1));
        result.append(hex1);

        // 2. 字符串长度哈希
        int hash2 = deviceInfo.length() * 31;
        String hex2 = Integer.toHexString(Math.abs(hash2));
        result.append(hex2);

        // 3. 字符分布哈希（基于字符的ASCII值）
        int charHash = 0;
        for (int i = 0; i < deviceInfo.length(); i++) {
            charHash = 31 * charHash + deviceInfo.charAt(i);
        }
        String hex3 = Integer.toHexString(Math.abs(charHash));
        result.append(hex3);

        // 4. 位置加权哈希（字符位置影响哈希值）
        int posHash = 0;
        for (int i = 0; i < deviceInfo.length(); i++) {
            posHash += (deviceInfo.charAt(i) * (i + 1));
        }
        String hex4 = Integer.toHexString(Math.abs(posHash));
        result.append(hex4);

        // 5. 如果还不够32位，使用字符串的奇偶位特征
        if (result.length() < 32) {
            int oddEvenHash = 0;
            for (int i = 0; i < deviceInfo.length(); i++) {
                if (i % 2 == 0) {
                    oddEvenHash += deviceInfo.charAt(i) * 2;
                } else {
                    oddEvenHash += deviceInfo.charAt(i) * 3;
                }
            }
            String hex5 = Integer.toHexString(Math.abs(oddEvenHash));
            result.append(hex5);
        }

        // 6. 如果仍然不够32位，使用字符串的字符频率
        if (result.length() < 32) {
            int freqHash = 0;
            for (int i = 0; i < deviceInfo.length(); i++) {
                freqHash += (deviceInfo.charAt(i) * (deviceInfo.length() - i));
            }
            String hex6 = Integer.toHexString(Math.abs(freqHash));
            result.append(hex6);
        }

        // 7. 如果还是不够32位，使用字符串的字符差异
        if (result.length() < 32) {
            int diffHash = 0;
            for (int i = 1; i < deviceInfo.length(); i++) {
                diffHash += Math.abs(deviceInfo.charAt(i) - deviceInfo.charAt(i - 1));
            }
            String hex7 = Integer.toHexString(Math.abs(diffHash));
            result.append(hex7);
        }

        // 确保长度为32位
        String finalResult = result.toString();
        if (finalResult.length() >= 32) {
            return finalResult.substring(0, 32);
        } else {
            // 如果仍然不足32位，使用UUID补充（这种情况很少见）
            String uuid = UUID.randomUUID().toString().replace("-", "");
            return (finalResult + uuid).substring(0, 32);
        }
    }

    /**
     * 生成32位唯一标识
     *
     * @return 32位唯一标识
     */
    private static String generate32BitUniqueId() {
        // 使用UUID生成唯一标识，然后转换为32位
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 如果UUID不足32位，用0补齐；如果超过32位，截取前32位
        return ensure32BitLength(uuid);
    }

    /**
     * 确保字符串长度为32位
     *
     * @param input 输入字符串
     * @return 32位字符串
     */
    private static String ensure32BitLength(String input) {
        if (input == null) {
            return generate32BitUniqueId();
        }

        if (input.length() == 32) {
            return input;
        } else if (input.length() > 32) {
            return input.substring(0, 32);
        } else {
            // 不足32位，用0补齐
            StringBuilder sb = new StringBuilder(input);
            while (sb.length() < 32) {
                sb.append("0");
            }
            return sb.toString();
        }
    }

    public static String getDeviceName() {
        // 获取手机品牌和型号
        String brand = Build.BRAND;        // 品牌，如：Xiaomi、Samsung、Huawei
        String model = Build.MODEL;        // 型号，如：Mi 10、SM-G975F、P40 Pro
        String manufacturer = Build.MANUFACTURER; // 制造商，如：Xiaomi、samsung、HUAWEI
        return brand + " " + model;
    }
}
