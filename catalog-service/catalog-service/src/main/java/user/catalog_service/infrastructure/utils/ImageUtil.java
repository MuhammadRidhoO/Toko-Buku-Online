package user.catalog_service.infrastructure.utils;

import java.util.Base64;

public class ImageUtil {

    public static String encodeStringToBase64(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return Base64.getEncoder().encodeToString(text.getBytes());
    }
}