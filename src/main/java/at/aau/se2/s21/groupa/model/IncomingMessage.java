package at.aau.se2.s21.groupa.model;

public class IncomingMessage {
    public String[] to;
    public Payload data;

    public boolean isSystem() {
        return to == null || to.length == 0;
    }
}
