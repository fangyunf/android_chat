package com.turunsi.yaoxin.main.mine.collection;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.CollectInfoPage;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.ui.page.WatchImageActivity;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMineCollectionListBinding;
import com.turunsi.yaoxin.databinding.ViewCollectionTabItemBinding;
import com.turunsi.yaoxin.main.mine.collection.adapter.CollectionListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class CollectionListActivity extends BaseActivity implements View.OnClickListener {

    private static final int TAB_COLOR_SELECTED = Color.parseColor("#3B5CC9");
    private static final int TAB_COLOR_NORMAL = Color.parseColor("#999999");

    ActivityMineCollectionListBinding binding;
    CollectionListAdapter adapter;
    private final List<CollectInfo> allCollectList = new ArrayList<>();
    private final List<CollectInfo> displayList = new ArrayList<>();
    private String currentTab = CollectionListHelper.TAB_ALL;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMineCollectionListBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.activityMineCollectionListNav.addCloseImageButton().setOnClickListener(this);

    binding.activityMineCollectionListRv.setLayoutManager(new LinearLayoutManager(this));
    adapter = new CollectionListAdapter();
    binding.activityMineCollectionListRv.setAdapter(adapter);

    initTabs();
    initListListeners();
    loadCollectList();
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadCollectList();
  }

  private void initTabs() {
    setupTab(
        binding.activityMineCollectionTabAll,
        "全部",
        CollectionListHelper.TAB_ALL);
    setupTab(
        binding.activityMineCollectionTabText,
        "文字",
        CollectionListHelper.TAB_TEXT);
    setupTab(
        binding.activityMineCollectionTabImage,
        "图片",
        CollectionListHelper.TAB_IMAGE);
    setupTab(
        binding.activityMineCollectionTabVideo,
        "视频",
        CollectionListHelper.TAB_VIDEO);
    setupTab(
        binding.activityMineCollectionTabFile,
        "文件",
        CollectionListHelper.TAB_FILE);
    updateTabUi();
  }

  private void setupTab(ViewCollectionTabItemBinding tabBinding, String title, String tabKey) {
    if (tabBinding == null) {
      return;
    }
    tabBinding.collectionTabTitleTv.setText(title);
    tabBinding.getRoot()
        .setOnClickListener(
            v -> {
              currentTab = tabKey;
              updateTabUi();
              applyTabFilter();
            });
  }

  private void updateTabUi() {
    updateOneTab(binding.activityMineCollectionTabAll, CollectionListHelper.TAB_ALL);
    updateOneTab(binding.activityMineCollectionTabText, CollectionListHelper.TAB_TEXT);
    updateOneTab(binding.activityMineCollectionTabImage, CollectionListHelper.TAB_IMAGE);
    updateOneTab(binding.activityMineCollectionTabVideo, CollectionListHelper.TAB_VIDEO);
    updateOneTab(binding.activityMineCollectionTabFile, CollectionListHelper.TAB_FILE);
  }

  private void updateOneTab(ViewCollectionTabItemBinding tabBinding, String tabKey) {
    if (tabBinding == null) {
      return;
    }
    boolean selected = tabKey.equals(currentTab);
    tabBinding.collectionTabTitleTv.setTextColor(
        selected ? TAB_COLOR_SELECTED : TAB_COLOR_NORMAL);
    tabBinding.collectionTabIndicatorV.setVisibility(
        selected ? View.VISIBLE : View.INVISIBLE);
  }

  private void initListListeners() {
    adapter.setOnItemClickListener(
        new BaseQuickAdapter.OnItemClickListener<CollectInfo>() {
          @Override
          public void onClick(
              @NonNull BaseQuickAdapter<CollectInfo, ?> baseQuickAdapter,
              @NonNull View view,
              int i) {
            CollectInfo item = adapter.getItem(i);
            if (item == null) {
              return;
            }
            String contentType = CollectionListHelper.getContentType(item);
            String content = CollectionListHelper.getDisplayData(item);
            if (content == null) {
              content = "";
            }
            if (CollectionListHelper.CONTENT_IMAGE.equals(contentType)) {
              if (isPickerMode()) {
                returnPickerResult(content, 1);
              } else {
                previewCollectImages(item);
              }
              return;
            }
            if (!isPickerMode()) {
              return;
            }
            returnPickerResult(content, 0);
          }
        });
    adapter.setOnItemLongClickListener(
        new BaseQuickAdapter.OnItemLongClickListener<CollectInfo>() {
          @Override
          public boolean onLongClick(
              @NonNull BaseQuickAdapter<CollectInfo, ?> baseQuickAdapter,
              @NonNull View view,
              int i) {
            DialogAlertUtil.showAlert(
                "确定删除？",
                new DialogAlertUtil.DialogAlertUtilCallBack() {
                  @Override
                  public void clickType(int type) {
                    if (type == 1) {
                      CollectInfo item = baseQuickAdapter.getItem(i);
                      if (item == null) {
                        return;
                      }
                      ArrayList<Pair<Long, Long>> list = new ArrayList<>();
                      list.add(new Pair<>(item.getId(), item.getCreateTime()));
                      NIMClient.getService(MsgService.class)
                          .removeCollect(list)
                          .setCallback(
                              new RequestCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer param) {
                                  ToastUtils.toastMsg("删除成功");
                                  loadCollectList();
                                }

                                @Override
                                public void onFailed(int code) {
                                  ToastUtils.toastMsg("删除失败");
                                }

                                @Override
                                public void onException(Throwable exception) {}
                              });
                    }
                  }
                },
                getSupportFragmentManager());
            return true;
          }
        });
  }

  private void applyTabFilter() {
    displayList.clear();
    for (CollectInfo info : allCollectList) {
      if (CollectionListHelper.matchTab(info, currentTab)) {
        displayList.add(info);
      }
    }
    adapter.setItems(displayList);
    adapter.notifyDataSetChanged();
  }

  /** 从聊天「发送收藏」进入：点击后回填并关闭 */
  private boolean isPickerMode() {
    if (extras != null && "1".equals(extras.get("pickerMode"))) {
      return true;
    }
    return getIntent() != null && getIntent().getBooleanExtra("pickerMode", false);
  }

  private void returnPickerResult(String content, int type) {
    Intent result = new Intent();
    result.putExtra("text", content);
    result.putExtra("type", type);
    setResult(RESULT_OK, result);
    finish();
  }

  /** 浏览收藏：图片点击查看大图（支持左右滑动同列表中的其他图片） */
  private void previewCollectImages(@NonNull CollectInfo clicked) {
    ArrayList<IMMessage> messages = new ArrayList<>();
    int showIndex = 0;
    for (CollectInfo info : displayList) {
      if (!CollectionListHelper.CONTENT_IMAGE.equals(CollectionListHelper.getContentType(info))) {
        continue;
      }
      String url = CollectionListHelper.getDisplayData(info);
      if (TextUtils.isEmpty(url)) {
        continue;
      }
      if (CollectionListHelper.isSameCollectItem(info, clicked)) {
        showIndex = messages.size();
      }
      messages.add(CollectionListHelper.buildImagePreviewMessage(url));
    }
    if (messages.isEmpty()) {
      return;
    }
    WatchImageActivity.launch(this, messages, showIndex);
  }

  /** BaseActivity.onCreate 里调用过早，列表尚未初始化，此处不加载 */
  @Override
  protected void _requestData() {}

  private void loadCollectList() {
    if (adapter == null) {
      return;
    }
    allCollectList.clear();
    NIMClient.getService(MsgService.class)
        .queryCollect(100)
        .setCallback(
            new RequestCallback<CollectInfoPage>() {
              @Override
              public void onSuccess(CollectInfoPage param) {
                if (adapter == null) {
                  return;
                }
                allCollectList.clear();
                if (param != null && param.getCollectList() != null) {
                  allCollectList.addAll(param.getCollectList());
                }
                applyTabFilter();
              }

              @Override
              public void onFailed(int code) {
                if (adapter != null) {
                  applyTabFilter();
                }
              }

              @Override
              public void onException(Throwable exception) {
                if (adapter != null) {
                  applyTabFilter();
                }
              }
            });
  }

  @Override
  public void onClick(View v) {
    if (v == binding.activityMineCollectionListNav.addCloseImageButton()) {
      finish();
    }
  }
}
