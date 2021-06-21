package at.aau.se2.s21.groupa.api;

import at.aau.se2.s21.groupa.model.Game;
import at.aau.se2.s21.groupa.model.GamesRepository;
import at.aau.se2.s21.groupa.model.IncomingMessage;
import at.aau.se2.s21.groupa.model.OutgoingMessage;
import at.aau.se2.s21.groupa.model.Payload;
import at.aau.se2.s21.groupa.model.SessionsRepository;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.NotFoundException;
import java.util.stream.Stream;

@ServerEndpoint("/game/{userId}")
@Singleton
public class SimpleGameDataDispatcher {
    static final Logger LOG = Logger.getLogger(SimpleGameDataDispatcher.class);

    @Inject
    SessionsRepository sessions;

    @Inject
    GamesRepository games;

    @Inject
    Jsonb jsonb;

    public SimpleGameDataDispatcher() {
        LOG.infof("started");
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        LOG.infof("connected %s", userId);
        sessions.put(userId, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        LOG.infof("disconnected %s", userId);
        sessions.remove(userId);
        Game g = games.findByUserId(userId);
        if (g != null) {
            if (g.leave(userId)) {
                sendMessage(g.hostUserId, userId, new Payload(103));
            } else if (g.hostUserId.equals(userId)) {
                // host is gone -> eliminate the game!
                games.remove(g);
                g.guestUserIds.forEach(u -> {
                    Session s = sessions.get(u);
                    if (s != null && s.isOpen()) {
                        try {
                            s.close();
                        } catch (Exception ex) {
                            LOG.debugf(ex, "Error closing session for user %s", u);
                        }
                    }
                });
            }
        }
    }

    @OnError
    public void onError(Session session, @PathParam("userId") String userId, Throwable throwable) {
        //sessions.remove(username);
        LOG.errorf(throwable, "Error from %s", userId);
    }


    @OnMessage
    public void onMessage(String msg, @PathParam("userId") String userId) {
        LOG.infof("message from %s: %s", userId, msg);

        IncomingMessage incomingMessage = jsonb.fromJson(msg, IncomingMessage.class);

        if (incomingMessage.isSystem()) {
            // process system message
            Game g;
            switch (incomingMessage.data.type) {
                case 100:
                    // create game
                    g = games.create(userId);
                    g.name = incomingMessage.data.payload;
                    sessions.putName(userId, incomingMessage.data.payload);
                    // ack
                    sendMessage(userId, null, new Payload(100));
                    break;
                case 101:
                    // discover games
                    sessions.putName(userId, incomingMessage.data.payload);
                    sendMessage(userId, null, new Payload(
                            101, jsonb.toJson(games.findLobbies())));
                    break;
                case 102:
                    // join game
                    String gId = incomingMessage.data.payload;
                    g = games.join(gId, userId);
                    // notify host only!
                    sendMessage(g.hostUserId, userId, new Payload(102, sessions.getName(userId)));
                    break;
                default:
                    LOG.warnf("Unknown system command from %s: %s", userId, msg);
            }
        } else {
            // dispatch to other
            Game g = games.findByUserId(userId);
            if (g == null)
                throw new NotFoundException("Not joined any game. user: " + userId + "; message: " + msg);

            if (g.hostUserId.equals(userId)) {
                // broadcast
                Stream.of(incomingMessage.to)
                        .filter(g.guestUserIds::contains)
                        .forEach(u -> sendMessage(u, userId, incomingMessage.data));
            } else {
                // end to owner only
                sendMessage(g.hostUserId, userId, incomingMessage.data);
            }
        }
    }

    void sendMessage(String to, String from, Payload payload) {
        Session s = sessions.get(to);
        if (s == null) {
            LOG.error("Error sending message to " + to);
            return;
        }
        String msg = jsonb.toJson(
                new OutgoingMessage(from, payload));
        LOG.infof("message to %s: %s", to, msg);
        s.getAsyncRemote()
                .sendText(msg);
    }
}
