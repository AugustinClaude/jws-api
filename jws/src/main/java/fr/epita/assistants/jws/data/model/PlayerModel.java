package fr.epita.assistants.jws.data.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity @Table(name = "player")
public class PlayerModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Timestamp lastbomb;
    public Timestamp lastmovement;
    public Integer lives;
    public String name;
    public Integer posx;
    public Integer posy;
    public Integer position;

    @ManyToOne(targetEntity = GameModel.class) @JoinColumn(name = "game_id") public GameModel gameModel;
    @ManyToOne @JoinTable(name = "game_player", joinColumns = @JoinColumn(name = "players_id"), inverseJoinColumns = @JoinColumn(name = "gamemodel_id")) private GameModel gameModel1;

    public PlayerModel() {}

    public PlayerModel(String name, GameModel gameModel) {
        this.lives = 3;
        this.name = name;
        this.posx = 1;
        this.posy = 1;
        this.gameModel = gameModel;
        this.gameModel1 = gameModel;
    }
}
