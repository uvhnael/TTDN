package org.uvhnael.ktal.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlCleaner {
    /**
     * Clean HTML content by removing tags, scripts, and styles.
     *
     * @param htmlContent The HTML content to be cleaned.
     * @return Cleaned text content.
     */
    public static String cleanHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }

        // Parse HTML
        Document doc = Jsoup.parse(htmlContent);

        // Loại bỏ script và style
        doc.select("script, style, noscript, img, video, iframe, audio, source").remove();

        // Lấy text đã clean
        return doc.text().trim();
    }
}
