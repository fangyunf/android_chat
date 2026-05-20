package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.util.ArrayList;
import java.util.List;

/** 自定义表情包网格：首格为添加按钮。 */
public class CustomFavoriteStickerAdapter extends BaseAdapter {

  public interface Callback {
    void onAddClick();

    void onStickerClick(String stickerId);

    void onStickerLongClick(String stickerId);
  }

  private static final int TYPE_ADD = 0;
  private static final int TYPE_STICKER = 1;

  private final Context context;
  private final Callback callback;
  private final List<String> stickerIds = new ArrayList<>();

  public CustomFavoriteStickerAdapter(Context context, Callback callback) {
    this.context = context;
    this.callback = callback;
  }

  public void refresh() {
    stickerIds.clear();
    for (StickerItem item : CustomStickerStore.getInstance().getStickerItems()) {
      String name = item.getName();
      if (name != null && name.endsWith(".png")) {
        stickerIds.add(name.substring(0, name.length() - 4));
      } else {
        stickerIds.add(name);
      }
    }
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return stickerIds.size() + 1;
  }

  @Override
  public int getItemViewType(int position) {
    return position == 0 ? TYPE_ADD : TYPE_STICKER;
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public Object getItem(int position) {
    return position == 0 ? null : stickerIds.get(position - 1);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (getItemViewType(position) == TYPE_ADD) {
      if (convertView == null || convertView.findViewById(R.id.custom_sticker_add_btn) == null) {
        convertView = View.inflate(context, R.layout.chat_custom_sticker_add_item, null);
        convertView.findViewById(R.id.custom_sticker_add_btn)
            .setOnClickListener(v -> callback.onAddClick());
      }
      return convertView;
    }

    ViewHolder holder;
    if (convertView == null) {
      convertView = View.inflate(context, R.layout.chat_sticker_picker_view, null);
      holder = new ViewHolder();
      holder.imageView = convertView.findViewById(R.id.sticker_thumb_image);
      convertView.findViewById(R.id.sticker_desc_label).setVisibility(View.GONE);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    String stickerId = stickerIds.get(position - 1);
    String path = CustomStickerStore.getInstance().getLocalPath(stickerId);
    Glide.with(context)
        .load(path)
        .apply(
            new RequestOptions()
                .error(R.drawable.ic_img_failed)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate())
        .into(holder.imageView);

    convertView.setOnClickListener(v -> callback.onStickerClick(stickerId));
    convertView.setOnLongClickListener(
        v -> {
          callback.onStickerLongClick(stickerId);
          return true;
        });
    return convertView;
  }

  static class ViewHolder {
    ImageView imageView;
  }
}
