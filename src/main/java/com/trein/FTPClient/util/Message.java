package com.trein.FTPClient.util;

/**
 * DTO Message class
 */
public class Message {
    private int code;
    private String message;

    public Message(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "code=" + code +
                ", text='" + message + '\'' +
                '}';
    }
}
