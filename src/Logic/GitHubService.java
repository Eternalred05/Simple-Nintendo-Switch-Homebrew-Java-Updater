package Logic;

import org.kohsuke.github.*;
import java.io.IOException;
import java.util.List;

public class GitHubService {

    private final GitHub github;

    public GitHubService() throws IOException {

        this.github = new GitHubBuilder().build();

    }

    public String getLatestVersion(String owner, String repo) {
        String s = null;
        try {
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHRelease latestRelease = repository.getLatestRelease();
            s = latestRelease.getTagName();
        } catch (IOException e) {
            s = "Error";
        }
        return s;
    }

    public String getAssetDownloadUrl(String owner, String repo) {
        String s = null;
        try {
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHRelease latestRelease = repository.getLatestRelease();
            List<GHAsset> assets = latestRelease.getAssets();

            for (GHAsset asset : assets) {
                if (asset.getName().toLowerCase().endsWith(".zip")) {
                    s = asset.getBrowserDownloadUrl();
                }
            }

            if (!assets.isEmpty()) {
                s = assets.get(0).getBrowserDownloadUrl();
            }
        } catch (IOException e) {

        }
        return s;
    }
}
