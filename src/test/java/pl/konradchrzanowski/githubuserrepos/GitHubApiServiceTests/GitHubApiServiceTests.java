package pl.konradchrzanowski.githubuserrepos.GitHubApiServiceTests;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerRequest;
import pl.konradchrzanowski.githubuserrepos.payload.ConsumerResponse;
import pl.konradchrzanowski.githubuserrepos.service.dto.BranchDTO;
import pl.konradchrzanowski.githubuserrepos.service.impl.GitHubApiServiceImpl;
import pl.konradchrzanowski.githubuserrepos.utils.MockedResponses;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class GitHubApiServiceTests {

    public static MockWebServer mockGitHubApi;
    @Autowired
    public ObjectMapper objectMapper;
    @Autowired
    public WebClient.Builder webClientBuilder;


    public static GitHubApiServiceImpl gitHubApiService;

    @BeforeAll
    static void setUp() throws IOException {
        mockGitHubApi = new MockWebServer();
        mockGitHubApi.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockGitHubApi.shutdown();
    }

    @BeforeEach
    void init() {
        String baseUrl = String.format("http://localhost:%s/users", mockGitHubApi.getPort());
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
        gitHubApiService = new GitHubApiServiceImpl(objectMapper, webClient);
    }

    @Test
    public void getCorrectResponseWebClient() {
        //given
        String ownerLogin = "chrzanekk";
        String branchName = "master";
        String sha = "68e38d691fd9fd0a6e9d9665adb5f8a3eafd41ed";
        MockResponse reposResponse = new MockResponse().setBody(MockedResponses.MOCKED_REPOS)
                .addHeader("Content-Type", "application/json");
        MockResponse branchesResponse = new MockResponse().setBody(MockedResponses.MOCKED_BRANCHES)
                .addHeader("Content-Type", "application/json");
        mockGitHubApi.enqueue(reposResponse);
        mockGitHubApi.enqueue(branchesResponse);
        ConsumerRequest consumerRequest = ConsumerRequest.builder().username(ownerLogin).build();

        //when
        List<ConsumerResponse> responses = gitHubApiService.getGithubRepo(consumerRequest.getUsername());

        //then
        ConsumerResponse response = responses.get(0);
        assertThat(response.getName()).isEqualTo("BoardGames");
        assertThat(response.getOwnerLogin()).isEqualTo(ownerLogin);
        assertThat(response.getBranches()).hasSize(1);
        List<BranchDTO> branchDTOList = response.getBranches();
        BranchDTO branchDTO = branchDTOList.get(0);
        assertThat(branchDTO.getName()).isEqualTo(branchName);
        assertThat(branchDTO.getCommitSha()).isEqualTo(sha);
    }
}
