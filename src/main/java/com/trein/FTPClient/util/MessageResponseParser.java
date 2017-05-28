package com.trein.FTPClient.util;

/**
 * Class
 */

public final class MessageResponseParser {
    private MessageResponseParser() {
    }

    public static Message parseResponse(String response) {
        Message msg;

        try {
            msg = new Message(Integer.parseInt(response.substring(0, 3)), response.substring(4).trim());
        } catch (NumberFormatException e) {
            msg = new Message(0, response.trim());
        }

        return msg;
    }

    public static ConnectionData parsePasv(String message) {
        ConnectionData result = new ConnectionData();
        String ipAndPort = message.substring(message.indexOf("(") + 1, message.indexOf(")"));

        String[] strings = ipAndPort.split(",");
        if(strings.length != 6)
             return result;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(strings[i]);
            if (i != 3){
                sb.append('.');
            }
        }
        result.serverAddr = sb.toString();
        result.port = (Integer.parseInt(strings[4]) << 8) + Integer.parseInt(strings[5]);
        sb.setLength(0);

        return result;
    }
}
