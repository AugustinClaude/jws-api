package fr.epita.assistants.jws.data.model;

import fr.epita.assistants.jws.utils.GameStateUtils;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Entity @Table(name = "game")
public class GameModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Timestamp starttime;
    public String state;

    public @ElementCollection @CollectionTable(name = "game_map", joinColumns = @JoinColumn(name = "gamemodel_id")) List<String> map;

    public GameModel() {}

    public GameModel(List<String> map) {
        this.starttime = Timestamp.from(Instant.now());
        this.state = String.valueOf(GameStateUtils.STARTING);
        this.map = map;
    }
}
