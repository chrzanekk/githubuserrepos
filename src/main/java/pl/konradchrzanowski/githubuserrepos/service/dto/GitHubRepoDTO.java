package pl.konradchrzanowski.githubuserrepos.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


public record GitHubRepoDTO(@JsonProperty("name") String name,
                            @JsonProperty("owner") String owner,
                            @JsonProperty("fork") boolean fork,
                            @JsonProperty("branches_url") String branchesUrl) {

}
