package at.aau.se2.s21.groupa.api;

import at.aau.se2.s21.groupa.model.Game;
import at.aau.se2.s21.groupa.model.GamesRepository;
import at.aau.se2.s21.groupa.model.SessionsRepository;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("lobby")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameLobbyApi {

    @Inject
    GamesRepository games;

    @Inject
    SessionsRepository sessions;

    /**
     * Creates a game lobby.
     * Inactive lobby will be removed by timeout.
     * @param ownerId
     * @return game URI
     */
    @POST
    public Response create(@QueryParam("owner") String ownerId) {
        Game g = games.create(ownerId);
        return Response.created(
                URI.create(
                        String.format("/game/%s/%s", g.id, ownerId)))
                .build();
    }

    /**
     * Find a lobby to join
     * @param userId
     * @return
     */
    @GET
    public Response find(@QueryParam("userId") String userId) {
        Map<String,String> ids = games.findAll().stream()
                .collect(Collectors.toMap(g -> g.id, g -> g.hostUserId));
        return Response.ok(ids).build();
    }

    /**
     * Join lobby
     * @param id
     * @param userId
     * @return
     */
    @PUT
    @Path("{id}/join/{userId}")
    public Response join(@PathParam("id") String id, @PathParam("userId") String userId) {
        games.join(id, userId);
        return Response.accepted().build();
    }

    @POST
    @Path("{id}/start")
    public Response startGame(@PathParam("id") String id, @QueryParam("owner") String ownerId) {
        games.get(id).guestUserIds.stream()
                .map(u -> sessions.get(u))
                .forEach(s -> s.getAsyncRemote()
                        .sendText("start"));

        return Response.ok().build();
    }
}
