package pl.konradchrzanowski.githubuserrepos.payload;

import pl.konradchrzanowski.githubuserrepos.service.dto.BranchDTO;

import java.util.List;

public record ConsumerResponse(String name,
                               String ownerLogin,
                               List<BranchDTO> branches) {
}
