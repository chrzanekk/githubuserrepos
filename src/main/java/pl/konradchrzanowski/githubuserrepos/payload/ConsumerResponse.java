package pl.konradchrzanowski.githubuserrepos.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.konradchrzanowski.githubuserrepos.service.dto.BranchDTO;

import java.util.List;
@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class ConsumerResponse {
    private String name;
    private String ownerLogin;
    private List<BranchDTO> branches;
}
