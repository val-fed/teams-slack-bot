package ua.com.juja.microservices.teams.slackbot.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ua.com.juja.microservices.teams.slackbot.model.User;
import ua.com.juja.microservices.teams.slackbot.repository.UserRepository;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Ivan Shapovalov
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {

    @Inject
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void findUsersBySlackNamesExecutedCorrectly() throws Exception {
        //given
        List<String> slackNamesRequest = Arrays.asList("@user11", "@user22");
        List<User> expected = Arrays.asList(new User("uuid1", "@user11"),
                new User("uuid2", "user2"));
        given(userRepository.findUsersBySlackNames(slackNamesRequest)).willReturn(expected);
        //when
        List<User> actual = userService.findUsersBySlackNames(slackNamesRequest);
        //then
        assertThat(actual, is(expected));
        verify(userRepository).findUsersBySlackNames(slackNamesRequest);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void findUsersByUuidsExecutedCorrectly() throws Exception {
        //given
        List<String> uuidsRequest = Arrays.asList("uuid1", "uuid2");
        List<User> expected = Arrays.asList(new User("uuid1", "@user11"),
                new User("uuid2", "user2"));
        given(userRepository.findUsersByUuids(uuidsRequest)).willReturn(expected);
        //when
        List<User> actual = userService.findUsersByUuids(uuidsRequest);
        //then
        assertThat(actual, is(expected));
        verify(userRepository).findUsersByUuids(uuidsRequest);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void replaceUuidsBySlackNamesInExceptionMessageExecutedCorrectly() throws Exception {
        //given
        String exceptionMessage = String.format("User(s) '#%s#' exist(s) in another teams",
                "uuid1,uuid2,uuid3,uuid4");
        String expected = String.format("User(s) '%s' exist(s) in another teams",
                "@slack1,@slack2,@slack3,@slack4");
        List<User> users = Arrays.asList(new User("uuid1", "@slack1"),
                new User("uuid2", "@slack2"), new User("uuid3", "@slack3"),
                new User("uuid4", "@slack4"));
        given(userRepository.findUsersByUuids(anyListOf(String.class))).willReturn(users);
        //when
        String actual = userService.replaceUuidsBySlackNamesInExceptionMessage(exceptionMessage);
        //then
        assertThat(actual, is(expected));
        verify(userRepository).findUsersByUuids(anyListOf(String.class));
        verifyNoMoreInteractions(userRepository);
    }
}