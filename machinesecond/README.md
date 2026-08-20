# MachineSecond Android SDK

红包助手（秒抢 / 发包 / 赔付 / 爆粉 / 群发）封装为 **Android Library**。  
本仓库版本 **无授权校验**，打开总开关即可用。

宿主 App **只实现 `HostBridge` + 调用本 SDK**。

## 宿主接入

### 1. 依赖

```kotlin
implementation(project(":machinesecond"))
```

### 2. 实现 HostBridge

宿主提供：当前用户、`POST /red/*`、解析红包消息、群成员、加好友、群发等。  
接口：`com.machinesecond.host.HostBridge`

### 3. 调用公开 API

```kotlin
MachineSecond.init(this, MyHostBridge())
MachineSecond.setActiveGroupId(groupId, true)
MachineSecond.installChatFloating(overlayParent, groupId)
MachineSecond.onReceiveMessages(list, sessionId)
MachineSecond.presentSettings(activity)
MachineSecond.stopAll() // 登出
```

xinda 工程已接好：`MsHostBridge` + `MsChatHook`（聊天浮钮 / 收消息），设置页入口「红包助手」。

## 公开面

| 类型 | 说明 |
|------|------|
| `com.machinesecond.api.MachineSecond` | 对外入口 |
| `com.machinesecond.host.HostBridge` | 宿主必须实现 |
| `com.machinesecond.host.RedInbound` / `FanUser` | 消息/成员模型 |
