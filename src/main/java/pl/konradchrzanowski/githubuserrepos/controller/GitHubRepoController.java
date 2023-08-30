package pl.konradchrzanowski.githubuserrepos.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerRequest;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;
import pl.konradchrzanowski.githubuserrepos.service.ClientService;

import java.util.List;

@RestController
@RequestMapping("/api/githubrepo/")
public class GitHubRepoController {

    private final ClientService clientService;

    public GitHubRepoController(ClientService clientService) {
        this.clientService = clientService;
    }


    @GetMapping("/repositories")
    public ResponseEntity<List<ConsumerResponse>> getUserRepositories(@RequestBody ConsumerRequest request) {
        List<ConsumerResponse> response = clientService.getUserRepositories(request.getUsername());
        return ResponseEntity.ok(response);
    }
}
