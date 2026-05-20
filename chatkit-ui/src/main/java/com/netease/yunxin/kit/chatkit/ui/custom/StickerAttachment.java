// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.custom;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import org.json.JSONObject;

public class StickerAttachment extends CustomAttachment {

  private static final String KEY_CATALOG = "catalog";
  private static final String KEY_CHART_LET = "chartlet";
  private static final String KEY_URL = "url";

  private String catalog;
  private String chartLet;
  private String url;

  public StickerAttachment() {
    super(ChatMessageType.CUSTOM_STICKER);
  }

  public StickerAttachment(String catalog, String emotion) {
    this();
    this.catalog = catalog;
    this.chartLet = FileUtils.getFileNameNoExtension(emotion);
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  @Override
  protected void parseData(JSONObject data) {
    try {
      this.catalog = data.optString(KEY_CATALOG, "");
      this.chartLet = data.optString(KEY_CHART_LET, "");
      this.url = data.optString(KEY_URL, "");
    } catch (Exception exception) {

    }
  }

  @Override
  protected JSONObject packData() {
    JSONObject data = new JSONObject();
    try {
      data.put(KEY_CATALOG, catalog);
      data.put(KEY_CHART_LET, chartLet);
      if (url != null && !url.isEmpty()) {
        data.put(KEY_URL, url);
      }
    } catch (Exception exception) {

    }

    return data;
  }

  @Nullable
  @Override
  public String getContent() {
    return "[表情]";
  }

  public String getCatalog() {
    return catalog;
  }

  public String getChartLet() {
    return chartLet;
  }
}
