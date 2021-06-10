package at.aau.se2.s21.groupa.model;

import java.io.Serializable;

public class Payload implements Serializable {
    public int type;
    public String payload;

    public Payload() {
    }

    public Payload(int type) {
        this.type = type;
    }

    public Payload(int type, String payload) {
        this.type = type;
        this.payload = payload;
    }
}
