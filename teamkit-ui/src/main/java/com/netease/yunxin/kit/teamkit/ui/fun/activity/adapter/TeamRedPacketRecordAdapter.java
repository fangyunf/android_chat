package com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.kit.teamkit.ui.databinding.ItemFunTeamRedPacketRecordBinding;
import com.yaoxin.appbase.R;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.ArrayList;
import java.util.List;

public class TeamRedPacketRecordAdapter extends RecyclerView.Adapter<TeamRedPacketRecordAdapter.RecordViewHolder> {
    private final List<ItemData> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public void setItems(List<ItemData> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFunTeamRedPacketRecordBinding binding =
                ItemFunTeamRedPacketRecordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        ItemData item = items.get(position);
        String lastDay = position == 0 ? "" : items.get(position - 1).dayLabel;
        boolean showDay = !item.dayLabel.equals(lastDay);
        holder.binding.itemFunTeamRedPacketRecordDayTv.setVisibility(showDay ? View.VISIBLE : View.GONE);
        holder.binding.itemFunTeamRedPacketRecordDayTv.setText(item.dayLabel);
        holder.binding.itemFunTeamRedPacketRecordTitleTv.setText(item.title);
        holder.binding.itemFunTeamRedPacketRecordTimeTv.setText(item.timeLabel);
        holder.binding.itemFunTeamRedPacketRecordMoneyTv.setText(item.amountLabel);
        holder.binding.itemFunTeamRedPacketRecordSubTv.setText(item.subTitle);
        int iconRes;
        if (item.customType == 21) {
            // 专属红包
            iconRes = com.netease.yunxin.kit.chatkit.ui.R.drawable.chat_red_packet_open_bg_can_open;
        } else if (item.customType == 28) {
            // 转账
            iconRes = com.netease.yunxin.kit.chatkit.ui.R.drawable.chat_zhuanzhang_bg_no_open;
        } else {
            // 拼手气/普通红包
            iconRes = com.netease.yunxin.kit.chatkit.ui.R.drawable.chat_red_packet_open_bg_group_can_open;
        }
        holder.binding.itemFunTeamRedPacketRecordIconIv.setImageResource(iconRes);
        GlideUtil.loadImage(
                holder.binding.getRoot().getContext(),
                holder.binding.itemFunTeamRedPacketRecordAvatarIv,
                item.avatarUrl,
                R.mipmap.app_default_base_icon_geren,
                R.mipmap.app_default_base_icon_geren);
        holder.binding.getRoot().setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        public final ItemFunTeamRedPacketRecordBinding binding;

        public RecordViewHolder(@NonNull ItemFunTeamRedPacketRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class ItemData {
        public long timestamp;
        public String dayLabel;
        public String title;
        public String timeLabel;
        public String amountLabel;
        public String subTitle;
        public String avatarUrl;
        public String senderName;
        public String redpacketId;
        public String createTime;
        public int customType;
        public String sendName;
        public String sendAvatar;
        public String amount;
        public String toUserId;
    }

    public interface OnItemClickListener {
        void onItemClick(ItemData item);
    }
}
