package pl.konradchrzanowski.githubuserrepos.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.konradchrzanowski.githubuserrepos.service.dto.BranchDTO;

import java.util.List;


public record GitHubBranchesApiResponse(@JsonProperty("branches") List<BranchDTO> branches) {

}
