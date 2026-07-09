package android.util;

public class Base64 {
    public static final int DEFAULT = 0;
    public static final int NO_WRAP = 2;

    public static String encodeToString(byte[] input, int flags) {
        if (flags == NO_WRAP) {
            return java.util.Base64.getEncoder().withoutPadding().encodeToString(input);
        }
        return java.util.Base64.getEncoder().encodeToString(input);
    }

    public static byte[] decode(String str, int flags) {
        // Remove whitespace/newlines if any, to match Android's lax decoding
        String cleanStr = str.replace("\n", "").replace("\r", "").trim();
        return java.util.Base64.getDecoder().decode(cleanStr);
    }

    public static byte[] decode(byte[] input, int flags) {
        return java.util.Base64.getDecoder().decode(input);
    }
}
