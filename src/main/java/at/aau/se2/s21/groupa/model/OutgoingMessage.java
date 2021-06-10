package at.aau.se2.s21.groupa.model;

public class OutgoingMessage {
    public String from;
    public Payload data;

    public OutgoingMessage(Payload data) {
        this.data = data;
    }

    public OutgoingMessage(String from, Payload data) {
        this.from = from;
        this.data = data;
    }
}
