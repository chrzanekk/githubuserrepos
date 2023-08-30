package pl.konradchrzanowski.githubuserrepos.service;

import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;

import java.util.List;

public interface GitHubApiService {

    List<ConsumerResponse> getGithubRepo(String username);
}
