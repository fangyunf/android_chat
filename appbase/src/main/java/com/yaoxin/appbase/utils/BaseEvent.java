package com.yaoxin.appbase.utils;

import com.yaoxin.appbase.model.GroupInfoBean;

import java.util.HashMap;

public class BaseEvent {
  private HashMap<String, String> params;
  private String tag;
  private String text;
  public GroupInfoBean infoBean;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public BaseEvent() {
  }

  public BaseEvent(String tag) {
    this.tag = tag;
  }

  public HashMap<String, String> getParams() {
    if (this.params == null) {
      this.params = new HashMap();
    }

    return this.params;
  }

  public void put(String key, String value) {
    this.getParams().put(key, value);
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }
}