package pl.konradchrzanowski.githubuserrepos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;

public interface ClientService {

    ConsumerResponse getUserRepositories(String userName) throws JsonProcessingException;
}
