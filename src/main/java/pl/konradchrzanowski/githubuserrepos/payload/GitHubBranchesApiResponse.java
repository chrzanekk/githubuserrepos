package pl.konradchrzanowski.githubuserrepos.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import pl.konradchrzanowski.githubuserrepos.service.dto.BranchDTO;

import java.util.List;


@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class GitHubBranchesApiResponse {
    private @JsonProperty("branches")List<BranchDTO> branches;
}
