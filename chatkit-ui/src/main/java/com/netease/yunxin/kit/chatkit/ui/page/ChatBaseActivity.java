// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatActivityLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CloseChatPageEvent;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;

/** BaseActivity for Chat include P2P chat page and Team chat page */
public abstract class ChatBaseActivity extends BaseActivity {

  ChatActivityLayoutBinding binding;

  protected final EventNotify<CloseChatPageEvent> closeEventNotify =
      new EventNotify<CloseChatPageEvent>() {
        @Override
        public void onNotify(@NonNull CloseChatPageEvent message) {
          finish();
        }

        @NonNull
        @Override
        public String getEventType() {
          return CloseChatPageEvent.EVENT_TYPE;
        }
      };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(null);
    EventCenter.notifyEventSync(new CloseChatPageEvent());
    EventCenter.registerEventNotify(closeEventNotify);
    binding = ChatActivityLayoutBinding.inflate(LayoutInflater.from(this));
    setContentView(binding.getRoot());
    initChat();
  }

  protected abstract void initChat();

  /**
   * 相册等由 Activity 发起的 startActivityForResult，需转发给聊天 Fragment（如添加自定义表情包）。
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
    if (fragment != null) {
      fragment.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    //stop message audio
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventCenter.unregisterEventNotify(closeEventNotify);
  }
}
