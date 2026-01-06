package com.yaoxin.appbase.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

/**
 * 资源获取工具类
 * 用于动态获取图片、颜色、背景等资源
 */
public class ResourceHelper {

    /**
     * 根据名称获取Drawable资源
     *
     * @param context      上下文
     * @param resourceName 资源名称（不包含扩展名）
     * @param resourceType 资源类型（如"mipmap", "drawable"等）
     * @return Drawable对象，如果未找到则返回null
     */
    public static Drawable getDrawableByName(Context context, String resourceName, String resourceType) {
        if (context == null || resourceName == null || resourceType == null) {
            return null;
        }

        int resId = context.getResources().getIdentifier(resourceName, resourceType, context.getPackageName());
        if (resId > 0) {
            return ContextCompat.getDrawable(context, resId);
        }
        return null;
    }

    /**
     * 根据名称获取Drawable资源（默认从mipmap中获取）
     *
     * @param context      上下文
     * @param resourceName 资源名称（不包含扩展名）
     * @return Drawable对象，如果未找到则返回null
     */
    public static Drawable getDrawableByName(Context context, String resourceName) {
        return getDrawableByName(context, resourceName, "mipmap");
    }

    /**
     * 根据名称获取颜色资源
     *
     * @param context   上下文
     * @param colorName 颜色资源名称
     * @return 颜色值，如果未找到则返回0
     */
    public static int getColorByName(Context context, String colorName) {
        if (context == null || colorName == null) {
            return 0;
        }
        int resId = context.getResources().getIdentifier(colorName, "color", context.getPackageName());
        if (resId > 0) {
            return ContextCompat.getColor(context, resId);
        }
        return 0;
    }

    /**
     * 根据名称获取背景Drawable
     *
     * @param context        上下文
     * @param backgroundName 背景资源名称
     * @return Drawable对象，如果未找到则返回null
     */
    public static Drawable getBackgroundByName(Context context, String backgroundName) {
        return getDrawableByName(context, backgroundName, "mipmap");
    }

    /**
     * 根据名称获取背景Drawable（支持多种资源类型）
     *
     * @param context        上下文
     * @param backgroundName 背景资源名称
     * @param resourceType   资源类型（如"drawable", "mipmap"等）
     * @return Drawable对象，如果未找到则返回null
     */
    public static Drawable getBackgroundByName(Context context, String backgroundName, String resourceType) {
        return getDrawableByName(context, backgroundName, resourceType);
    }

    /**
     * 检查资源是否存在
     *
     * @param context      上下文
     * @param resourceName 资源名称
     * @param resourceType 资源类型
     * @return 如果资源存在返回true，否则返回false
     */
    public static boolean isResourceExists(Context context, String resourceName, String resourceType) {
        if (context == null || resourceName == null || resourceType == null) {
            return false;
        }

        int resId = context.getResources().getIdentifier(resourceName, resourceType, context.getPackageName());
        return resId > 0;
    }

    /**
     * 根据等级获取等级颜色
     *
     * @param context 上下文
     * @param grade   等级
     * @return 颜色值，如果未找到则返回0
     */
    public static int getGradeColor(Context context, int grade) {
        String colorName = "grade_color_" + grade;
        return getColorByName(context, colorName);
    }

    /**
     * 根据等级获取等级背景
     *
     * @param context 上下文
     * @param grade   等级
     * @return Drawable对象，如果未找到则返回null
     */
    public static Drawable getGradeBackground(Context context, int grade) {
        String backgroundName = "grade_bg_" + grade;
        return getBackgroundByName(context, backgroundName);
    }

    /**
     * 根据等级获取等级图标
     *
     * @param context 上下文
     * @param grade   等级
     * @return Drawable对象，如果未找到则返回null
     */
    public static Drawable getGradeDrawable(Context context, int grade) {
        String imageName = "mine_grade_level_" + grade;
        return getDrawableByName(context, imageName, "mipmap");
    }

} 