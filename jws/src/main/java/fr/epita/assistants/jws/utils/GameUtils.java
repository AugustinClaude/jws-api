package fr.epita.assistants.jws.utils;

import fr.epita.assistants.jws.data.repository.GameRepository;
import fr.epita.assistants.jws.data.repository.PlayerRepository;
import fr.epita.assistants.jws.presentation.rest.response.GameDetailResponse;
import fr.epita.assistants.jws.presentation.rest.response.GameListResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GameUtils {
    @Inject
    GameRepository gameRepository;
    @Inject
    PlayerRepository playerRepository;

    public GameDetailResponse setupDetailResponse(Long gameId) {
        GameDetailResponse gameDetailResponse = new GameDetailResponse();

        gameDetailResponse.startTime = gameRepository.findById(gameId).starttime;
        gameDetailResponse.state = GameStateUtils.valueOf(gameRepository.findById(gameId).state);
        gameDetailResponse.map = gameRepository.findById(gameId).map;
        gameDetailResponse.id = gameRepository.findById(gameId).id;

        playerRepository.listAll().forEach(p -> {
            if (p.gameModel.id.equals(gameId)) {
                PlayerUtils playerUtils = new PlayerUtils(p.id, p.name, p.lives, p.posx, p.posy);
                if (!gameDetailResponse.players.contains(playerUtils))
                    gameDetailResponse.players.add(playerUtils);
            }
        });

        return gameDetailResponse;
    }

    public GameListResponse setupListResponse(Long gameId) {
        GameListResponse gameListResponse = new GameListResponse();

        gameListResponse.id = gameRepository.findById(gameId).id;
        gameListResponse.state = GameStateUtils.valueOf(gameRepository.findById(gameId).state);

        playerRepository.listAll().forEach(p -> {
            if (p.gameModel.id.equals(gameId))
                gameListResponse.players++;
        });

        return gameListResponse;
    }
}
