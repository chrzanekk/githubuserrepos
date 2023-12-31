package pl.konradchrzanowski.githubuserrepos.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BranchDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("commit")
    private JsonNode commit;
    @JsonProperty("sha")
    private String commitSha;
}
