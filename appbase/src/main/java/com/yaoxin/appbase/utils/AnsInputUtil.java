package com.yaoxin.appbase.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

/**
 * 密保等字段：仅允许数字和英文字母，不允许汉字及其他符号。
 */
public final class AnsInputUtil {

    private static final InputFilter ALPHANUMERIC_FILTER =
            new InputFilter() {
                @Override
                public CharSequence filter(
                        CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    if (source == null || start >= end) {
                        return null;
                    }
                    StringBuilder keep = null;
                    for (int i = start; i < end; i++) {
                        char c = source.charAt(i);
                        boolean ok =
                                (c >= '0' && c <= '9')
                                        || (c >= 'a' && c <= 'z')
                                        || (c >= 'A' && c <= 'Z');
                        if (!ok) {
                            if (keep == null) {
                                keep = new StringBuilder(end - start);
                                keep.append(source, start, i);
                            }
                        } else if (keep != null) {
                            keep.append(c);
                        }
                    }
                    return keep;
                }
            };

    private AnsInputUtil() {}

    public static void applyAlphanumericOnly(EditText editText) {
        if (editText == null) {
            return;
        }
        InputFilter[] old = editText.getFilters();
        InputFilter[] filters;
        if (old == null || old.length == 0) {
            filters = new InputFilter[] {ALPHANUMERIC_FILTER};
        } else {
            filters = new InputFilter[old.length + 1];
            System.arraycopy(old, 0, filters, 0, old.length);
            filters[old.length] = ALPHANUMERIC_FILTER;
        }
        editText.setFilters(filters);
    }
}
