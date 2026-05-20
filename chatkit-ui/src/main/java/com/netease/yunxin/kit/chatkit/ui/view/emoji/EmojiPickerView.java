// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatEmojiLayoutBinding;

/** emoji picker view */
public class EmojiPickerView extends LinearLayout implements IEmojiCategoryChanged {

  public interface OnCustomStickerActionListener {
    void onRequestAddCustomSticker();

    void onCustomStickerDelete(String stickerId);
  }

  private static final int MODE_EMOJI = 0;
  private static final int MODE_STICKER = 1;

  private Context context;
  private ChatEmojiLayoutBinding viewBinding;

  private IEmojiSelectedListener listener;
  private OnCustomStickerActionListener customStickerActionListener;

  private boolean loaded = false;
  private boolean withSticker;
  private EmojiView gifView;
  private int segmentMode = MODE_EMOJI;
  private Handler uiHandler;
  private CustomFavoriteStickerAdapter customStickerAdapter;

  public EmojiPickerView(Context context) {
    super(context);
    init(context);
  }

  public EmojiPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public EmojiPickerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    this.uiHandler = new Handler(context.getMainLooper());
    LayoutInflater.from(context).inflate(R.layout.chat_emoji_layout, this, true);
    viewBinding = ChatEmojiLayoutBinding.bind(this);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    setupEmojiView();
  }

  public void show(IEmojiSelectedListener listener) {
    setListener(listener);
    if (!loaded) {
      prepareStickerData();
      loaded = true;
    }
    show();
  }

  public void setListener(IEmojiSelectedListener listener) {
    if (listener != null) {
      this.listener = listener;
    } else {
      ALog.d(LIB_TAG, "sticker", "listener is null");
    }
  }

  public void setOnCustomStickerActionListener(OnCustomStickerActionListener listener) {
    this.customStickerActionListener = listener;
  }

  public void reloadCustomStickers() {
    CustomStickerStore.getInstance().init(context);
    StickerManager.getInstance().reloadCategories();
    if (customStickerAdapter != null) {
      customStickerAdapter.refresh();
    }
    if (segmentMode == MODE_STICKER) {
      showCustomStickerGrid();
    } else {
      selectSegmentMode(MODE_STICKER);
    }
  }

  protected void setupEmojiView() {
    viewBinding.topDividerLine.setVisibility(View.VISIBLE);
    viewBinding.emojiSendTv.setOnClickListener(
        view -> {
          if (listener != null) {
            listener.onEmojiSendClick();
          }
        });
    setupSegmentBar();
    setupCustomStickerGrid();
  }

  private void prepareStickerData() {
    StickerManager.getInstance().reloadCategories();
    updateSegmentBarVisibility();
    viewBinding.emojTabViewContainer.setVisibility(GONE);
  }

  private void setupCustomStickerGrid() {
    customStickerAdapter =
        new CustomFavoriteStickerAdapter(
            context,
            new CustomFavoriteStickerAdapter.Callback() {
              @Override
              public void onAddClick() {
                if (customStickerActionListener != null) {
                  customStickerActionListener.onRequestAddCustomSticker();
                }
              }

              @Override
              public void onStickerClick(String stickerId) {
                if (listener != null) {
                  listener.onStickerSelected(CustomStickerStore.CATALOG, stickerId);
                }
              }

              @Override
              public void onStickerLongClick(String stickerId) {
                if (customStickerActionListener != null) {
                  customStickerActionListener.onCustomStickerDelete(stickerId);
                }
              }
            });
    viewBinding.customStickerGv.setAdapter(customStickerAdapter);
    viewBinding.customStickerGv.setSelector(R.drawable.emoji_item_selector);
  }

  private void setupSegmentBar() {
    viewBinding.emojiSegmentEmojiTv.setOnClickListener(v -> selectSegmentMode(MODE_EMOJI));
    viewBinding.emojiSegmentStickerTv.setOnClickListener(v -> selectSegmentMode(MODE_STICKER));
  }

  private void updateSegmentBarVisibility() {
    viewBinding.emojiSegmentBar.setVisibility(withSticker ? VISIBLE : GONE);
  }

  private void selectSegmentMode(int mode) {
    segmentMode = mode;
    updateSegmentStyle(mode);
    if (mode == MODE_EMOJI) {
      showEmojiPanel();
    } else {
      showCustomStickerGrid();
    }
  }

  private void updateSegmentStyle(int mode) {
    viewBinding.emojiSegmentEmojiTv.setSelected(mode == MODE_EMOJI);
    viewBinding.emojiSegmentStickerTv.setSelected(mode == MODE_STICKER);
  }

  private void showEmojiPanel() {
    viewBinding.scrPlugin.setVisibility(VISIBLE);
    viewBinding.customStickerGv.setVisibility(GONE);
    viewBinding.layoutScrBottom.setVisibility(VISIBLE);
    if (gifView == null) {
      gifView =
          new EmojiView(context, listener, viewBinding.scrPlugin, viewBinding.layoutScrBottom);
      gifView.setCategoryChangCheckedCallback(this);
    }
    gifView.showStickers(0);
  }

  private void showCustomStickerGrid() {
    viewBinding.scrPlugin.setVisibility(GONE);
    viewBinding.customStickerGv.setVisibility(VISIBLE);
    viewBinding.layoutScrBottom.setVisibility(GONE);
    if (customStickerAdapter != null) {
      customStickerAdapter.refresh();
    }
  }

  private void show() {
    segmentMode = MODE_EMOJI;
    updateSegmentStyle(MODE_EMOJI);
    showEmojiPanel();
  }

  @Override
  public void onCategoryChanged(int index) {
    if (!withSticker) {
      return;
    }
    int mode = index == 0 ? MODE_EMOJI : MODE_STICKER;
    if (segmentMode != mode) {
      segmentMode = mode;
      updateSegmentStyle(mode);
      if (mode == MODE_STICKER) {
        showCustomStickerGrid();
      } else {
        showEmojiPanel();
      }
    }
  }

  public void setWithSticker(boolean withSticker) {
    this.withSticker = withSticker;
    updateSegmentBarVisibility();
    if (loaded) {
      prepareStickerData();
    }
  }
}
