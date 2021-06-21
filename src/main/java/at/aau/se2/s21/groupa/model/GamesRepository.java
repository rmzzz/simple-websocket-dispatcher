package at.aau.se2.s21.groupa.model;

import at.aau.se2.s21.groupa.api.SimpleGameDataDispatcher;
import org.jboss.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Singleton
public class GamesRepository {
    static final Logger LOG = Logger.getLogger(GamesRepository.class);

    ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();
    ConcurrentMap<String, Game> userGames = new ConcurrentHashMap<>();

    public Game create(String owner) {
        Game g = new Game(owner);
        games.put(g.id, g);
        userGames.put(owner, g);
        LOG.infof("user %s created game %s", owner, g.id);
        return g;
    }

    public List<Game> findAll() {
        return new LinkedList<>(games.values());
    }

    public Game join(String id, String userId) {
        Game g = games.get(id);
        if(g == null)
            throw new NotFoundException("id=" + id);
        g.join(userId);
        userGames.put(userId, g);
        LOG.infof("user %s joined game %s", userId, id);
        return g;
    }

    public Game get(String id) {
        return games.get(id);
    }

    public Game findByUserId(String userId) {
        return userGames.get(userId);
    }

    public Map<String, String> findLobbies() {
        return games.values().stream()
                .filter(g -> g.isAvailable)
                .collect(Collectors.toMap(g -> g.id, g -> g.name));
    }

    public void remove(Game g) {
        userGames.remove(g.hostUserId);
        games.remove(g.id);
        LOG.infof("removed game %s", g.id);
    }
}
