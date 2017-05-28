package com.trein.FTPClient;

import com.trein.FTPClient.controllers.ControlConnection;
import com.trein.FTPClient.controllers.DataConnection;
import org.apache.log4j.Logger;


public class FTPClient {
    private Logger log = Logger.getLogger(FTPClient.class);

    private static final int DEFAULT_PORT = 21;
    private static final String LOCALHOST = "127.0.0.1";

    private ControlConnection controlConnection;
    private DataConnection dataConnection;


    public FTPClient() {
        controlConnection = new ControlConnection();
        dataConnection = new DataConnection();
    }

    public ControlConnection.State getCurrentControlState() {
        return controlConnection.getCurrentState();
    }

    public String getLastResponse() {
        return controlConnection.getLastResponse();
    }

    public void connect(String serverAddr) {
        this.connect(serverAddr, DEFAULT_PORT);
    }

    public void connect(String serverAddr, int port) {
        controlConnection.tryToConnect(serverAddr, port);
    }

    public void login(String login, String password) {
        controlConnection.tryToLogin(login, password);
    }

    public void disconnect(){
        controlConnection.tryToDisconnect();
    }


}
