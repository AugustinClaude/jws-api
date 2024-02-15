package fr.epita.assistants.jws.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.epita.assistants.jws.utils.GameStateUtils;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.BadRequestException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Value
public class GameEntity {
    Timestamp startTime;
    GameStateUtils state;
    List<PlayerEntity> playerEntities;
    List<String> map;
    Long id;

    @ConfigProperty(name = "JWS_MAP_PATH", defaultValue = "src/test/resources/map1.rle") @NonFinal @JsonIgnore
    String mapPath;

    public GameEntity(Timestamp startTime, GameStateUtils state, List<PlayerEntity> playerEntities, List<String> map, Long id) {
        this.startTime = startTime;
        this.state = state;
        this.playerEntities = Objects.requireNonNullElseGet(playerEntities, ArrayList::new);
        this.map = map;
        this.id = id;
    }

    public void addPlayer(PlayerEntity playerEntity) {
        if (playerEntities.size() >= 4)
            throw new BadRequestException();

        playerEntities.add(playerEntity);
    }
}
