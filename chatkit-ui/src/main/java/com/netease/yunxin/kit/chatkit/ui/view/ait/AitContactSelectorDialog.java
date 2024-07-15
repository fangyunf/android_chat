// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageAitSelectorDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.AitContactAdapter;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.yaoxin.appbase.model.GroupInfoBean;

import java.util.ArrayList;
import java.util.List;

/** Team member @ Dialog */
public class AitContactSelectorDialog extends BottomSheetDialog {
  private final ChatMessageAitSelectorDialogBinding binding;
  private AitContactAdapter adapter;
  private AitContactAdapter.OnItemSelectListener listener;

  //展示风格，0:办公风格 1:新版本
  private int uiStyle = 0;

  List<UserInfoWithTeam> _data;
  public AitContactSelectorDialog(@NonNull Context context) {
    this(context, R.style.TransBottomSheetTheme);
  }

  public AitContactSelectorDialog(@NonNull Context context, int themeResId) {
    super(context, themeResId);
    binding =
        ChatMessageAitSelectorDialogBinding.inflate(LayoutInflater.from(getContext()), null, false);
    setContentView(
        binding.getRoot(),
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getDisplayHeight() * 2 / 3));

    setCanceledOnTouchOutside(true);
    initViews();
  }

  public void setUIStyle(int style) {
    uiStyle = style;
    switchStyle();
  }

  private void initViews() {
    binding.contactArrowIcon.setOnClickListener(v -> dismiss());
    binding.contactList.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new AitContactAdapter();
    adapter.setOnItemSelectListener(
        item -> {
          if (listener != null) {
            listener.onSelect(item);
          }
          dismiss();
        });
    binding.contactList.setAdapter(adapter);
    binding.chatMessageAitSelectorDialogSearchEt.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        String string = s.toString();
        if (string.isEmpty()) {
          adapter.setMembers(_data);
          adapter.notifyDataSetChanged();
        } else {
          ArrayList<UserInfoWithTeam> tempArr = new ArrayList<>();
          for (UserInfoWithTeam temp :
                  _data) {
            if (temp.getName().contains(string)) {
              tempArr.add(temp);
            }
          }
          adapter.setMembers(tempArr);
          adapter.notifyDataSetChanged();
        }
      }
    });
  }

  private void switchStyle() {
//    if (uiStyle == 0) {
//      binding.getRoot().setBackgroundResource(R.color.color_white);
//      adapter.setAitContactConfig(
//          new AitContactAdapter.AitContactConfig(
//              SizeUtils.dp2px(30),
//              getContext().getResources().getColor(R.color.color_333333),
//              R.drawable.ic_team_all));
//    } else {
      binding.getRoot().setBackgroundResource(R.color.color_ededed);
      adapter.setAitContactConfig(
          new AitContactAdapter.AitContactConfig(
              SizeUtils.dp2px(4),
              getContext().getResources().getColor(R.color.color_222222),
              R.drawable.ic_chat_at_all_avatar));
//    }
  }

  public void setData(List<UserInfoWithTeam> data, boolean refresh) {
    _data = data;
    adapter.setMembers(data);
    if (refresh) {
      adapter.notifyItemRangeChanged(0, adapter.getItemCount());
    }
  }

  public void setData(List<UserInfoWithTeam> data, boolean refresh, boolean showAll) {
    _data = data;
    adapter.setShowAll(showAll);
    adapter.setMembers(data);
    if (refresh) {
      adapter.notifyItemRangeChanged(0, adapter.getItemCount());
    }
  }

  public void setOnItemSelectListener(AitContactAdapter.OnItemSelectListener listener) {
    this.listener = listener;
  }
}
