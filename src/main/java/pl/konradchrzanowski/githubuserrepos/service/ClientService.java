package pl.konradchrzanowski.githubuserrepos.service;

import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;

public interface ClientService {

    ConsumerResponse getUserRepositories(String userName);
}
