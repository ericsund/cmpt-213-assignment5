package ca.as4.models;

public class AboutResponse {

    private String appName;
    private String authorName;

    public AboutResponse() {}

    public AboutResponse(String appName, String authorName) {
        this.appName = appName;
        this.authorName = authorName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAppName() {
        return appName;
    }

    public String getAuthorName() {
        return authorName;
    }
}
