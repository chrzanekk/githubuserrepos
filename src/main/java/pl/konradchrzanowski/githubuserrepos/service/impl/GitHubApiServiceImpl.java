package pl.konradchrzanowski.githubuserrepos.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import pl.konradchrzanowski.githubuserrepos.service.dto.CommitDTO;
import pl.konradchrzanowski.githubuserrepos.service.dto.GitHubRepoDTO;
import pl.konradchrzanowski.githubuserrepos.service.dto.OwnerDTO;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitHubApiServiceImpl implements GitHubApiService {

    private final Logger log = LoggerFactory.getLogger(GitHubApiServiceImpl.class);

    private static final String REPOS = "/repos";
    private final ObjectMapper objectMapper;

    private final WebClient webClient;

    public GitHubApiServiceImpl(ObjectMapper objectMapper, WebClient webClient) {
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }

    @Override
    public List<ConsumerResponse> getGithubRepo(String username) {
        log.debug("Request to get list of github repositories of: {} ", username);
        List<GitHubRepoDTO> listOfReposDTO = getListOfUserRepos(username);
        List<GitHubRepoDTO> filteredReposDTOS = findNoForkRepos(listOfReposDTO);
        Map<String, List<BranchDTO>> branchDTOList = getMapOfUserRepoBranches(filteredReposDTOS);
        List<ConsumerResponse> prepareResponse = prepareResponseForClient(filteredReposDTOS, branchDTOList);
        return prepareResponse;
    }

    private List<ConsumerResponse> prepareResponseForClient(List<GitHubRepoDTO> filteredReposDTOS,
                                                            Map<String, List<BranchDTO>> branchDTOMap) {
        List<ConsumerResponse> result = new ArrayList<>();
        filteredReposDTOS.forEach(gitHubRepoDTO -> {
            ConsumerResponse response = ConsumerResponse.builder()
                    .name(gitHubRepoDTO.getName())
                    .ownerLogin(gitHubRepoDTO.getLogin())
                    .branches(mapBranchesForGivenRepo(gitHubRepoDTO.getName(), branchDTOMap)).build();
            result.add(response);
        });
        return result;
    }

    private List<BranchDTO> mapBranchesForGivenRepo(String name, Map<String, List<BranchDTO>> stringListMap) {
        return stringListMap.entrySet().stream()
                .filter(entry -> name.equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().orElse(Collections.emptyList());
    }

    private List<GitHubRepoDTO> getListOfUserRepos(String userName) {
        final Object[] gitHubRepos = getUserReposFromApi(userName);
        List<GitHubRepoDTO> repos = mapObjectsFromApi(gitHubRepos);
        return mapLoginFromOwner(repos);
    }

    private Object[] getUserReposFromApi(String userName) {
        return webClient.get()
                .uri(uriCreatorForUserGitHubRepos(userName))
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .bodyToMono(Object[].class)
                .block();
    }

    private List<GitHubRepoDTO> mapObjectsFromApi(Object[] gitHubRepos) {
        return Arrays.stream(gitHubRepos).map(object -> objectMapper.convertValue(object, GitHubRepoDTO.class))
                .collect(Collectors.toList());
    }

    private List<GitHubRepoDTO> mapLoginFromOwner(List<GitHubRepoDTO> repos) {
        List<GitHubRepoDTO> result = new ArrayList<>();
        repos.forEach(gitHubRepoDTO -> {
            String ownerJson = objectMapper.convertValue(gitHubRepoDTO.getOwner().toString(), String.class);
            OwnerDTO ownerDTO = mapLoginFromOwnerJson(ownerJson);
            result.add(gitHubRepoDTO.toBuilder().login(ownerDTO.getLogin()).build());
        });
        return result;
    }

    private OwnerDTO mapLoginFromOwnerJson(String ownerJson) {
        OwnerDTO ownerDTO;
        try {
            ownerDTO = objectMapper.readValue(ownerJson, OwnerDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ownerDTO;
    }

    private List<GitHubRepoDTO> findNoForkRepos(List<GitHubRepoDTO> listOfReposDTO) {
        return listOfReposDTO.stream().filter(gitHubRepoDTO -> !gitHubRepoDTO.isFork()).toList();
    }


    private Map<String, List<BranchDTO>> getMapOfUserRepoBranches(List<GitHubRepoDTO> filteredReposDTOS) {
        Map<String, List<BranchDTO>> result = new HashMap<>();
        filteredReposDTOS.forEach(gitHubRepoDTO -> {
            List<BranchDTO> repoBranches = getListOfRepoBranchesNamesWithLastCommitSha(gitHubRepoDTO.getBranchesUrl());
            result.put(gitHubRepoDTO.getName(), repoBranches);
        });
        return result;
    }

    private List<BranchDTO> getListOfRepoBranchesNamesWithLastCommitSha(String branchesUrl) {
        int indexOfElementToCutFromString = branchesUrl.indexOf('{');
        String trimmedUrl = branchesUrl.substring(0, indexOfElementToCutFromString);
        Object[] repoBranches = getRepoBranchesFromApi(trimmedUrl);
        List<BranchDTO> mappedBranchesFromApi = mapRepoBranchesFromApi(repoBranches);
        return mapCommitShaFromBranches(mappedBranchesFromApi);
    }

    private Object[] getRepoBranchesFromApi(String branchesUrl) {
        return webClient.get()
                .uri(branchesUrl)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .bodyToMono(Object[].class)
                .block();
    }

    private List<BranchDTO> mapRepoBranchesFromApi(Object[] repoBranches) {
        return Arrays.stream(repoBranches).map(object -> objectMapper.convertValue(object, BranchDTO.class))
                .collect(Collectors.toList());
    }

    private List<BranchDTO> mapCommitShaFromBranches(List<BranchDTO> mappedBranchesFromApi) {
        List<BranchDTO> result = new ArrayList<>();
        mappedBranchesFromApi.forEach(branchDTO -> {
            String commitJson = objectMapper.convertValue(branchDTO.getCommit().toString(), String.class);
            CommitDTO commitDTO = mapShaFromCommitJson(commitJson);
            result.add(branchDTO.toBuilder().commitSha(commitDTO.getCommitSha()).commit(null).build());
        });
        return result;
    }

    private CommitDTO mapShaFromCommitJson(String commitJson) {
        CommitDTO commitDTO;
        try {
            commitDTO = objectMapper.readValue(commitJson, CommitDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return commitDTO;
    }


    private String uriCreatorForUserGitHubRepos(String userName) {
        return "/" + userName + REPOS;
    }

    private Mono<Throwable> handleError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class).flatMap(body -> {
            log.debug("Not found");
            return Mono.error(new UserNotFoundException("Not found"));
        });
    }

}
