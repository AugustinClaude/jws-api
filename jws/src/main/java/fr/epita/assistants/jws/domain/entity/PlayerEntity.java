package fr.epita.assistants.jws.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;

import java.sql.Timestamp;

@Value
public class PlayerEntity {
    Long id;
    String name;
    Integer lives;
    Integer posX;
    Integer posY;

    public PlayerEntity(Long id, String name, Integer lives, Integer posX, Integer posY) {
        this.id = id;
        this.name = name;
        this.lives = lives;
        this.posX = posX;
        this.posY = posY;
    }
}
