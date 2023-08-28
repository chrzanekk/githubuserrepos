package pl.konradchrzanowski.githubuserrepos.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.konradchrzanowski.githubuserrepos.exception.UserNotFoundException;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;
import pl.konradchrzanowski.githubuserrepos.service.GitHubApiService;
import pl.konradchrzanowski.githubuserrepos.service.dto.BranchDTO;
import pl.konradchrzanowski.githubuserrepos.service.dto.GitHubRepoDTO;
import pl.konradchrzanowski.githubuserrepos.service.dto.OwnerDTO;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitHubApiServiceImpl implements GitHubApiService {

    private final Logger log = LoggerFactory.getLogger(GitHubApiServiceImpl.class);

    private static final String REPOS = "/repos";
    private static final String BRANCHES = "/branches";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${github.api.baseUrl}")
    private String baseUrl;

    private final WebClient webClient;

    public GitHubApiServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public ConsumerResponse getGithubRepo(String username) {
        log.debug("Request to get list of github repositories of: {} ", username);
        //todo 1. pobrać z api listę repo wg usera odfiltrowaną fork = false;
        List<GitHubRepoDTO> listOfReposDTO = getListOfUserRepos(username);
        List<GitHubRepoDTO> filteredReposDTOS = findNoForkRepos(listOfReposDTO);
        //todo 2. pobrać z api listę branchy (zbudować uri do pobrania branchy - potrzebna będzie lista nazw
        // repozytoriów)
        Map<String, List<BranchDTO>> branchDTOList = getMapOfBranchesFromUserRepos(filteredReposDTOS, username);

        //todo 3. zmapować pola z pierwszej i drugiej listy wg wytycznych i zbudować obiekt do zwrotu
        return null;
    }

    private Map<String, List<BranchDTO>> getMapOfBranchesFromUserRepos(List<GitHubRepoDTO> filteredReposDTOS,
                                                                       String userName) {
//        Map<String, List<BranchDTO>> result = new HashMap<>();
//        filteredReposDTOS.forEach(filteredReposDTO -> {
//            List<BranchDTO> listOfBranches = get(URI.create(uriCreatorForUserGitRepoBranches(userName,
//                    filteredReposDTO.getName())), BranchDTO.class);
//            result.put(filteredReposDTO.getName(), listOfBranches);
//        });
//        return result;
        return null;
    }

    private List<GitHubRepoDTO> findNoForkRepos(List<GitHubRepoDTO> listOfReposDTO) {
        return listOfReposDTO.stream().filter(gitHubRepoDTO -> !gitHubRepoDTO.isFork()).toList();
    }

    private List<GitHubRepoDTO> getListOfUserRepos(String userName) {
        final Object[] gitHubRepos = getUserRepos(userName);
        List<GitHubRepoDTO> repos = mapObjectsFromApi(gitHubRepos);
        return mapLoginFromOwner(repos);
    }

    private List<GitHubRepoDTO> mapLoginFromOwner(List<GitHubRepoDTO> repos) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<GitHubRepoDTO> result = new ArrayList<>();
        repos.forEach(gitHubRepoDTO -> {
            String ownerJson = objectMapper.convertValue(gitHubRepoDTO.getOwner().toString(), String.class);
            OwnerDTO ownerDTO = mapLoginFromOwnerJson(ownerJson);
            result.add(gitHubRepoDTO.toBuilder().login(ownerDTO.getLogin()).build());
        });
        return result;
    }

    private OwnerDTO mapLoginFromOwnerJson(String ownerJson) {
        OwnerDTO ownerDTO = null;
        try {
            ownerDTO = objectMapper.readValue(ownerJson, OwnerDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ownerDTO;
    }

    private List<GitHubRepoDTO> mapObjectsFromApi(Object[] gitHubRepos) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return Arrays.stream(gitHubRepos).map(object -> objectMapper.convertValue(object, GitHubRepoDTO.class))
                .collect(Collectors.toList());
    }


    private Object[] getUserRepos(String userName) {
        return webClient.get()
                .uri(uriCreatorForUserGitHubRepos(userName))
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .bodyToMono(Object[].class)
                .block();
    }

    private String uriCreatorForUserGitHubRepos(String userName) {
        return baseUrl + "/" + userName + REPOS;
    }

    private String uriCreatorForUserGitRepoBranches(String userName, String repoName) {
        return baseUrl + REPOS + "/" + userName + "/" + repoName + BRANCHES;
    }

    private Mono<Throwable> handleError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class).flatMap(body -> {
            log.debug("Not found");
            return Mono.error(new UserNotFoundException("Not found"));
        });
    }

}
