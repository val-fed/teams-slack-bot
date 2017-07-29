package ua.com.juja.microservices.teams.slackbot.dao.impl;

import net.javacrumbs.jsonunit.core.util.ResourceUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ua.com.juja.microservices.teams.slackbot.dao.UserRepository;
import ua.com.juja.microservices.teams.slackbot.model.UserDTO;
import ua.com.juja.microservices.teams.slackbot.util.Utils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


/**
 * @author Ivan Shapovalov
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestUserRepositoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Inject
    private UserRepository userRepository;
    @Inject
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    @Value("${user.baseURL}")
    private String userUrlBase;
    @Value("${endpoint.userSearchBySlackName}")
    private String userUrlFindUsersBySlackNames;
    @Value("${endpoint.userSearchByUuids}")
    private String userUrlFindUsersByUuids;

    @Before
    public void setup() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void findUsersBySlackNamesReturnUserDTOCorrectly() throws IOException {

        //given
        List<String> slackNames = new ArrayList<>();
        slackNames.add("@user1");
        slackNames.add("@user2");
        slackNames.add("@user3");
        slackNames.add("@user4");
        final int[] number = {1};
        List<UserDTO> expected = slackNames.stream().map(slackName -> new UserDTO(String.valueOf(number[0]++), slackName))
                .collect(Collectors.toList());

        String jsonContentRequest = Utils.convertToString(ResourceUtils.resource
                ("datasets/requestUserRepositoryGetUsersBySlacknames.json"));

        String jsonContentExpectedResponse = Utils.convertToString(
                resource("datasets/responseUserRepositoryGetUsersBySlacknames.json"));
        mockServer.expect(requestTo(userUrlBase + userUrlFindUsersBySlackNames))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().string(jsonContentRequest))
                .andRespond(withSuccess(jsonContentExpectedResponse, MediaType.APPLICATION_JSON_UTF8));
        //when
        List<UserDTO> result = userRepository.findUsersBySlackNames(slackNames);
        // then
        mockServer.verify();
        assertThat(expected, is(result));
    }

    @Test
    public void findUsersByUuidsReturnUserDTOCorrectly() throws IOException {

        //given
        List<String> uuids = new ArrayList<>();
        uuids.add("1");
        uuids.add("2");
        uuids.add("3");
        uuids.add("4");
        List<UserDTO> expected = uuids.stream().map(uuid -> new UserDTO(uuid, "@user" + uuid))
                .collect(Collectors.toList());

        String jsonContentRequest = Utils.convertToString(ResourceUtils.resource
                ("datasets/requestUserRepositoryGetUsersByUuids.json"));

        String jsonContentExpectedResponse = Utils.convertToString(
                resource("datasets/responseUserRepositoryGetUsersByUuids.json"));
        mockServer.expect(requestTo(userUrlBase + userUrlFindUsersByUuids))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().string(jsonContentRequest))
                .andRespond(withSuccess(jsonContentExpectedResponse, MediaType.APPLICATION_JSON_UTF8));
        //when
        List<UserDTO> result = userRepository.findUsersByUuids(uuids);
        // then
        mockServer.verify();
        assertThat(expected, is(result));
    }
}