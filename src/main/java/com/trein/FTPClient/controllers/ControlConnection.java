package com.trein.FTPClient.controllers;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ControlConnection {
    private State currentState = State.DISCONNECTED;
    private Logger log = Logger.getLogger(ControlConnection.class);

    private Socket server;
    private InputStream in;
    private PrintStream out;

    private int port;
    private String serverAddr;
    private String login;

    private static final char EOL = 10;
    private String lastResponse;
    private String currentCmd;
    private boolean isLogged;

    public String getLastResponse() {
        return lastResponse;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void tryToConnect(String serverAddr, int port) {
        if(currentState != State.DISCONNECTED)
            tryToDisconnect();

        this.serverAddr = serverAddr;
        this.port = port;

        connectingState();
    }

    //TODO Send information to user we are already disconnected
    public void tryToDisconnect() {
        if(currentState == State.DISCONNECTED)
            return;

        disconnectingState();
    }

    //TODO throw exception if current state not CONNECTED_IDLE
    public void tryToLogin(String login, String password) {
        if(currentState != State.CONNECTED_IDLE)
            return;

        this.login = login;

        sendToServer(FTPCommands.LOGIN + login);
        sendToServer(FTPCommands.PASSWORD + password);
    }

    //TODO Send information to user we cant send to server if we not connected
    public void sendToServer(String command) {
        if(currentState != State.CONNECTED_IDLE)
            return;

        currentCmd = command;
        writingState();
    }

    public enum State {
        DISCONNECTING,
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECTED_IDLE,
        READING,
        WRITING
    }

    //TODO throw Exception instead "return"
    private void disconnectingState() {
        if(currentState != State.CONNECTED_IDLE)
            return;

        setState(State.DISCONNECTING);
    }

    private void disconnectedState() {
        setState(State.DISCONNECTED);
    }

    private void connectingState() {
        if (currentState != State.DISCONNECTED)
            return;

        setState(State.CONNECTING);
    }

    private void connectedState() {
        if (currentState == State.DISCONNECTED)
            return;

        setState(State.CONNECTED);
    }

    private void connectedIdleState() {
        if(currentState == State.DISCONNECTED || currentState == State.DISCONNECTING)
            return;

        setState(State.CONNECTED_IDLE);
    }

    private void readingState() {
        setState(State.READING);
    }

    private void writingState() {
        if(currentState != State.CONNECTED_IDLE)
            return;

        setState(State.WRITING);
    }


    private void setState(State newState) {

        this.currentState = newState;

        try {
            switch (this.currentState) {
                case DISCONNECTING:
                    disconnectPrivate();
                    break;
                case DISCONNECTED:
                    break;
                case CONNECTING:
                    connectPrivate();
                    break;
                case CONNECTED:
                    readingState();
                    break;
                case CONNECTED_IDLE:
                    break;
                case READING:
                    readServerResponse();
                    break;
                case WRITING:
                    writingPrivate();
                    break;
            }
        } catch (Exception e) {
            log.warn("Exception: " + e);
            disconnectedState();
        }
    }

    private void connectPrivate() throws IOException {
        server = new Socket();
        server.connect(new InetSocketAddress(serverAddr, port));
        if (isConnected()) {
            in = server.getInputStream();
            out = new PrintStream(server.getOutputStream(), true);
            connectedState();
        } else {
            disconnectedState();
        }
    }

    private void disconnectPrivate() throws IOException {
        if(!isConnected())
            return;

        server.close();
        server = null;
        in.close();
        in = null;
        out.close();
        out = null;

        disconnectedState();
    }

    private void readServerResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = in.read()) != -1) {
            sb.append((char) read);
            if (read == EOL)
                break;
        }
        lastResponse = sb.toString();
        log.info("Server >> " + lastResponse);

        connectedIdleState();
    }

    private void writingPrivate() {
        out.print(currentCmd);
        log.info("Client << " + currentCmd);
        readingState();
    }




    public boolean isConnected() {
        return server != null;
    }
}
