package fr.epita.assistants.jws.utils;

public class PlayerUtils {
    public Long id;
    public String name;
    public Integer lives;
    public Integer posX;
    public Integer posY;

    public PlayerUtils(Long id, String name, Integer lives, Integer posX, Integer posY) {
        this.id = id;
        this.name = name;
        this.lives = lives;
        this.posX = posX;
        this.posY = posY;
    }
}
