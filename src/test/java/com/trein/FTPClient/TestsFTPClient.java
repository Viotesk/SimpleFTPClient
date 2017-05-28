package com.trein.FTPClient;


import com.trein.FTPClient.controllers.ControlConnection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestsFTPClient  {

    @Test
    public void testConnect() {
        FTPClient ftp = new FTPClient();

        //DEFAULT PORT
        assertEquals(ControlConnection.State.DISCONNECTED, ftp.getCurrentControlState());
        ftp.connect("asdasd");
        assertEquals(ControlConnection.State.DISCONNECTED, ftp.getCurrentControlState());
        ftp.connect("127.0.0.1");
        assertEquals(ControlConnection.State.CONNECTED_IDLE, ftp.getCurrentControlState());
        ftp.disconnect();
        assertEquals(ControlConnection.State.DISCONNECTED, ftp.getCurrentControlState());

        //USER PORT
        ftp.connect("asdasd", 21);
        assertEquals(ControlConnection.State.DISCONNECTED, ftp.getCurrentControlState());
        ftp.connect("127.0.0.1", 25);
        assertEquals(ControlConnection.State.DISCONNECTED, ftp.getCurrentControlState());
        ftp.connect("127.0.0.1", 21);
        assertEquals(ControlConnection.State.CONNECTED_IDLE, ftp.getCurrentControlState());
        assertEquals(true, ftp.getLastResponse().contains("code=220"));
        ftp.disconnect();
        assertEquals(ControlConnection.State.DISCONNECTED, ftp.getCurrentControlState());
    }

    @Test
    public void testLogin() {
        FTPClient ftp = new FTPClient();
        ftp.connect("127.0.0.1");

        ftp.login("fasodfhasdf", "klfhsdklf");
        assertEquals(false, ftp.isLogged());
        ftp.login("anonymous", "");
        assertEquals(true, ftp.isLogged());
        ftp.login("dasd", "fsdf");
        assertEquals(false, ftp.isLogged());

        ftp.login("anonymous", "");
        assertEquals(true, ftp.isLogged());
        ftp.login("anonymous", "");
        assertEquals(true, ftp.isLogged());



    }

}
