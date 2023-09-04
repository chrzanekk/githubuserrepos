package pl.konradchrzanowski.githubuserrepos.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.konradchrzanowski.githubuserrepos.exception.UserNotFoundException;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;
import pl.konradchrzanowski.githubuserrepos.service.GitHubApiService;
import pl.konradchrzanowski.githubuserrepos.service.dto.BranchDTO;
import pl.konradchrzanowski.githubuserrepos.service.dto.CommitDTO;
import pl.konradchrzanowski.githubuserrepos.service.dto.GitHubRepoDTO;
import pl.konradchrzanowski.githubuserrepos.service.dto.OwnerDTO;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitHubApiServiceImpl implements GitHubApiService {

    private static final String BRANCHES = "/branches";
    private static final String REPOS = "/repos";
    private final Logger log = LoggerFactory.getLogger(GitHubApiServiceImpl.class);
    @Value("${github.api.baseUrl}")
    private String baseUrl;

    private final ObjectMapper objectMapper;

    private final WebClient webClient;

    public GitHubApiServiceImpl(ObjectMapper objectMapper, WebClient webClient) {
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }

    @Override
    public List<ConsumerResponse> getGithubRepo(String username) throws JsonProcessingException {
        log.debug("Request to get list of github repositories of: {} ", username);
        String uri = uriCreatorForUserGitHubRepos(username);
        String reposString = getJsonFromUri(uri);
        List<GitHubRepoDTO> gitHubRepoDTOList = objectMapper.readValue(reposString, new TypeReference<>() {
        });
        List<ConsumerResponse> responses = new ArrayList<>();
        gitHubRepoDTOList.forEach(gitHubRepoDTO -> {
            List<BranchDTO> branchDTOList = getBranchesFromUri(gitHubRepoDTO);
            branchDTOList.forEach(branchDTO -> {
                branchDTO.toBuilder().commitSha(branchDTO.getCommit().get("sha").textValue()).commit(null);
            });
            responses.add(new ConsumerResponse(gitHubRepoDTO.getName(), gitHubRepoDTO.getOwner().get("login").textValue(),
                    branchDTOList));
        });
        return responses;
    }

    private List<BranchDTO> getBranchesFromUri(GitHubRepoDTO gitHubRepoDTO) {
        String branchesUri = gitHubRepoDTO.getBranchesUrl();
        String branchesUriSubstring = branchesUri.substring(0, branchesUri.lastIndexOf('{'));
        String branchesString =
                getJsonFromUri(branchesUriSubstring);
        try {
            return objectMapper.readValue(branchesString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private String getJsonFromUri(String uri) {
        return webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .bodyToMono(new ParameterizedTypeReference<String>() {
                }).block();

    }


    private String uriCreatorForUserGitHubRepos(String userName) {
        return baseUrl + "/" + userName + REPOS;
    }

    private Mono<Throwable> handleError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class).flatMap(body -> {
            log.debug("Not found");
            return Mono.error(new UserNotFoundException("Not found"));
        });
    }

}
