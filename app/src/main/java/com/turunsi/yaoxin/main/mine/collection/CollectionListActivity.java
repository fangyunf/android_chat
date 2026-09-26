package com.turunsi.yaoxin.main.mine.collection;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.CollectInfoPage;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMineCollectionListBinding;
import com.turunsi.yaoxin.main.mine.collection.adapter.CollectionListAdapter;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ImageUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;

public class CollectionListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineCollectionListBinding binding;

    CollectionListAdapter adapter;

    private ActivityResultLauncher<Intent> forwardP2PLauncher;
    private ActivityResultLauncher<Intent> forwardTeamLauncher;

    /** 待转发的纯文本（收藏类型非图片） */
    private String pendingForwardText;
    /** 待转发的图片地址（收藏 type==1） */
    private String pendingImageUrl;
    private boolean pendingIsImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        forwardP2PLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() != RESULT_OK) {
                                return;
                            }
                            Intent data = result.getData();
                            if (data == null) {
                                return;
                            }
                            ArrayList<String> sessionList =
                                    data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
                            if (sessionList != null && !sessionList.isEmpty()) {
                                sendPendingToSessions(sessionList, SessionTypeEnum.P2P);
                            }
                        });
        forwardTeamLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() != RESULT_OK) {
                                return;
                            }
                            Intent data = result.getData();
                            if (data == null) {
                                return;
                            }
                            String tid = data.getStringExtra(RouterConstant.KEY_TEAM_ID);
                            if (!TextUtils.isEmpty(tid)) {
                                ArrayList<String> list = new ArrayList<>();
                                list.add(tid);
                                sendPendingToSessions(list, SessionTypeEnum.Team);
                            }
                        });

        binding = ActivityMineCollectionListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineCollectionListNav.addCloseImageButton().setOnClickListener(this);

        binding.activityMineCollectionListRv.setLayoutManager(new LinearLayoutManager(this));
         adapter = new CollectionListAdapter();
        binding.activityMineCollectionListRv.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<CollectInfo>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<CollectInfo, ?> baseQuickAdapter, @NonNull View view, int i) {
                CollectInfo item = adapter.getItem(i);
                showCollectItemActions(item);
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener<CollectInfo>() {
            @Override
            public boolean onLongClick(@NonNull BaseQuickAdapter<CollectInfo, ?> baseQuickAdapter, @NonNull View view, int i) {
                DialogAlertUtil.showAlert("确定删除？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                    @Override
                    public void clickType(int type) {
                        if (type == 1) {
                            ArrayList list = new ArrayList<>();
                            CollectInfo item = baseQuickAdapter.getItem(i);
                            Pair pair = new Pair<>(item.getId(),item.getCreateTime());
                            list.add(pair);
                            NIMClient.getService(MsgService.class).removeCollect(list).setCallback(
                                    new RequestCallback<Integer>() {
                                        @Override
                                        public void onSuccess(Integer param) {
//                                            Toast.makeText(SessionExtension.this, "批量移除收藏成功", Toast.LENGTH_SHORT).show();
                                            ToastUtils.toastMsg("删除成功");
                                            _requestData();
                                        }

                                        @Override
                                        public void onFailed(int code) {
                                            ToastUtils.toastMsg("删除失败");
                                        }

                                        @Override
                                        public void onException(Throwable exception) {
                                        }
                                    }
                            );

                        }
                    }
                }, getSupportFragmentManager());

                return false;
            }
        });
    }

    @Override
    protected void _requestData() {
        super._requestData();
        dataList.clear();
        NIMClient.getService(MsgService.class).queryCollect(100).setCallback(
                new RequestCallback<CollectInfoPage>() {
                    @Override
                    public void onSuccess(CollectInfoPage param) {
                        if (param == null) {
                            return;
                        }
                        ArrayList<CollectInfo> collectList = param.getCollectList();
                        for (CollectInfo tempInfo :
                                collectList) {

                            int type = tempInfo.getType();
                            long id = tempInfo.getId();
                            String data = tempInfo.getData();
                            String ext = tempInfo.getExt();
                            if (type == 1024 || type == 1) {
                                dataList.add(tempInfo);
                            }
                        }
                        adapter.setItems(dataList);
                        adapter.notifyDataSetChanged();
//                        addCollects(param.getCollectList());
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                }

        );
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineCollectionListNav.addCloseImageButton()) {
            finish();
        }
    }

    private void showCollectItemActions(@Nullable CollectInfo item) {
        if (item == null) {
            return;
        }
        boolean openedForPick = getCallingActivity() != null;
        if (openedForPick) {
            new AlertDialog.Builder(this)
                    .setItems(
                            new String[]{"插入当前会话", "转发给好友", "转发到群聊"},
                            (d, which) -> {
                                if (which == 0) {
                                    finishWithInsertResult(item);
                                } else if (which == 1) {
                                    startForwardP2P(item);
                                } else {
                                    startForwardTeam(item);
                                }
                            })
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setItems(
                            new String[]{"转发给好友", "转发到群聊"},
                            (d, which) -> {
                                if (which == 0) {
                                    startForwardP2P(item);
                                } else {
                                    startForwardTeam(item);
                                }
                            })
                    .show();
        }
    }

    private void finishWithInsertResult(CollectInfo item) {
        String content = item.getData();
        if (item.getType() == 1) {
            Intent result = new Intent();
            result.putExtra("text", content);
            result.putExtra("type", 1);
            setResult(RESULT_OK, result);
            finish();
            return;
        }
        try {
            content = AESUtil.msgAseDecrypt(content);
        } catch (Exception ignored) {
        }
        Intent result = new Intent();
        result.putExtra("text", content);
        result.putExtra("type", 0);
        setResult(RESULT_OK, result);
        finish();
    }

    private void fillPendingForward(CollectInfo item) {
        if (item.getType() == 1) {
            pendingIsImage = true;
            pendingImageUrl = item.getData();
            pendingForwardText = null;
        } else {
            pendingIsImage = false;
            pendingImageUrl = null;
            String content = item.getData();
            try {
                content = AESUtil.msgAseDecrypt(content);
            } catch (Exception ignored) {
            }
            pendingForwardText = content;
        }
    }

    private void startForwardP2P(CollectInfo item) {
        if (!NetworkUtils.isConnected()) {
            ToastUtils.toastMsg("网络未连接");
            return;
        }
        fillPendingForward(item);
        ChatUtils.startP2PSelector(
                this, RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE, null, forwardP2PLauncher);
    }

    private void startForwardTeam(CollectInfo item) {
        if (!NetworkUtils.isConnected()) {
            ToastUtils.toastMsg("网络未连接");
            return;
        }
        fillPendingForward(item);
        ChatUtils.startTeamList(this, RouterConstant.PATH_FUN_MY_TEAM_PAGE, forwardTeamLauncher);
    }

    private void sendPendingToSessions(ArrayList<String> sessionIds, SessionTypeEnum sessionType) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return;
        }
        if (pendingIsImage) {
            if (TextUtils.isEmpty(pendingImageUrl)) {
                return;
            }
            File dir = getExternalFilesDir(null);
            if (dir == null) {
                ToastUtils.toastMsg("发送失败");
                return;
            }
            File imageFile = new File(dir, "collect_forward_" + System.currentTimeMillis() + ".jpg");
            ImageUtil.downloadImageSync(pendingImageUrl, imageFile);
            if (!imageFile.exists() || imageFile.length() == 0) {
                ToastUtils.toastMsg("图片下载失败");
                clearPendingForward();
                return;
            }
            for (String sid : sessionIds) {
                IMMessage imageMsg = MessageBuilder.createImageMessage(sid, sessionType, imageFile);
                ChatRepo.sendMessage(imageMsg, false, null);
            }
            ToastUtils.toastMsg("已转发");
        } else {
            if (TextUtils.isEmpty(pendingForwardText)) {
                clearPendingForward();
                return;
            }
            for (String sid : sessionIds) {
                IMMessage textMsg = MessageBuilder.createTextMessage(sid, sessionType, pendingForwardText);
                ChatRepo.sendMessage(textMsg, false, null);
            }
            ToastUtils.toastMsg("已转发");
        }
        clearPendingForward();
    }

    private void clearPendingForward() {
        pendingForwardText = null;
        pendingImageUrl = null;
        pendingIsImage = false;
    }

}
