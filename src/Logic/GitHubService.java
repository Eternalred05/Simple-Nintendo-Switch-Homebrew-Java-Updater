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
        try {
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHRelease latestRelease = repository.getLatestRelease();
            return latestRelease.getTagName();
        } catch (IOException e) {
            return "Error";
        }
    }

    public String getAssetDownloadUrl(String owner, String repo) {
        try {
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHRelease latestRelease = repository.getLatestRelease();
            List<GHAsset> assets = latestRelease.getAssets();

            for (GHAsset asset : assets) {
                if (asset.getName().toLowerCase().endsWith(".zip")) {
                    return asset.getBrowserDownloadUrl();
                }
            }

            if (!assets.isEmpty()) {
                return assets.get(0).getBrowserDownloadUrl();
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
