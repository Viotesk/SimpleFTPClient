package com.trein.FTPClient.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageResponseParser {
    private MessageResponseParser() {
    }

    public static Message parseResponse(String response) {
        Message msg = new Message();

//        Pattern p = Pattern.compile("^[0-9]{3}");
//        Matcher matcher = p.matcher(response);
//
//        if(!matcher.matches()) {
//            msg.code = 0;
//            msg.text = response.trim();
//            return msg;
//        }

        try {
            msg.code = Integer.parseInt(response.substring(0, 3));
            msg.text = response.substring(4);
        } catch (NumberFormatException e) {
            msg.code = 0;
            msg.text = response.trim();
        }

        return msg;
    }
}
