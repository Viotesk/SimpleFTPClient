package com.trein.FTPClient.util;

/**
 * DTO Message class
 */
public class Message {
    private int code;
    private String message;
    private boolean multiline;

    public Message(int code, String message, boolean multiline) {
        this.code = code;
        this.message = message;
        this.multiline = multiline;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMultiline(){
        return multiline;
    }

    @Override
    public String toString() {
        return "Message{" +
                "code=" + code +
                ", text='" + message + '\'' +
                '}';
    }
}