package pl.konradchrzanowski.githubuserrepos.service.impl;

import org.springframework.stereotype.Service;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;
import pl.konradchrzanowski.githubuserrepos.service.ClientService;
import pl.konradchrzanowski.githubuserrepos.service.GitHubApiService;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {
    private final GitHubApiService gitHubApiService;

    public ClientServiceImpl(GitHubApiService gitHubApiService) {
        this.gitHubApiService = gitHubApiService;
    }

    @Override
    public List<ConsumerResponse> getUserRepositories(String userName) {
        return gitHubApiService.getGithubRepo(userName);
    }
}
