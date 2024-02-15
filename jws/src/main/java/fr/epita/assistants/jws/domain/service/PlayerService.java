package fr.epita.assistants.jws.domain.service;

import fr.epita.assistants.jws.converter.GameConverter;
import fr.epita.assistants.jws.converter.PlayerConverter;
import fr.epita.assistants.jws.data.model.GameModel;
import fr.epita.assistants.jws.data.model.PlayerModel;
import fr.epita.assistants.jws.data.repository.GameRepository;
import fr.epita.assistants.jws.data.repository.PlayerRepository;
import fr.epita.assistants.jws.domain.entity.GameEntity;
import fr.epita.assistants.jws.domain.entity.PlayerEntity;
import fr.epita.assistants.jws.utils.GameStateUtils;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PlayerService {
    @Inject
    GameRepository gameRepository;
    @Inject
    PlayerRepository playerRepository;
    @Inject
    GameService gameService;

    @ConfigProperty(name = "JWS_TICK_DURATION") Long tickDuration;
    @ConfigProperty(name = "JWS_DELAY_MOVEMENT") int delayMovement;
    @ConfigProperty(name = "JWS_DELAY_BOMB") int delayBomb;

    @Getter
    List<PlayerEntity> playerEntities = new ArrayList<>();

    @Transactional
    public PlayerEntity addPlayer(String name, GameEntity gameEntity) {
        GameModel gameModel = gameRepository.findById(gameEntity.getId());
        PlayerModel playerModel = new PlayerModel(name, gameModel);

        if (gameEntity.getPlayerEntities().isEmpty()) {
            playerModel.posx = 1;
            playerModel.posy = 1;
        }
        else if (gameEntity.getPlayerEntities().size() == 1) {
            playerModel.posx = 15;
            playerModel.posy = 1;
        }
        else if (gameEntity.getPlayerEntities().size() == 2) {
            playerModel.posx = 15;
            playerModel.posy = 13;
        }
        else if (gameEntity.getPlayerEntities().size() == 3) {
            playerModel.posx = 1;
            playerModel.posy = 13;
        }

        playerRepository.persist(playerModel);

        PlayerConverter playerConverter = new PlayerConverter(playerModel);
        PlayerEntity playerEntity = playerConverter.convertToPlayerEntity();
        playerEntities.add(playerEntity);

        return playerEntity;
    }

    public PlayerEntity getPlayerById(Long playerId) {
        PlayerModel playerModel = playerRepository.findById(playerId);
        if (playerModel == null)
            return null;

        PlayerConverter playerConverter = new PlayerConverter(playerModel);
        return playerConverter.convertToPlayerEntity();
    }

    private void removeLife(Long playerId) {
        PlayerModel playerModel = playerRepository.findById(playerId);
        if (playerModel == null)
            return;

        playerModel.lives--;
        if (playerModel.lives <= 0)
            playerModel.lives = 0;
    }

    private boolean canMove(PlayerModel playerModel, List<String> map, int newX, int newY) {
        if (newY < 0 || newY >= 15 || newX < 0 || newX >= 17)
            return false;

        char c = map.get(newY).charAt(newX);
        if (c != 'G')
            return false;

        int diffX = newX - playerModel.posx;
        int diffY = newY - playerModel.posy;
        if (Math.abs(diffX) == 1 && diffY == 0)
            return true;
        return diffX == 0 && Math.abs(diffY) == 1;
    }

    @Transactional
    public boolean move(Long playerId, List<String> map, int newX, int newY) {
        PlayerModel playerModel = playerRepository.findById(playerId);
        if (playerModel == null)
            return false;

        long delayMovementMillis = delayMovement * tickDuration;
        long lastMovement = playerModel.lastmovement == null ? delayMovementMillis : Duration.between(playerModel.lastmovement.toInstant(), Instant.now()).toMillis();
        if (lastMovement < delayMovementMillis)
            throw new WebApplicationException(Response.Status.TOO_MANY_REQUESTS);

        if (!canMove(playerModel, map, newX, newY))
            return false;

        playerModel.posx = newX;
        playerModel.posy = newY;
        playerModel.lastmovement = Timestamp.from(Instant.now());
        return true;
    }

    @Transactional
    public void explodeBomb(Long gameId, List<String> map, int posX, int posY) {
        if (posY > 0 && map.get(posY - 1).charAt(posX) == 'W')
            gameService.placeOnMap(gameId, 'G', posX, posY - 1);
        if (posY < 14 && map.get(posY + 1).charAt(posX) == 'W')
            gameService.placeOnMap(gameId, 'G', posX, posY + 1);
        if (posX > 0 && map.get(posY).charAt(posX - 1) == 'W')
            gameService.placeOnMap(gameId, 'G', posX - 1, posY);
        if (posX < 16 && map.get(posY).charAt(posX + 1) == 'W')
            gameService.placeOnMap(gameId, 'G', posX + 1, posY);
        gameService.placeOnMap(gameId, 'G', posX, posY);

        for (PlayerModel player : playerRepository.listAll()) {
            if (player.gameModel.id.equals(gameId)) {
                if (player.posx.equals(posX) && player.posy.equals(posY))
                    removeLife(player.id);
                else if (player.posx.equals(posX) && player.posy.equals(posY - 1))
                    removeLife(player.id);
                else if (player.posx.equals(posX) && player.posy.equals(posY + 1))
                    removeLife(player.id);
                else if (player.posx.equals(posX - 1) && player.posy.equals(posY))
                    removeLife(player.id);
                else if (player.posx.equals(posX + 1) && player.posy.equals(posY))
                    removeLife(player.id);
            }
        }

        int aliveCounter = 0;
        for (PlayerModel player : playerRepository.listAll()) {
            if (player.gameModel.id.equals(gameId) && player.lives > 0)
                aliveCounter++;
        }

        if (aliveCounter <= 1)
            gameService.updateState(gameId, GameStateUtils.FINISHED);
    }

    @Transactional
    public void placeBomb(Long playerId, Long gameId) {
        PlayerModel playerModel = playerRepository.findById(playerId);
        if (playerModel == null)
            return;

        GameModel gameModel = gameRepository.findById(gameId);
        if (gameModel == null)
            return;

        long delayBombMillis = delayBomb * tickDuration;
        long lastBomb = playerModel.lastbomb == null ? delayBombMillis : Duration.between(playerModel.lastbomb.toInstant(), Instant.now()).toMillis();
        if (lastBomb < delayBombMillis)
            throw new WebApplicationException(Response.Status.TOO_MANY_REQUESTS);

        // Place bomb on map
        int posX = playerModel.posx;
        int posY = playerModel.posy;
        gameService.placeOnMap(gameId, 'B', posX, posY);
        playerModel.lastbomb = Timestamp.from(Instant.now());
    }
}
