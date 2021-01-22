package com.example.es.util;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommonUtil {
    /**
     * 去掉 str 中的非中文字
     * @param str
     * @return
     */
    public static String getChineseStr(String str) {
        // 使用正则表达式 [\u4E00-\u9FA5]是unicode2的中文区间
        Pattern pattern = Pattern.compile("[^\u4E00-\u9FA5]");
        Matcher matcher = pattern.matcher(str);
        return  matcher.replaceAll("");
    }
}
