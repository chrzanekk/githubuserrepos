package pl.konradchrzanowski.githubuserrepos.payload;

import com.fasterxml.jackson.annotation.JsonProperty;


public record ConsumerRequest(@JsonProperty("username") String username) {

}
