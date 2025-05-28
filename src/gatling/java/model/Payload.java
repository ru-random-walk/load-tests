package model;

public class Payload {
    private String type;
    private String text;

    public Payload setType(String type) {
        this.type = type;
        return this;
    }

    public Payload setText(String text) {
        this.text = text;
        return this;
    }

    public Payload() {
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
