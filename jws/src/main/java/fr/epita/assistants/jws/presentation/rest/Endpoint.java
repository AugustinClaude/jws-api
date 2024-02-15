package fr.epita.assistants.jws.presentation.rest;

import fr.epita.assistants.jws.domain.entity.GameEntity;
import fr.epita.assistants.jws.domain.entity.PlayerEntity;
import fr.epita.assistants.jws.domain.service.GameService;
import fr.epita.assistants.jws.domain.service.PlayerService;
import fr.epita.assistants.jws.presentation.rest.request.CreateGameRequest;
import fr.epita.assistants.jws.presentation.rest.request.JoinGameRequest;
import fr.epita.assistants.jws.presentation.rest.request.MovePlayerRequest;
import fr.epita.assistants.jws.presentation.rest.request.PutBombRequest;
import fr.epita.assistants.jws.presentation.rest.response.GameDetailResponse;
import fr.epita.assistants.jws.presentation.rest.response.GameListResponse;
import fr.epita.assistants.jws.utils.GameStateUtils;
import fr.epita.assistants.jws.utils.GameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class Endpoint {
    @Inject
    public GameService gameService;
    @Inject
    public PlayerService playerService;
    @Inject
    public GameUtils gameUtils;

    @ConfigProperty(name = "JWS_MAP_PATH", defaultValue = "src/test/resources/map1.rle") String mapPath;
    @ConfigProperty(name = "JWS_TICK_DURATION") Long tickDuration;
    @ConfigProperty(name = "JWS_DELAY_BOMB") int delayBomb;

    @Path("games")
    @GET
    public List<GameListResponse> getGames() {
        List<GameListResponse> gameListResponseList = new ArrayList<>();
        for (GameEntity gameEntity : gameService.getGameEntities()) {
            GameListResponse gameListResponse = gameUtils.setupListResponse(gameEntity.getId());
            gameListResponseList.add(gameListResponse);
        }

        return gameListResponseList;
    }

    @Path("games")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public GameDetailResponse newGame(CreateGameRequest createGameRequest) throws IOException {
        if (createGameRequest == null || createGameRequest.name == null)
            throw new BadRequestException();
        GameEntity gameEntity = gameService.addGame(createGameRequest.name, mapPath);

        return gameUtils.setupDetailResponse(gameEntity.getId());
    }

    @Path("games/{gameId}")
    @GET
    public GameDetailResponse getGameById(@PathParam("gameId") Long gameId) {
        GameEntity gameEntity = gameService.getGameById(gameId);
        if (gameEntity == null)
            throw new NotFoundException();

        return gameUtils.setupDetailResponse(gameEntity.getId());
    }

    @Path("games/{gameId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public GameDetailResponse joinGame(@PathParam("gameId") Long gameId, JoinGameRequest joinGameRequest) {
        if (joinGameRequest == null || joinGameRequest.name == null)
            throw new BadRequestException();

        GameEntity gameEntity = gameService.getGameById(gameId);
        if (gameEntity == null)
            throw new NotFoundException();
        if (gameEntity.getState() != GameStateUtils.STARTING)
            throw new BadRequestException();

        PlayerEntity playerEntity = playerService.addPlayer(joinGameRequest.name, gameEntity);
        gameEntity.addPlayer(playerEntity);

        return gameUtils.setupDetailResponse(gameEntity.getId());
    }

    @Path("games/{gameId}/start")
    @PATCH
    public GameDetailResponse startGame(@PathParam("gameId") Long gameId) {
        gameService.start(gameId);
        return gameUtils.setupDetailResponse(gameId);
    }

    @Path("games/{gameId}/players/{playerId}/bomb")
    @POST
    public GameDetailResponse putBomb(@PathParam("gameId") Long gameId, @PathParam("playerId") Long playerId, PutBombRequest putBombRequest) {
        GameEntity gameEntity = gameService.getGameById(gameId);
        if (gameEntity == null)
            throw new NotFoundException();

        PlayerEntity playerEntity = playerService.getPlayerById(playerId);
        if (playerEntity == null)
            throw new NotFoundException();

        if (putBombRequest == null || putBombRequest.posX == null || putBombRequest.posY == null)
            throw new BadRequestException();

        if (gameEntity.getState() != GameStateUtils.RUNNING || playerEntity.getLives() == 0 || !putBombRequest.posX.equals(playerEntity.getPosX()) || !putBombRequest.posY.equals(playerEntity.getPosY()))
            throw new BadRequestException();

        long delayBombMillis = delayBomb * tickDuration;
        int posX = playerEntity.getPosX();
        int posY = playerEntity.getPosY();
        playerService.placeBomb(playerId, gameId);
        gameEntity = gameService.getGameById(gameId);

        // Wait until bomb explodes
        List<String> map = gameService.decodeMap(gameEntity.getMap());
        CompletableFuture.runAsync(() -> playerService.explodeBomb(gameId, map, posX, posY), CompletableFuture.delayedExecutor(delayBombMillis, TimeUnit.MILLISECONDS));

        return gameUtils.setupDetailResponse(gameEntity.getId());
    }

    @Path("games/{gameId}/players/{playerId}/move")
    @POST
    public GameDetailResponse movePlayer(@PathParam("gameId") Long gameId, @PathParam("playerId") Long playerId, MovePlayerRequest movePlayerRequest) {
        GameEntity gameEntity = gameService.getGameById(gameId);
        if (gameEntity == null)
            throw new NotFoundException();

        PlayerEntity playerEntity = playerService.getPlayerById(playerId);
        if (playerEntity == null)
            throw new NotFoundException();

        if (movePlayerRequest == null || movePlayerRequest.posX == null || movePlayerRequest.posY == null)
            throw new BadRequestException();

        if (gameEntity.getState() != GameStateUtils.RUNNING || playerEntity.getLives() == 0)
            throw new BadRequestException();

        if (!playerService.move(playerId, gameService.decodeMap(gameEntity.getMap()), movePlayerRequest.posX, movePlayerRequest.posY))
            throw new BadRequestException();

        return gameUtils.setupDetailResponse(gameEntity.getId());
    }
}
