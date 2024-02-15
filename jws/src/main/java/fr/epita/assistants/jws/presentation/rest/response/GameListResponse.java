package fr.epita.assistants.jws.presentation.rest.response;

import fr.epita.assistants.jws.utils.GameStateUtils;

public class GameListResponse {
    public Long id;
    public Integer players = 0;
    public GameStateUtils state;

    public GameListResponse() {}
}
