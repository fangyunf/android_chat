// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.viewpager2.widget.ViewPager2;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.WatchImageAdapter;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.yaoxin.appbase.utils.ImageUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Watch picture page */
public class WatchImageActivity extends WatchBaseActivity {
  private static final String TAG = "WatchImageActivity";

  public static final String EXT_MESSAGE_LIST_KEY = "EXT_MESSAGE_LIST_KEY";
  public static final String EXT_FIRST_DISPLAY_INDEX_KEY = "EXT_FIRST_DISPLAY_INDEX_KEY";

  private ViewPager2 viewPager2;
  private WatchImageAdapter watchImageAdapter;

  private List<IMMessage> messages;
  private int firstDisplayImageIndex = 0;
  private boolean newPageSelected = false;
  private String pendingSavePath;

  public static void launch(Context context, ArrayList<IMMessage> list, int showIndex) {
    Intent intent = new Intent(context, WatchImageActivity.class);
    intent.putExtra(EXT_MESSAGE_LIST_KEY, list);
    intent.putExtra(EXT_FIRST_DISPLAY_INDEX_KEY, showIndex);
    context.startActivity(intent);
  }

  @Override
  public void initData(Intent intent) {
    if (intent != null) {
      messages = (List<IMMessage>) intent.getSerializableExtra(EXT_MESSAGE_LIST_KEY);
      if (messages == null || messages.size() < 1) {
        finish();
        return;
      }
      firstDisplayImageIndex = intent.getIntExtra(EXT_FIRST_DISPLAY_INDEX_KEY, messages.size() - 1);
      ALog.d(
          LIB_TAG,
          TAG,
          "initData message size: " + messages.size() + " firstIndex:" + firstDisplayImageIndex);
    } else {
      finish();
    }
  }

  @Override
  public void initDataObserver() {
    super.initDataObserver();
    ALog.d(LIB_TAG, TAG, "initDataObserver");
    viewModel
        .getStatusMessageLiveData()
        .observe(
            this,
            messageStatusChangeResult -> {
              int pos = messages.indexOf(messageStatusChangeResult.getData());
              ALog.d(
                  LIB_TAG,
                  TAG,
                  "message livedata observe -->> pos:"
                      + pos
                      + " "
                      + messageStatusChangeResult.getLoadStatus());
              if (pos >= 0) {
                watchImageAdapter.notifyItemChanged(pos, messageStatusChangeResult.getLoadStatus());
              }
            });
  }

  @Override
  public void initView() {
    super.initView();
    watchImageAdapter = new WatchImageAdapter(this, messages);
    viewPager2.setAdapter(watchImageAdapter);
    viewPager2.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {

          @Override
          public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (positionOffset == 0f && newPageSelected) {
              newPageSelected = false;
              viewModel.requestFile(messages.get(position));
            }
          }

          @Override
          public void onPageSelected(int position) {
            newPageSelected = true;
          }
        });
    viewPager2.setCurrentItem(firstDisplayImageIndex, false);
  }

  @Override
  public View initMediaView() {
    viewPager2 = new ViewPager2(this);
    viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
    return viewPager2;
  }

  @Override
  public void saveMedia() {
    int position = viewPager2.getCurrentItem();
    ALog.d(LIB_TAG, TAG, "save image -->> currentItem:" + position);
    if (position < 0 || position >= messages.size()) {
      return;
    }
    String path = resolveLocalImagePath(messages.get(position));
    if (TextUtils.isEmpty(path)) {
      ToastX.showShortToast(R.string.chat_message_image_save_fail);
      return;
    }
    if (path.startsWith("http://") || path.startsWith("https://")) {
      saveImageFromUrl(path);
      return;
    }
    saveLocalImageFile(path);
  }

  private String resolveLocalImagePath(IMMessage message) {
    if (message == null || !(message.getAttachment() instanceof ImageAttachment)) {
      return null;
    }
    ImageAttachment attachment = (ImageAttachment) message.getAttachment();
    if (!TextUtils.isEmpty(attachment.getPath())) {
      return attachment.getPath();
    }
    if (!TextUtils.isEmpty(attachment.getThumbPath())) {
      return attachment.getThumbPath();
    }
    return attachment.getUrl();
  }

  private void saveLocalImageFile(String path) {
    pendingSavePath = path;
    String[] permissions = ImageUtil.getImageSavePermissions();
    if (PermissionUtils.hasPermissions(this, permissions)) {
      performSave(path);
      pendingSavePath = null;
      return;
    }
    Permission.requirePermissions(this, permissions)
        .request(
            new Permission.PermissionCallback() {
              @Override
              public void onGranted(List<String> permissionsGranted) {
                if (!TextUtils.isEmpty(pendingSavePath)) {
                  performSave(pendingSavePath);
                  pendingSavePath = null;
                }
              }

              @Override
              public void onDenial(
                  List<String> permissionsDenial, List<String> permissionDenialForever) {
                pendingSavePath = null;
                showPermissionDeniedToast();
              }

              @Override
              public void onException(Exception exception) {
                pendingSavePath = null;
                showPermissionDeniedToast();
              }
            });
  }

  private void saveImageFromUrl(String url) {
    File cacheFile =
        new File(getCacheDir(), "watch_save_" + System.currentTimeMillis() + ".jpg");
    new Thread(
            () -> {
              ImageUtil.downloadImage(url, cacheFile);
              runOnUiThread(
                  () -> {
                    if (cacheFile.exists() && cacheFile.length() > 0) {
                      saveLocalImageFile(cacheFile.getAbsolutePath());
                    } else {
                      ToastX.showShortToast(R.string.chat_message_image_save_fail);
                    }
                  });
            })
        .start();
  }

  private void performSave(String path) {
    if (ImageUtil.saveImageFileToGallery(this, path)) {
      ToastX.showShortToast(R.string.chat_message_image_save);
    } else {
      ToastX.showShortToast(R.string.chat_message_image_save_fail);
    }
  }

  private void showPermissionDeniedToast() {
    Toast.makeText(this, getResources().getString(R.string.permission_default), Toast.LENGTH_SHORT)
        .show();
  }
}
