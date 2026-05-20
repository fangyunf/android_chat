// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StickerManager {
  private static StickerManager instance;

  private final List<StickerCategory> stickerCategories = new ArrayList<>();
  private final Map<String, StickerCategory> stickerCategoryMap = new HashMap<>();

  public static StickerManager getInstance() {
    if (instance == null) {
      instance = new StickerManager();
    }
    return instance;
  }

  public StickerManager() {
    reloadCategories();
  }

  public void reloadCategories() {
    stickerCategories.clear();
    stickerCategoryMap.clear();
    if (EmojiManager.getContext() != null) {
      CustomStickerStore.getInstance().init(EmojiManager.getContext());
      StickerCategory custom =
          new StickerCategory(CustomStickerStore.CATALOG, "我的表情", false, 1);
      custom.reloadStickerData();
      stickerCategories.add(custom);
      stickerCategoryMap.put(custom.getName(), custom);
    }
  }

  public synchronized List<StickerCategory> getCategories() {
    return stickerCategories;
  }

  public synchronized StickerCategory getCategory(String name) {
    return stickerCategoryMap.get(name);
  }

  public String getStickerUri(String categoryName, String stickerName) {
    if (CustomStickerStore.CATALOG.equals(categoryName)) {
      return CustomStickerStore.getInstance().getLocalPath(stickerName);
    }
    return null;
  }

  /** 远程 URL（发送方上传后写入 attachment） */
  public static String resolveDisplayUri(String catalog, String chartlet, String remoteUrl) {
    if (!TextUtils.isEmpty(remoteUrl)) {
      return remoteUrl;
    }
    return getInstance().getStickerUri(catalog, chartlet);
  }
}
