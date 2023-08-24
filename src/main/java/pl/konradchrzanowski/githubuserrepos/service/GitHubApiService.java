package pl.konradchrzanowski.githubuserrepos.service;

import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;

public interface GitHubApiService {

    ConsumerResponse getGithubRepo(String username);
}
