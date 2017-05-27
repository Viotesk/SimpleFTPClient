package com.trein.FTPClient;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestsFTPClient  {

    @Test
    public void testConnect() {
        FTPClient ftp = new FTPClient();

        //DEFAULT PORT
        assertEquals(FTPClient.ControlState.DISCONNECTED, ftp.getCurrentControlState());
        ftp.connect("asdasd");
        assertEquals(FTPClient.ControlState.DISCONNECTED, ftp.getCurrentControlState());
        ftp.connect("127.0.0.1");
        assertEquals(FTPClient.ControlState.CONNECTED_IDLE, ftp.getCurrentControlState());
        ftp.disconnect();
        assertEquals(FTPClient.ControlState.DISCONNECTED, ftp.getCurrentControlState());

        //USER PORT
        ftp.connect("asdasd", 21);
        assertEquals(FTPClient.ControlState.DISCONNECTED, ftp.getCurrentControlState());
        ftp.connect("127.0.0.1", 25);
        assertEquals(FTPClient.ControlState.DISCONNECTED, ftp.getCurrentControlState());
        ftp.connect("127.0.0.1", 21);
        assertEquals(FTPClient.ControlState.CONNECTED_IDLE, ftp.getCurrentControlState());
        assertEquals(true, ftp.getLastResponse().startsWith("220"));
        ftp.disconnect();
        assertEquals(FTPClient.ControlState.DISCONNECTED, ftp.getCurrentControlState());
    }

}
