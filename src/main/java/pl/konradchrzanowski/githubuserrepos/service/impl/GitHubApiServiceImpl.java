package pl.konradchrzanowski.githubuserrepos.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.konradchrzanowski.githubuserrepos.exception.ReposNotFoundException;
import pl.konradchrzanowski.githubuserrepos.exception.UserNotFoundException;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;
import pl.konradchrzanowski.githubuserrepos.payload.GitHubReposApiResponse;
import pl.konradchrzanowski.githubuserrepos.service.GitHubApiService;
import pl.konradchrzanowski.githubuserrepos.service.dto.BranchDTO;
import pl.konradchrzanowski.githubuserrepos.service.dto.GitHubRepoDTO;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

@Service
public class GitHubApiServiceImpl implements GitHubApiService {

    private final Logger log = LoggerFactory.getLogger(GitHubApiServiceImpl.class);

    private static final String REPOS = "/repos";
    private static final String BRANCHES = "/branches";

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
        List<GitHubRepoDTO> listOfReposDTO = getListOfGitHubDTOS(getListOfUserRepos(username));
        List<GitHubRepoDTO> filteredReposDTOS = findNoForkRepos(listOfReposDTO);
        //todo 2. pobrać z api listę branchy (zbudować uri do pobrania branchy - potrzebna będzie lista nazw
        // repozytoriów)
        Map<String, List<BranchDTO>> branchDTOList = getMapOfBranchesFromUserRepos(filteredReposDTOS, username);

        //todo 3. zmapować pola z pierwszej i drugiej listy wg wytycznych i zbudować obiekt do zwrotu
        return null;
    }

    private Map<String, List<BranchDTO>> getMapOfBranchesFromUserRepos(List<GitHubRepoDTO> filteredReposDTOS,
                                                                       String userName) {
        Map<String, List<BranchDTO>> result = new HashMap<>();
        filteredReposDTOS.forEach(filteredReposDTO -> {
            List<BranchDTO> listOfBranches = get(URI.create(uriCreatorForUserGitRepoBranches(userName,
                    filteredReposDTO.name())), BranchDTO.class);
            result.put(filteredReposDTO.name(), listOfBranches);
        });
        return result;
    }

    private List<GitHubRepoDTO> findNoForkRepos(List<GitHubRepoDTO> listOfReposDTO) {
        return listOfReposDTO.stream().filter(gitHubRepoDTO -> !gitHubRepoDTO.fork()).toList();
    }

    private List<GitHubReposApiResponse> getListOfUserRepos(String userName) {
        final GitHubReposApiResponse[] gitHubRepos = getUserRepos(userName);
        List<GitHubReposApiResponse> gitHubReposApiResponsesList =
                get(URI.create(uriCreatorForUserGitHubRepos(userName)),
                        GitHubReposApiResponse.class);
        if (gitHubRepos == null) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(gitHubRepos).toList();
        }
    }

    private List<GitHubRepoDTO> getListOfGitHubDTOS(List<GitHubReposApiResponse> reposResponses) {
        GitHubReposApiResponse first = reposResponses.stream().findFirst()
                .orElseThrow(() -> new ReposNotFoundException("Repos" +
                        " not found"));
        return first.getRepos();
    }

    private GitHubReposApiResponse[] getUserRepos(String userName) {
        return webClient.get()
                .uri(uriCreatorForUserGitHubRepos(userName))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .bodyToMono(GitHubReposApiResponse[].class)
                .block();
    }


    private <T> List<T> get(URI uri, Class<T> responseType) {
        return webClient.get().uri(uri)
                .header("Accept: application/json")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .bodyToFlux(responseType)
                .collectList().block();
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
