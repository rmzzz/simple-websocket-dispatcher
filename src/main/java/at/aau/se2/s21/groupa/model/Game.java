package at.aau.se2.s21.groupa.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Game {
    public final String id;
    public final String hostUserId;
    private final Set<String> guestUserIds = new LinkedHashSet<>();
    public String name;
    public boolean isAvailable;

    public Game(String ownerId) {
        hostUserId = ownerId;
        id = UUID.randomUUID().toString();
        isAvailable = true;
    }

    public synchronized void join(String userId) {
        guestUserIds.add(userId);
    }

    public synchronized boolean leave(String userId) {
        return guestUserIds.remove(userId);
    }

    public synchronized Set<String> guests() {
        return new LinkedHashSet<>(guestUserIds);
    }

    public synchronized boolean hasUser(String userId) {
        return guestUserIds.contains(userId);
    }
}
