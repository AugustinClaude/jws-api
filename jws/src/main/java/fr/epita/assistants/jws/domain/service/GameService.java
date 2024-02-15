package fr.epita.assistants.jws.domain.service;

import fr.epita.assistants.jws.converter.GameConverter;
import fr.epita.assistants.jws.data.model.GameModel;
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
import javax.ws.rs.NotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class GameService {
    @ConfigProperty(name = "JWS_TICK_DURATION") Long tickDuration;
    @ConfigProperty(name = "JWS_DELAY_FREE") int delayFree;
    @ConfigProperty(name = "JWS_DELAY_SHRINK") int delayShrink;

    @Inject
    GameRepository gameRepository;
    @Inject
    PlayerRepository playerRepository;

    @Inject
    PlayerService playerService;
    @Getter
    List<GameEntity> gameEntities = new ArrayList<>();

    @Transactional
    public GameEntity addGame(String firstPlayer, String mapPath) throws IOException {
        GameModel gameModel = new GameModel(initMap(mapPath));

        boolean isNewGame = !gameRepository.listAll().contains(gameModel);
        if (isNewGame)
            gameRepository.persist(gameModel);

        GameConverter gameConverter = new GameConverter(gameModel);
        GameEntity gameEntity = gameConverter.convertToGameEntity(playerRepository);
        PlayerEntity playerEntity = playerService.addPlayer(firstPlayer, gameEntity);
        gameEntity.addPlayer(playerEntity);

        if (isNewGame)
            gameEntities.add(gameEntity);

        return gameEntity;
    }

    private List<String> initMap(String mapPath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(mapPath));
        List<String> map = bufferedReader.lines().toList();
        bufferedReader.close();
        return map;
    }

    public List<String> decodeMap(List<String> encodedMap) {
        List<String> decodedMap = new ArrayList<>();
        for (String line : encodedMap) {
            StringBuilder mapLine = new StringBuilder();
            for (int i = 0; i < line.length(); i += 2)
                mapLine.append(String.valueOf(line.charAt(i + 1)).repeat(Integer.parseInt(String.valueOf(line.charAt(i)))));
            decodedMap.add(mapLine.toString());
        }

        return decodedMap;
    }

    private List<String> encodeMap(List<String> decodedMap) {
        List<String> encodedMap = new ArrayList<>();
        for (String line : decodedMap) {
            StringBuilder mapLine = new StringBuilder();
            int i = 0;
            while (i < line.length()) {
                int counter = 0;
                char c = line.charAt(i);

                while (i < line.length() && line.charAt(i) == c && counter < 9) {
                    counter++;
                    i++;
                }

                mapLine.append(counter).append(c);
            }

            encodedMap.add(mapLine.toString());
        }

        return encodedMap;
    }

    @Transactional
    public void shrinkMapByOne(Long gameId, List<String> decodedMap, int shrinkPos) {
        List<String> shrunkMap = new ArrayList<>();
        String fullMetal = "MMMMMMMMMMMMMMMMM";

        for (int i = 0; i < decodedMap.size(); i++) {
            if (i <= shrinkPos || i >= decodedMap.size() - shrinkPos - 1)
                shrunkMap.add(fullMetal);
            else {
                StringBuilder mapLine = new StringBuilder();
                String line = decodedMap.get(i);
                for (int j = 0; j < line.length(); j++) {
                    if (j <= shrinkPos || j >= line.length() - shrinkPos - 1)
                        mapLine.append('M');
                    else
                        mapLine.append(line.charAt(j));
                }

                shrunkMap.add(mapLine.toString());
            }
        }

        // Kill all players that got squished to death
        AtomicInteger aliveCounter = new AtomicInteger();
        playerRepository.listAll().forEach(p -> {
            if (p.gameModel.id.equals(gameId) && shrunkMap.get(p.posy).charAt(p.posx) == 'M')
                p.lives = 0;
            if (p.gameModel.id.equals(gameId) && p.lives > 0)
                aliveCounter.getAndIncrement();
        });

        List<String> newMap = encodeMap(shrunkMap);
        GameModel gameModel = gameRepository.findById(gameId);
        if (gameModel == null)
            return;

        if (aliveCounter.get() <= 1)
            gameModel.state = String.valueOf(GameStateUtils.FINISHED);
        gameModel.map = newMap;
    }

    @Transactional
    public void startShrinking(Long gameId, int shrinkPos) throws ExecutionException, InterruptedException {
        GameModel gameModel = gameRepository.findById(gameId);
        if (gameModel == null)
            return;

        if (gameModel.state.equals(String.valueOf(GameStateUtils.FINISHED)))
            return;
        if (gameModel.map.stream().allMatch(l -> l.equals("9M8M")))
            return;

        shrinkMapByOne(gameId, decodeMap(gameModel.map), shrinkPos);
        CompletableFuture.runAsync(() -> {
            try {
                startShrinking(gameId, shrinkPos + 1);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CompletableFuture.delayedExecutor(delayShrink * tickDuration, TimeUnit.MILLISECONDS));
    }

    @Transactional
    public void start(Long gameId) {
        GameModel gameModel = gameRepository.findById(gameId);
        if (gameModel == null)
            return;

        if (!gameModel.state.equals(String.valueOf(GameStateUtils.STARTING)))
            throw new NotFoundException();

        GameConverter gameConverter = new GameConverter(gameModel);
        GameEntity gameEntity = gameConverter.convertToGameEntity(playerRepository);
        List<PlayerEntity> playerEntities = gameEntity.getPlayerEntities();

        gameModel.state = playerEntities.size() == 1 ? String.valueOf(GameStateUtils.FINISHED) : String.valueOf(GameStateUtils.RUNNING);

        // Start shrinking after JWS_DELAY_FREE ticks
        CompletableFuture.runAsync(() -> {
            try {
                startShrinking(gameId, 1);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, CompletableFuture.delayedExecutor(delayFree * tickDuration, TimeUnit.MILLISECONDS));
    }

    @Transactional
    public void placeOnMap(Long gameId, char c, int posX, int posY) {
        GameModel gameModel = gameRepository.findById(gameId);
        if (gameModel == null)
            return;

        List<String> map = decodeMap(gameModel.map);
        StringBuilder line = new StringBuilder(map.get(posY));
        line.setCharAt(posX, c);
        map.set(posY, line.toString());
        gameModel.map = encodeMap(map);
    }

    @Transactional
    public void updateState(Long gameId, GameStateUtils state) {
        GameModel gameModel = gameRepository.findById(gameId);
        if (gameModel == null)
            return;

        gameModel.state = String.valueOf(state);
    }

    public GameEntity getGameById(Long gameId) {
        GameModel gameModel = gameRepository.findById(gameId);
        if (gameModel == null)
            return null;

        GameConverter gameConverter = new GameConverter(gameModel);
        return gameConverter.convertToGameEntity(playerRepository);
    }
}
