package fr.epita.assistants.jws.converter;

import fr.epita.assistants.jws.data.model.GameModel;
import fr.epita.assistants.jws.data.model.PlayerModel;
import fr.epita.assistants.jws.data.repository.PlayerRepository;
import fr.epita.assistants.jws.domain.entity.GameEntity;
import fr.epita.assistants.jws.utils.GameStateUtils;
import fr.epita.assistants.jws.domain.entity.PlayerEntity;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GameConverter {
    private final Timestamp startTime;
    private final GameStateUtils state;
    private final List<String> map;
    private final Long id;

    public GameConverter(GameModel gameModel) {
        this.startTime = gameModel.starttime;
        this.state = GameStateUtils.valueOf(gameModel.state);
        this.map = gameModel.map;
        this.id =  gameModel.id;
    }

    public GameEntity convertToGameEntity(PlayerRepository playerRepository) {
        List<PlayerEntity> playerEntities = new ArrayList<>();
        for (PlayerModel playerModel : playerRepository.listAll()) {
            if (playerModel.gameModel.id.equals(this.id)) {
                PlayerConverter playerConverter = new PlayerConverter(playerModel);
                playerEntities.add(playerConverter.convertToPlayerEntity());
            }
        }

        return new GameEntity(startTime, state, playerEntities, map, id);
    }
}
