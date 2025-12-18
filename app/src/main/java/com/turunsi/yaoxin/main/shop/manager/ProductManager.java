package com.turunsi.yaoxin.main.shop.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.main.shop.model.ProductModel;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 商品管理器（单例）
 */
public class ProductManager {
    private static final String PREFS_NAME = "ProductManager";
    private static final String KEY_COLLECTED_PRODUCTS = "collected_products";
    
    private static ProductManager instance;
    private List<ProductModel> allProducts = new ArrayList<>();
    private List<ProductModel> buyProducts = new ArrayList<>();
    private List<ProductModel> randomProducts = new ArrayList<>();
    private List<ProductModel> randomBuyProducts = new ArrayList<>();
    private Map<String, ProductModel> productsMap = new HashMap<>();
    private Context context;
    
    private ProductManager(Context context) {
        this.context = context.getApplicationContext();
        loadCollectedProducts();
    }
    
    public static synchronized ProductManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProductManager(context);
        }
        return instance;
    }
    
    /**
     * 加载商品列表（从JSON文件）
     */
    public void loadProducts() {
        try {
            InputStream is = context.getAssets().open("carme.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ProductModel>>(){}.getType();
            allProducts = gson.fromJson(json, listType);
            
            // 解析日期字段
            parseDates(allProducts);
            
            // 创建产品ID映射
            productsMap.clear();
            for (ProductModel product : allProducts) {
                productsMap.put(product.productId, product);
            }
            
            // 同步收藏状态
            syncCollectionStatus();
        } catch (Exception e) {
            e.printStackTrace();
            allProducts = new ArrayList<>();
        }
    }
    
    /**
     * 加载购买商品列表
     */
    public void loadBuyProducts() {
        try {
            InputStream is = context.getAssets().open("carme.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ProductModel>>(){}.getType();
            buyProducts = gson.fromJson(json, listType);
            
            // 解析日期字段
            parseDates(buyProducts);
            
            // 创建产品ID映射
            productsMap.clear();
            for (ProductModel product : buyProducts) {
                productsMap.put(product.productId, product);
            }
            
            // 同步收藏状态
            syncCollectionStatus();
        } catch (Exception e) {
            e.printStackTrace();
            buyProducts = new ArrayList<>();
        }
    }
    
    /**
     * 解析日期字段
     */
    private void parseDates(List<ProductModel> products) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        for (ProductModel product : products) {
            try {
                if (product.limitedTimeActivityStartTime != null && !product.limitedTimeActivityStartTime.isEmpty()) {
                    product.limitedTimeActivityStartDate = formatter.parse(product.limitedTimeActivityStartTime);
                }
                if (product.limitedTimeActivityEndTime != null && !product.limitedTimeActivityEndTime.isEmpty()) {
                    product.limitedTimeActivityEndDate = formatter.parse(product.limitedTimeActivityEndTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 同步收藏状态
     */
    private void syncCollectionStatus() {
        List<ProductModel> collected = getCollectedProducts();
        Set<String> collectedIds = new java.util.HashSet<>();
        for (ProductModel product : collected) {
            collectedIds.add(product.productId);
        }
        
        for (ProductModel product : allProducts) {
            product.isCollected = collectedIds.contains(product.productId);
        }
        
        for (ProductModel product : buyProducts) {
            product.isCollected = collectedIds.contains(product.productId);
        }
    }
    
    /**
     * 获取随机商品列表
     */
    public List<ProductModel> getRandomProducts() {
        if (allProducts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ProductModel> randomList = new ArrayList<>();
        List<ProductModel> available = new ArrayList<>(allProducts);
        Random random = new Random();
        
        // 生成随机商品，排除已选中的购买商品
        while (available.size() > 0 && randomList.size() < allProducts.size()) {
            int randomIndex = random.nextInt(available.size());
            ProductModel product = available.remove(randomIndex);
            
            // 检查是否已存在
            boolean exists = false;
            for (ProductModel p : randomList) {
                if (p.productId.equals(product.productId)) {
                    exists = true;
                    break;
                }
            }
            
            // 检查是否在购买商品列表中
            if (!exists && !randomBuyProducts.isEmpty()) {
                for (ProductModel p : randomBuyProducts) {
                    if (p.productId.equals(product.productId)) {
                        exists = true;
                        break;
                    }
                }
            }
            
            if (!exists) {
                randomList.add(product);
            }
        }
        
        randomProducts = randomList;
        return randomList;
    }
    
    /**
     * 获取随机购买商品列表（3个）
     */
    public List<ProductModel> getRandomBuyProducts() {
        if (buyProducts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ProductModel> randomList = new ArrayList<>();
        List<ProductModel> available = new ArrayList<>(buyProducts);
        Random random = new Random();
        
        // 生成随机商品，排除已选中的商品
        while (available.size() > 0 && randomList.size() < 3) {
            int randomIndex = random.nextInt(available.size());
            ProductModel product = available.remove(randomIndex);
            
            // 检查是否已存在
            boolean exists = false;
            for (ProductModel p : randomList) {
                if (p.productId.equals(product.productId)) {
                    exists = true;
                    break;
                }
            }
            
            // 检查是否在商品列表中
            if (!exists && !randomProducts.isEmpty()) {
                for (ProductModel p : randomProducts) {
                    if (p.productId.equals(product.productId)) {
                        exists = true;
                        break;
                    }
                }
            }
            
            if (!exists) {
                randomList.add(product);
            }
        }
        
        randomBuyProducts = randomList;
        return randomList;
    }
    
    /**
     * 切换收藏状态
     */
    public void toggleCollectStatusForProduct(String productId) {
        ProductModel product = productsMap.get(productId);
        if (product == null) return;
        
        product.isCollected = !product.isCollected;
        
        List<ProductModel> collected = getCollectedProducts();
        if (product.isCollected) {
            collected.add(product);
        } else {
            collected.removeIf(p -> p.productId.equals(productId));
        }
        
        saveCollectedProducts(collected);
    }
    
    /**
     * 获取收藏的商品列表
     */
    public List<ProductModel> getCollectedProducts() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_COLLECTED_PRODUCTS, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ProductModel>>(){}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 保存收藏的商品列表
     */
    private void saveCollectedProducts(List<ProductModel> products) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(products);
        prefs.edit().putString(KEY_COLLECTED_PRODUCTS, json).apply();
    }
    
    /**
     * 加载收藏的商品列表
     */
    private void loadCollectedProducts() {
        List<ProductModel> collected = getCollectedProducts();
        for (ProductModel product : collected) {
            ProductModel current = productsMap.get(product.productId);
            if (current != null) {
                current.isCollected = true;
            }
        }
    }
    
    public List<ProductModel> getAllProducts() {
        return allProducts;
    }
    
    public List<ProductModel> getBuyProducts() {
        return buyProducts;
    }
}

