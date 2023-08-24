package pl.konradchrzanowski.githubuserrepos.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.konradchrzanowski.githubuserrepos.service.dto.GitHubRepoDTO;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class GitHubReposApiResponse {
    private @JsonProperty("repos") List<GitHubRepoDTO> repos;
}
