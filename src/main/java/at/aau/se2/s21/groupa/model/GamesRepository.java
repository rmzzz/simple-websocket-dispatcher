package at.aau.se2.s21.groupa.model;

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

    ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();
    ConcurrentMap<String, Game> userGames = new ConcurrentHashMap<>();

    public Game create(String owner) {
        Game g = new Game(owner);
        games.put(g.id, g);
        userGames.put(owner, g);
        return g;
    }

    public List<Game> findAll() {
        return new LinkedList<>(games.values());
    }

    public void join(String id, String userId) {
        Game g = games.get(id);
        if(g == null)
            throw new NotFoundException("id=" + id);
        g.guestUserIds.add(userId);
        userGames.put(userId, g);
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
    }
}
