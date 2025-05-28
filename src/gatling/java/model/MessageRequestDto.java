package model;

import java.util.UUID;

public class MessageRequestDto{
    private Payload payload;
    private UUID chatId;
    private UUID sender;
    private UUID recipient;
    private String createdAt;

    public MessageRequestDto() {}

    public MessageRequestDto setPayload(Payload payload) {
        this.payload = payload;
        return this;
    }

    public MessageRequestDto setChatId(UUID chatId) {
        this.chatId = chatId;
        return this;
    }

    public MessageRequestDto setSender(UUID sender) {
        this.sender = sender;
        return this;
    }

    public MessageRequestDto setRecipient(UUID recipient) {
        this.recipient = recipient;
        return this;
    }

    public MessageRequestDto setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Payload getPayload() {
        return payload;
    }

    public UUID getChatId() {
        return chatId;
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getRecipient() {
        return recipient;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}