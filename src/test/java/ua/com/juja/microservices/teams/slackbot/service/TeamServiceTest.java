package ua.com.juja.microservices.teams.slackbot.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ua.com.juja.microservices.teams.slackbot.exceptions.TeamExchangeException;
import ua.com.juja.microservices.teams.slackbot.exceptions.UserExchangeException;
import ua.com.juja.microservices.teams.slackbot.exceptions.WrongCommandFormatException;
import ua.com.juja.microservices.teams.slackbot.model.Team;
import ua.com.juja.microservices.teams.slackbot.model.TeamRequest;
import ua.com.juja.microservices.teams.slackbot.model.User;
import ua.com.juja.microservices.teams.slackbot.repository.TeamRepository;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Ivan Shapovalov
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TeamServiceTest {
    private static final int TEAM_SIZE = 4;

    @Rule
    final public ExpectedException expectedException = ExpectedException.none();
    private final User user1 = new User("uuid1", "@slack1");
    private final User user2 = new User("uuid2", "@slack2");
    private final User user3 = new User("uuid3", "@slack3");
    private final User user4 = new User("uuid4", "@slack4");
    @MockBean
    private TeamRepository teamRepository;
    @MockBean
    private UserService userService;
    @Inject
    private TeamService teamService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void activateTeamIfMembersSizeEqualsFourExecutedCorrectly() {
        String text = "@slack1 @slack2 @slack3 @slack4";
        Set<String> uuids = new LinkedHashSet<>(Arrays.asList(user1.getUuid(), user2.getUuid(), user3.getUuid(),
                user4.getUuid()));
        List<User> users = Arrays.asList(user1, user2, user3, user4);
        Team expected = new Team(uuids);
        when(userService.findUsersBySlackNames(anyListOf(String.class))).thenReturn(users);
        given(teamRepository.activateTeam(any(TeamRequest.class))).willReturn(expected);

        Team actual = teamService.activateTeam(text);

        assertEquals(expected, actual);
        verify(userService).findUsersBySlackNames(anyListOf(String.class));
        ArgumentCaptor<TeamRequest> captor = ArgumentCaptor.forClass(TeamRequest.class);
        verify(teamRepository).activateTeam(captor.capture());
        assertTrue(captor.getValue().getMembers().equals(uuids));
        verifyNoMoreInteractions(userService, teamRepository);
    }

    @Test
    public void activateTeamIfUserServiceFindUsersBySlackNamesReturnsWrongUsersCountThrowsException() {
        String text = "@slack1 @slack2 @slack3 @slack4 ";
        List<String> slackNamesInText = Collections.singletonList(user1.getSlack());
        List<User> users = Arrays.asList(user1, user2);
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(users);
        expectedException.expect(UserExchangeException.class);
        expectedException.expectMessage("Users count is not equals in request and response from Users Service");

        teamService.activateTeam(text);

        verify(userService).findUsersBySlackNames(slackNamesInText);
        verifyNoMoreInteractions(teamRepository, userService);
    }

    @Test
    public void activateTeamIfMembersSizeNotEqualsFourThrowsException() {
        String text = "@slack1 @slack2 @slack3";
        Set<String> members = new LinkedHashSet<>(Arrays.asList(user1.getUuid(), user2.getUuid(), user3.getUuid()));

        expectedException.expect(WrongCommandFormatException.class);
        expectedException.expectMessage(String.format("We found %d slack names in your command." +
                " But size of the team must be %s.", members.size(), TEAM_SIZE));

        teamService.activateTeam(text);

    }

    @Test
    public void activateTeamIfMembersOfRequestAndResponseNotEqualsThrowsException() {
        String text = "@slack1 @slack2 @slack3 @slack4";
        Set<String> responseMembers = new LinkedHashSet<>(Arrays.asList(user1.getUuid(), user2.getUuid(),
                user3.getUuid(), "uuid5"));
        List<User> users = Arrays.asList(user1, user2, user3, user4);
        when(userService.findUsersBySlackNames(anyListOf(String.class))).thenReturn(users);
        Team activatedTeam = new Team(responseMembers);
        given(teamRepository.activateTeam(any(TeamRequest.class))).willReturn(activatedTeam);

        expectedException.expect(TeamExchangeException.class);
        expectedException.expectMessage("Team members is not equals in request and response from Teams Service");

        teamService.activateTeam(text);

        verify(userService).findUsersBySlackNames(anyListOf(String.class));
        verify(teamRepository).activateTeam(any(TeamRequest.class));
        verifyNoMoreInteractions(userService, teamRepository);
    }

    @Test
    public void activateTeamIfTextIsNullThrowsException() {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Text must not be null!");

        teamService.activateTeam(null);
        verifyNoMoreInteractions(userService, teamRepository);
    }

    @Test
    public void getTeamIfOneSlackNameInTextExecutedCorrectly() {
        String text = "@slack1";
        List<String> slackNamesInText = Collections.singletonList(text);
        List<User> users = Collections.singletonList(user1);
        List<User> teamUsers = Arrays.asList(user1, user2, user3, user4);
        Set<String> uuids= teamUsers.stream().map(User::getUuid).collect(Collectors.toSet());
        Set<String> expected = new LinkedHashSet<>(Arrays.asList(user1.getSlack(), user2.getSlack(), user3.getSlack(),
                user4.getSlack()));
        Team team = new Team(uuids);
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(users);
        given(teamRepository.getTeam(user1.getUuid())).willReturn(team);
        given(userService.findUsersByUuids(anyListOf(String.class))).willReturn(teamUsers);

        Set<String> actual = teamService.getTeam(text);

        assertEquals(expected, actual);
        verify(userService).findUsersBySlackNames(slackNamesInText);
        verify(teamRepository).getTeam(user1.getUuid());
        verify(userService).findUsersByUuids(anyListOf(String.class));
        verifyNoMoreInteractions(teamRepository, userService);
    }

    @Test
    public void getTeamIfMoreThanOneSlackNameInTextThrowsException() {
        String text = "@slack1 @slack2";
        List<String> slackNamesInText = Arrays.asList(user1.getSlack(), user2.getSlack());
        List<User> users = Arrays.asList(user1, user2);
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(users);
        expectedException.expect(WrongCommandFormatException.class);
        expectedException.expectMessage(String.format("We found %d slack names in your command." +
                " But expect one slack name.", users.size()));

        teamService.getTeam(text);

        verify(userService).findUsersBySlackNames(slackNamesInText);
        verifyNoMoreInteractions(teamRepository, userService);
    }

    @Test
    public void getTeamIfZeroSlackNameInTextThrowsException() {
        String text = "slack1 slack2";
        List<String> slackNamesInText = Collections.emptyList();
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(Collections.emptyList());
        expectedException.expect(WrongCommandFormatException.class);
        expectedException.expectMessage(String.format("We found %d slack names in your command." +
                " But expect one slack name.", slackNamesInText.size()));

        teamService.getTeam(text);

        verify(userService).findUsersBySlackNames(slackNamesInText);
        verifyNoMoreInteractions(teamRepository, userService);
    }

    @Test
    public void getTeamIfUserServiceFindUsersBySlackNamesReturnsWrongUsersCountThrowsException() {
        String text = "@slack1";
        List<String> slackNamesInText = Collections.singletonList(user1.getSlack());
        List<User> users = Arrays.asList(user1, user2);
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(users);
        expectedException.expect(UserExchangeException.class);
        expectedException.expectMessage("Users count is not equals in request and response from Users Service");

        teamService.getTeam(text);

        verify(userService).findUsersBySlackNames(slackNamesInText);
        verifyNoMoreInteractions(teamRepository, userService);
    }

    @Test
    public void getTeamIfUserServiceFindUsersByUuidsReturnsWrongUsersCountThrowsException() {
        String text = "@slack1";
        List<String> slackNamesInText = Collections.singletonList(user1.getSlack());
        List<User> users = Collections.singletonList(user1);
        List<User> teamUsers = Arrays.asList(user1, user2, user3);
        Team team = new Team(new HashSet<>());
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(users);
        given(teamRepository.getTeam(user1.getUuid())).willReturn(team);
        given(userService.findUsersByUuids(anyListOf(String.class))).willReturn(teamUsers);

        expectedException.expect(UserExchangeException.class);
        expectedException.expectMessage("Users count is not equals in request and response from Users Service");

        teamService.getTeam(text);

        verify(userService).findUsersBySlackNames(slackNamesInText);
        verify(teamRepository).getTeam(user1.getUuid());
        verify(userService).findUsersByUuids(anyListOf(String.class));
        verifyNoMoreInteractions(teamRepository, userService);
    }

    @Test
    public void deactivateTeamIfOneSlackNameInTextExecutedCorrectly() {
        String text = "@slack1";
        List<String> slackNamesInText = Collections.singletonList(text);
        List<User> users = Collections.singletonList(user1);
        List<User> teamUsers = Arrays.asList(user1, user2, user3, user4);
        Set<String> uuids= teamUsers.stream().map(User::getUuid).collect(Collectors.toSet());
        Set<String> expected = new LinkedHashSet<>(Arrays.asList(user1.getSlack(),
                user2.getSlack(), user3.getSlack(), user4.getSlack()));
        Team team = new Team(uuids);
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(users);
        given(teamRepository.deactivateTeam(user1.getUuid())).willReturn(team);
        given(userService.findUsersByUuids(anyListOf(String.class))).willReturn(teamUsers);

        Set<String> actual = teamService.deactivateTeam(text);

        assertThat(actual, is(expected));
        verify(userService).findUsersBySlackNames(slackNamesInText);
        verify(teamRepository).deactivateTeam(user1.getUuid());
        verify(userService).findUsersByUuids(anyListOf(String.class));
        verifyNoMoreInteractions(teamRepository, userService);
    }

    @Test
    public void deactivateTeamIfSeveralSlackNamesInTextThrowsException() {
        String text = "@slack1 @slack2";
        expectedException.expect(WrongCommandFormatException.class);
        expectedException.expectMessage(String.format("We found %d slack names in your command. " +
                "But expect one slack name.", 2));

        teamService.deactivateTeam(text);

        verifyNoMoreInteractions(teamService);
    }

    @Test
    public void deactivateTeamIfUserServiceFindUsersBySlackNamesReturnsWrongUsersCountThrowsException() {
        String text = "@slack1";
        List<String> slackNamesInText = Collections.singletonList(user1.getSlack());
        List<User> users = Arrays.asList(user1, user2);
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(users);
        expectedException.expect(UserExchangeException.class);
        expectedException.expectMessage("Users count is not equals in request and response from Users Service");

        teamService.deactivateTeam(text);

        verify(userService).findUsersBySlackNames(slackNamesInText);
        verifyNoMoreInteractions(teamRepository, userService);
    }

    @Test
    public void deactivateTeamIfUserServiceFindUsersByUuidsReturnsWrongUsersCountThrowsException() {
        String text = "@slack1";
        List<String> slackNamesInText = Collections.singletonList(user1.getSlack());
        List<User> users = Collections.singletonList(user1);
        List<User> teamUsers = Arrays.asList(user1, user2, user3);
        Team team = new Team(new HashSet<>());
        given(userService.findUsersBySlackNames(slackNamesInText)).willReturn(users);
        given(teamRepository.deactivateTeam(user1.getUuid())).willReturn(team);
        given(userService.findUsersByUuids(anyListOf(String.class))).willReturn(teamUsers);

        expectedException.expect(UserExchangeException.class);
        expectedException.expectMessage("Users count is not equals in request and response from Users Service");

        teamService.deactivateTeam(text);

        verify(userService).findUsersBySlackNames(slackNamesInText);
        verify(teamRepository).deactivateTeam(user1.getUuid());
        verify(userService).findUsersByUuids(anyListOf(String.class));
        verifyNoMoreInteractions(teamRepository, userService);
    }

}