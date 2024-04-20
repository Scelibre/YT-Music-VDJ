package com.scelibre.youtube.util;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnicodeEscapedStringConverter {

    private static final Pattern UNICODE_ESCAPED_PATTERN = Pattern.compile("\\\\x([0-9A-Fa-f]{2})");

    public static String convertUnicodeEscapes(String input) {
        Matcher matcher = UNICODE_ESCAPED_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer(input.length());
        while (matcher.find()) {
            int codePoint = Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(sb, "");
            sb.appendCodePoint(codePoint);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}