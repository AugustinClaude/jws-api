package fr.epita.assistants.jws.presentation.rest.request;

public class MovePlayerRequest {
    public Integer posX;
    public Integer posY;

    public MovePlayerRequest() {}

    public MovePlayerRequest(Integer posX, Integer posY) {
        this.posX = posX;
        this.posY = posY;
    }
}
