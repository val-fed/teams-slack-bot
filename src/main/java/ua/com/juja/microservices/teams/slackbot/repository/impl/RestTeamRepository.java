package ua.com.juja.microservices.teams.slackbot.repository.impl;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ua.com.juja.microservices.teams.slackbot.exceptions.ApiError;
import ua.com.juja.microservices.teams.slackbot.exceptions.TeamExchangeException;
import ua.com.juja.microservices.teams.slackbot.model.teams.ActivateTeamRequest;
import ua.com.juja.microservices.teams.slackbot.model.teams.DeactivateTeamRequest;
import ua.com.juja.microservices.teams.slackbot.model.teams.Team;
import ua.com.juja.microservices.teams.slackbot.repository.TeamRepository;
import ua.com.juja.microservices.teams.slackbot.repository.feign.TeamsClient;
import ua.com.juja.microservices.teams.slackbot.util.Utils;

import javax.inject.Inject;

/**
 * @author Ivan Shapovalov
 */
@Repository
@Slf4j
public class RestTeamRepository implements TeamRepository {
    @Inject
    private TeamsClient teamsClient;

    @Override
    public Team activateTeam(ActivateTeamRequest activateTeamRequest) {
        Team activatedTeam;
        try {
            activatedTeam = teamsClient.activateTeam(activateTeamRequest);
        } catch (FeignException ex) {
            ApiError error = Utils.convertToApiError(ex.getMessage());
            throw new TeamExchangeException(error, ex);
        }
        log.info("Team activated: '{}'", activatedTeam.getId());
        return activatedTeam;
    }

    @Override
    public Team deactivateTeam(DeactivateTeamRequest deactivateTeamRequest) {
        Team deactivatedTeam;
        try {
            deactivatedTeam = teamsClient.deactivateTeam(deactivateTeamRequest);
        } catch (FeignException ex) {
            ApiError error = Utils.convertToApiError(ex.getMessage());
            throw new TeamExchangeException(error, ex);
        }
        log.info("Team deactivated: '{}'", deactivatedTeam.getId());
        return deactivatedTeam;
    }

    @Override
    public Team getTeam(String uuid) {
        Team team;
        try {
            team = teamsClient.getTeam(uuid);
        } catch (FeignException ex) {
            ApiError error = Utils.convertToApiError(ex.getMessage());
            throw new TeamExchangeException(error, ex);
        }
        log.info("Team got: '{}'", team.getId());
        return team;
    }
}
