package nz.ac.auckland.aem.lmz.dto;

/**
 * @author Marnix Cook
 *
 * Contains a description of the location a LMZ widget is used.
 */
public class UsageLocation {

    private String component;
    private String url;
    private String pageTitle;

    /**
     * Initialize data-members
     *
     * @param component component name
     * @param url url of the component use
     * @param pageTitle is the page title of the page the component is one
     */
    public UsageLocation(String component, String url, String pageTitle) {
        this.component = component;
        this.url = url;
        this.pageTitle = pageTitle;
    }

    public String getComponent() {
        return component.substring(component.lastIndexOf('/') + 1);
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }
}
