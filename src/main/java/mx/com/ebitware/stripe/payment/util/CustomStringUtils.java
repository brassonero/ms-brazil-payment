package mx.com.ebitware.stripe.payment.util;

import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class CustomStringUtils {

    /**
     * Removes special characters and blank spaces in a String,
     *
     * @param s is a String to validate
     * @return string without blank spaces or special characters
     */
    public static String normalizeName(String s) {
        s = StringUtils.stripAccents(s);
        s = s.replaceAll(" ", "_");
        s = s.replaceAll("\\u0020", "_");
        s = s.replaceAll("\\u202F", "_");
        s = replaceCaracters(s);
        return s;
    }

    /**
     * ¨ Converts a map object to JSON string
     *
     * @param object is a map object
     * @return string with JSON format
     */
    public static String buildMapJson(Map<String, String> object) {
        return new Gson().toJson(object);
    }

    /**
     * ¨ Converts a map object to JSON string
     *
     * @param object is a map object
     * @return string with JSON format
     */
    public static String buildMapJson(Object object) {
        return new Gson().toJson(object);
    }

    /**
     * If a string length is greater than 1600 it will be resized to 1600
     *
     * @param string to validate
     * @return resized string
     */
    public static String resizeString(String s) {
        if (s.length() > 1600) {
            s = s.substring(0, 1599);
        }
        return s;
    }

    /**
     * Validates if a string is null: if it is true returns an empty string, if not
     * returns original string
     */
    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static String toUTF8(String s) {
        byte[] bytes;
        try {
            bytes = s.getBytes("UTF-8");
            String result = new String(bytes, "UTF-8");
            return result;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String decodeUTF8(String encoded) {
        try {
            return new String(encoded.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }

    }

    public static String getFilename(String fileUrl) {
        URL url = null;
        try {
            url = new URL(fileUrl);
            return FilenameUtils.getName(url.getPath());
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public static String replaceCaracters(String text) {
        String replaced = "";
        replaced = text.replaceAll("[\\[\\+\\^\\]\\-\\(\\{\\}\\?\\:\\,\\)\\*\\#\\@\\=\\$\\%\\@\\`\\'\\~\\´]", "").trim();
        return replaced;
    }

}
