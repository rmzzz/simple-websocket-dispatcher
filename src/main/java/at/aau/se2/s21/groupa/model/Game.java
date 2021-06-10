package at.aau.se2.s21.groupa.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Game {
    public final String id;
    public final String hostUserId;
    public final Set<String> guestUserIds = new LinkedHashSet<>();
    public String name;
    public boolean isAvailable;

    public Game(String ownerId) {
        hostUserId = ownerId;
        id = UUID.randomUUID().toString();
        isAvailable = true;
    }

    public void join(String userId) {
        guestUserIds.add(userId);
    }

    public boolean leave(String userId) {
        return guestUserIds.remove(userId);
    }

}
