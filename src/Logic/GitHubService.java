package Logic;

import org.kohsuke.github.*;
import java.io.IOException;

public class GitHubService {

    private final GitHub github;

    public GitHubService() throws IOException {

        this.github = GitHubBuilder.fromEnvironment().build();
    }

    public String getLatestVersion(String owner, String repo) {
        String release = null;
        try {
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHRelease latestRelease = repository.getLatestRelease();
            release = latestRelease.getTagName();
        } catch (IOException e) {
            System.err.println("Error obteniendo versión de " + owner + "/" + repo + ": " + e.getMessage());

        }
        return release;
    }
}
