package pl.konradchrzanowski.githubuserrepos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;

import java.util.List;

public interface ClientService {

    List<ConsumerResponse> getUserRepositories(String userName) throws JsonProcessingException;
}
