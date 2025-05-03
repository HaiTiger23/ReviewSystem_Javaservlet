package com.example.api.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lớp tiện ích để tạo slug từ chuỗi
 */
public class SlugUtil {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    /**
     * Tạo slug từ chuỗi
     * 
     * @param input Chuỗi đầu vào
     * @return Slug đã được tạo
     */
    public static String createSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String noseparator = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noseparator, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH);
        
        // Nếu slug rỗng sau khi xử lý, thêm một ID ngẫu nhiên
        if (slug.isEmpty()) {
            slug = "product-" + System.currentTimeMillis();
        }
        
        return slug;
    }
}
