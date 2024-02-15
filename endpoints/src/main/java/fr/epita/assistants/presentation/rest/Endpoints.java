package fr.epita.assistants.presentation.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epita.assistants.presentation.rest.request.ReverseRequest;
import fr.epita.assistants.presentation.rest.response.HelloResponse;
import fr.epita.assistants.presentation.rest.response.ReverseResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public class Endpoints {
    @Path("hello/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public String hello(@PathParam("name") String name) throws JsonProcessingException {
        if (name == null || name.isEmpty())
            throw new BadRequestException();

        HelloResponse helloResponse = new HelloResponse("hello " + name);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(helloResponse);
    }

    @Path("reverse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public String reverse(String content) throws JsonProcessingException {
        if (content == null || content.isEmpty())
            throw new BadRequestException();

        ReverseRequest reverseRequest = new ReverseRequest(content);
        ReverseResponse reverseResponse = new ReverseResponse(reverseRequest.content);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(reverseResponse);
    }
}
