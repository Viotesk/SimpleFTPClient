package com.trein.FTPClient;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FTPClient {
    private ControlState currentControlState = ControlState.DISCONNECTED;
    private Logger log = Logger.getLogger(FTPClient.class);

    private static final int DEFAULT_PORT = 21;
    private static final String LOCALHOST = "127.0.0.1";
    private static final char EOL = 10;

    private int port;
    private String serverAddr;
    private String username;

    private Socket server;
    private InputStream in;
    private PrintStream out;

    private String lastResponse;

    public ControlState getCurrentControlState() {
        return currentControlState;
    }

    public String getLastResponse() {
        return lastResponse;
    }

    public void connect(String serverAddr) {
        this.connect(serverAddr, DEFAULT_PORT);
    }

    public void connect(String serverAddr, int port) {
        this.serverAddr = serverAddr;
        this.port = port;
        connectingState();
    }

    public enum ControlState {
        DISCONNECTING,
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECTED_IDLE,
        READING,
        WRITING
    }

    private void disconnectingState() {
        if(currentControlState != ControlState.CONNECTED_IDLE)
            return;

        setState(ControlState.DISCONNECTING);
    }

    private void disconnectedState() {
        setState(ControlState.DISCONNECTED);
    }

    private void connectingState() {
        if (currentControlState != ControlState.DISCONNECTED)
            return;

        setState(ControlState.CONNECTING);
    }

    private void connectedState() {
        if (currentControlState == ControlState.DISCONNECTED)
            return;

        setState(ControlState.CONNECTED);
    }

    private void connectedIdleState() {
        if(currentControlState == ControlState.DISCONNECTED || currentControlState == ControlState.DISCONNECTING)
            return;

        setState(ControlState.CONNECTED_IDLE);
    }

    private void readingState() {
        setState(ControlState.READING);
    }


    private void setState(ControlState newState) {

        this.currentControlState = newState;

        try {
            switch (this.currentControlState) {
                case DISCONNECTING:
                    disconnectPrivate();
                    disconnectedState();
                    break;
                case DISCONNECTED:
                    break;
                case CONNECTING:
                    connectPrivate();
                    break;
                case CONNECTED:
                    readingState();
                    connectedIdleState();
                    break;
                case CONNECTED_IDLE:
                    break;
                case READING:
                    readServerResponse();
                    break;
                case WRITING:
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
    }

    public void disconnect() {
        disconnectingState();
    }


    private boolean isConnected() {
        return server != null;
    }
}
