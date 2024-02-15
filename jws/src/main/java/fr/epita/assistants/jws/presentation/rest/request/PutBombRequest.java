package fr.epita.assistants.jws.presentation.rest.request;

public class PutBombRequest {
    public Integer posX;
    public Integer posY;

    public PutBombRequest() {}

    public PutBombRequest(Integer posX, Integer posY) {
        this.posX = posX;
        this.posY = posY;
    }
}
