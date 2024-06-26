package com.yaoxin.appbase.utils;

import java.util.HashMap;

public class BaseEvent {
  private HashMap<String, String> params;
  private String tag;

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