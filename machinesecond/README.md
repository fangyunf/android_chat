# MachineSecond Android SDK

红包助手能力封装为 **Android Library（AAR）**。  
宿主 App **只实现 `HostBridge` + 调用本 SDK**，不要把引擎/UI 拷进业务工程。

## 工程形态

```
QianBaoSdk/
├── machinesecond/   ← SDK（打 AAR）
└── app/             ← Sample 宿主（演示如何调用）
```

## 宿主接入（3 步）

### 1. 依赖

本地模块：

```kotlin
implementation(project(":machinesecond"))
```

或发布后的 Maven / AAR：

```kotlin
implementation("com.machinesecond:sdk:1.0.0")
// 或
implementation(files("libs/machinesecond-release.aar"))
```

打本地 Maven：

```bash
./gradlew :machinesecond:publishReleasePublicationToLocalAarRepository
# 产物：build/maven-repo/com/machinesecond/sdk/1.0.0/
```

打 AAR：

```bash
./gradlew :machinesecond:assembleRelease
# 产物：machinesecond/build/outputs/aar/machinesecond-release.aar（已 R8 混淆）
# mapping：machinesecond/mapping/machinesecond-release-mapping.txt（崩溃还原用，勿随 AAR 外发）
```

## 混淆说明

- **release AAR 默认开启 R8**：内部引擎/实现混淆，公开 API 保留。
- **字典命名（更难读）**：
  - `dictionary-member.txt` / `dictionary-class.txt` / `dictionary-package.txt`
  - 规则已启用 `-obfuscationdictionary` / `-classobfuscationdictionary` / `-packageobfuscationdictionary`
  - 反编译可见类似 `zxq_Async_Helper_Ex_ProxyKeeper`、`O0lIl0O1_…` 的长名
- **保留**：`MachineSecond`、`HostBridge`/`RedInbound`/`FanUser`、Manifest Activity、关键 Gson 模型。
- **宿主 App 开 minify 时**：AAR 内已带 `consumer-rules.pro`，会自动合并。
- 规则文件：`proguard-rules.pro`（库自身）、`consumer-rules.pro`（给宿主）。
- mapping：`machinesecond/mapping/machinesecond-release-mapping.txt`（崩溃还原，勿随 AAR 外发）

### 2. 实现 HostBridge

宿主提供：当前用户、`POST /red/*`、解析红包消息、群成员、加好友、群发等。  
接口：`com.machinesecond.host.HostBridge`

### 3. 调用公开 API

```kotlin
import com.machinesecond.api.MachineSecond
import com.machinesecond.host.HostBridge

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MachineSecond.init(this, MyHostBridge())
    }
}

// 进群聊
MachineSecond.setActiveGroupId(groupId, true)
MachineSecond.installChatFloating(overlayParent, groupId)

// 收 IM
MachineSecond.onReceiveMessages(rawList, sessionId)

// 离群（只停发包定时器）
MachineSecond.setActiveGroupId(null, false)
MachineSecond.uninstallChatFloating()

// 打开设置
MachineSecond.presentSettings(activity)

// 登出
MachineSecond.stopAll()
```

## 公开面（宿主应依赖）

| 类型 | 说明 |
|------|------|
| `com.machinesecond.api.MachineSecond` | 对外入口 |
| `com.machinesecond.host.HostBridge` | 宿主必须实现 |
| `com.machinesecond.host.RedInbound` / `FanUser` | 消息/成员模型 |

引擎、设置页内部实现属 SDK 私有细节，宿主无需直接调用。

## 与 iOS 对应

| iOS | Android |
|-----|---------|
| `MachineSecond` / `MSManager` | `MachineSecond` / `MsSdk` |
| Network / Message Provider | `HostBridge` |
