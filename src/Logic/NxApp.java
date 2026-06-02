package Logic;

import java.io.IOException;

public class NxApp {

    private String name;
    private String version;
    private String url;
    private String repoOwner;
    private String repoName;

    public NxApp(String name, String url) {

        this.name = name;
        this.url = url;
        String[] info = extractRepoInfo(url);
        if (info != null) {
            this.repoOwner = info[0];
            this.repoName = info[1];
            try {
                GitHubService g = new GitHubService();
                String v = g.getLatestVersion(info[0], info[1]);
                this.version = (v != null) ? v : "Unknown";
            } catch (IOException e) {
                this.version = "Unknown";
                System.out.println("GitHub error: " + e.getMessage());
            }
        }
    }

    public static String[] extractRepoInfo(String githubUrl) {

        String[] s = null;

        String cleanUrl = githubUrl.replaceAll("https?://github.com/", "");
        if (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
        }
        String[] parts = cleanUrl.split("/");
        if (parts.length >= 2) {
            s = new String[]{parts[0], parts[1]};
        }
        return s;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getRepoName() {
        return repoName;
    }

}
