package fr.epita.assistants.presentation.rest.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epita.assistants.presentation.rest.response.HelloResponse;

import javax.ws.rs.BadRequestException;

public class ReverseRequest {
    public String content;

    public ReverseRequest(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        HelloResponse helloResponse;
        try {
            helloResponse = objectMapper.readValue(json, HelloResponse.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException();
        }

        if (helloResponse == null || helloResponse.content == null || helloResponse.content.isEmpty())
            throw new BadRequestException();

        this.content = helloResponse.content;
    }
}
