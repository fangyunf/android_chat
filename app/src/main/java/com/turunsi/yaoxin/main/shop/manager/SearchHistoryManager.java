package com.turunsi.yaoxin.main.shop.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索历史管理器
 */
public class SearchHistoryManager {
    
    private static final String PREF_NAME = "search_history";
    private static final String KEY_SEARCH_HISTORY = "search_history_list";
    private static final int MAX_HISTORY_COUNT = 10;
    
    private SharedPreferences preferences;
    private Gson gson;
    
    public SearchHistoryManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * 添加搜索历史
     */
    public void addSearchHistory(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return;
        }
        
        List<String> history = getSearchHistory();
        
        // 移除已存在的相同项
        history.remove(searchText);
        
        // 添加到开头
        history.add(0, searchText);
        
        // 限制历史记录数量
        if (history.size() > MAX_HISTORY_COUNT) {
            history = new ArrayList<>(history.subList(0, MAX_HISTORY_COUNT));
        }
        
        // 保存到 SharedPreferences（使用 Gson 保持顺序）
        saveSearchHistory(history);
    }
    
    /**
     * 获取搜索历史
     */
    public List<String> getSearchHistory() {
        String json = preferences.getString(KEY_SEARCH_HISTORY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> history = gson.fromJson(json, listType);
            return history != null ? history : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 保存搜索历史
     */
    private void saveSearchHistory(List<String> history) {
        String json = gson.toJson(history);
        preferences.edit().putString(KEY_SEARCH_HISTORY, json).apply();
    }
    
    /**
     * 清空搜索历史
     */
    public void clearSearchHistory() {
        preferences.edit().remove(KEY_SEARCH_HISTORY).apply();
    }
    
    /**
     * 删除指定的搜索历史
     */
    public void removeSearchHistory(String searchText) {
        if (searchText == null) {
            return;
        }
        
        List<String> history = getSearchHistory();
        history.remove(searchText);
        saveSearchHistory(history);
    }
}

