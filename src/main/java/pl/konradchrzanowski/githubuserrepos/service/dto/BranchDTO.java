package pl.konradchrzanowski.githubuserrepos.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public record BranchDTO(@JsonProperty("name") String name, @JsonProperty("commit") String commit) {
}
