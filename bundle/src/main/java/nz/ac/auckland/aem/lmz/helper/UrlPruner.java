package nz.ac.auckland.aem.lmz.helper;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: Marnix Cook <m.cook@auckland.ac.nz>
 */
public class UrlPruner {

    public static final int MAX_URL_WIDTH = 80;

    protected int maxUrlWidth = MAX_URL_WIDTH;

    /**
     * @return a version of the URL that is no longer than 80 characters
     */
    public String getPrunedUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }

        int halfWidth = this.maxUrlWidth / 2;
        if (url.length() > this.maxUrlWidth) {
            return
                url.substring(0, halfWidth) + " ... " +
                url.substring(url.length() - halfWidth, url.length());
        }

        return url;
    }

    protected void setMaxUrlWidth(int maxUrlWidth) {
        this.maxUrlWidth = maxUrlWidth;
    }

}
