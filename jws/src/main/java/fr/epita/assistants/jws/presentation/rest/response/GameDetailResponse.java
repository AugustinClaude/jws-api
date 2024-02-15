package fr.epita.assistants.jws.presentation.rest.response;

import fr.epita.assistants.jws.utils.GameStateUtils;
import fr.epita.assistants.jws.utils.PlayerUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GameDetailResponse {
    public Timestamp startTime;
    public GameStateUtils state;
    public List<PlayerUtils> players = new ArrayList<>();
    public List<String> map;
    public Long id;

    public GameDetailResponse() {}
}
