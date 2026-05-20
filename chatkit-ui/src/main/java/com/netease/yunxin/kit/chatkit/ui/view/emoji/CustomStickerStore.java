package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import com.netease.yunxin.kit.common.utils.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** 用户自定义表情包（类似微信收藏表情），本地存储。 */
public class CustomStickerStore {

  public static final String CATALOG = "custom";
  private static final String SP_NAME = "custom_sticker_store";
  private static final String KEY_IDS = "sticker_ids";

  private static CustomStickerStore instance;

  private Context appContext;
  private File stickerDir;
  private final List<String> stickerIds = new ArrayList<>();

  public static CustomStickerStore getInstance() {
    if (instance == null) {
      instance = new CustomStickerStore();
    }
    return instance;
  }

  public void init(Context context) {
    if (context == null) {
      return;
    }
    appContext = context.getApplicationContext();
    stickerDir = new File(appContext.getFilesDir(), "custom_stickers");
    if (!stickerDir.exists()) {
      stickerDir.mkdirs();
    }
    loadIds();
  }

  private void loadIds() {
    stickerIds.clear();
    if (appContext == null) {
      return;
    }
    SharedPreferences sp = appContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    String raw = sp.getString(KEY_IDS, "");
    if (TextUtils.isEmpty(raw)) {
      return;
    }
    stickerIds.addAll(Arrays.asList(raw.split(",")));
    List<String> valid = new ArrayList<>();
    for (String id : stickerIds) {
      if (!TextUtils.isEmpty(id) && getLocalFile(id).exists()) {
        valid.add(id);
      }
    }
    stickerIds.clear();
    stickerIds.addAll(valid);
    saveIds();
  }

  private void saveIds() {
    if (appContext == null) {
      return;
    }
    SharedPreferences sp = appContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    sp.edit().putString(KEY_IDS, TextUtils.join(",", stickerIds)).apply();
  }

  public List<StickerItem> getStickerItems() {
    List<StickerItem> items = new ArrayList<>();
    for (String id : stickerIds) {
      items.add(new StickerItem(CATALOG, id + ".png"));
    }
    return items;
  }

  /** @return 成功添加的数量 */
  public int addStickers(List<String> sourcePaths) {
    if (sourcePaths == null || sourcePaths.isEmpty()) {
      return 0;
    }
    int count = 0;
    for (String path : sourcePaths) {
      if (addSticker(path) != null) {
        count++;
      }
    }
    return count;
  }

  public String addSticker(String sourcePath) {
    if (TextUtils.isEmpty(sourcePath)) {
      return null;
    }
    if (appContext == null && EmojiManager.getContext() != null) {
      init(EmojiManager.getContext());
    }
    if (stickerDir == null) {
      return null;
    }
    String id = UUID.randomUUID().toString().replace("-", "");
    File target = getLocalFile(id);
    try {
      if (sourcePath.startsWith("content://")) {
        copyUriToFile(Uri.parse(sourcePath), target);
      } else {
        File src = new File(sourcePath);
        if (!src.exists()) {
          return null;
        }
        copyToFile(src, target);
      }
      stickerIds.add(0, id);
      saveIds();
      return id;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void removeSticker(String stickerId) {
    if (TextUtils.isEmpty(stickerId)) {
      return;
    }
    stickerIds.remove(stickerId);
    File f = getLocalFile(stickerId);
    if (f.exists()) {
      f.delete();
    }
    saveIds();
  }

  public String getLocalPath(String stickerId) {
    if (TextUtils.isEmpty(stickerId)) {
      return null;
    }
    String name = stickerId;
    if (name.contains(".")) {
      name = FileUtils.getFileNameNoExtension(name);
    }
    File file = getLocalFile(name);
    if (file.exists()) {
      return file.getAbsolutePath();
    }
    return null;
  }

  private File getLocalFile(String id) {
    return new File(stickerDir, id + ".png");
  }

  public boolean hasStickers() {
    return !stickerIds.isEmpty();
  }

  private void copyUriToFile(Uri uri, File dest) throws IOException {
    try (InputStream in = appContext.getContentResolver().openInputStream(uri);
        OutputStream out = new FileOutputStream(dest)) {
      if (in == null) {
        throw new IOException("Cannot open uri: " + uri);
      }
      byte[] buf = new byte[8192];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    }
  }

  private static void copyToFile(File src, File dest) throws IOException {
    try (InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest)) {
      byte[] buf = new byte[8192];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    }
  }
}
