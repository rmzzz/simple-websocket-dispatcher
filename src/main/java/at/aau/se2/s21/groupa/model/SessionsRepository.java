package at.aau.se2.s21.groupa.model;

import at.aau.se2.s21.groupa.api.SimpleGameDataDispatcher;
import org.jboss.logging.Logger;

import javax.inject.Singleton;
import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class SessionsRepository {
    static final Logger LOG = Logger.getLogger(SessionsRepository.class);
    ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    ConcurrentMap<String, String> names = new ConcurrentHashMap<>();

    public void put(String userId, Session session) {
        sessions.put(userId, session);
    }

    public void remove(String userId) {
        sessions.remove(userId);
        names.remove(userId);
    }

    public Session get(String userId) {
        return sessions.get(userId);
    }

    public void putName(String userId, String name) {
        names.put(userId, name);
    }

    public String getName(String userId) {
        String name = names.get(userId);
        if(name == null) {
            LOG.warnf("could not find name for userId: %s", userId);
            name = "anonymous?";
        }
        return name;
    }
}
