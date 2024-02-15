package fr.epita.assistants.jws.converter;

import fr.epita.assistants.jws.data.model.PlayerModel;
import fr.epita.assistants.jws.domain.entity.PlayerEntity;

import java.sql.Timestamp;

public class PlayerConverter {
    private final Long id;
    private final String name;
    private final Integer lives;
    private final Integer posX;
    private final  Integer posY;

    public PlayerConverter(PlayerModel playerModel) {
        this.id = playerModel.id;
        this.name = playerModel.name;
        this.lives = playerModel.lives;
        this.posX = playerModel.posx;
        this.posY = playerModel.posy;
    }

    public PlayerEntity convertToPlayerEntity() {
        return new PlayerEntity(id, name, lives, posX, posY);
    }
}
