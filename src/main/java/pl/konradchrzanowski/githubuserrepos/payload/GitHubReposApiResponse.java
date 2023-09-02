package pl.konradchrzanowski.githubuserrepos.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.konradchrzanowski.githubuserrepos.service.dto.GitHubRepoDTO;

import java.util.List;

public record GitHubReposApiResponse(@JsonProperty("repos") List<GitHubRepoDTO> repos) {

}
