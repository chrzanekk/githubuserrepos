package pl.konradchrzanowski.githubuserrepos.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitHubRepoDTO {
    private @JsonProperty("name") String name;
    private @JsonProperty("owner") JsonNode owner;
    private @JsonProperty("fork") boolean fork;
    private @JsonProperty("branches_url") String branchesUrl;
    private @JsonIgnore String login;
}
