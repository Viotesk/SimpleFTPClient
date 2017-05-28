package com.trein.FTPClient.controllers;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DataConnection {
    private Logger log = Logger.getLogger(DataConnection.class);
    private State currentState = State.DISCONNECTED;
    private static final char EOL = 10;

    private Socket server;
    private InputStream in;
    private OutputStream out;

    private String serverAddr;
    private int port;

    private byte[] dataToSend;

    private String response;

    public State getCurrentState() {
        return currentState;
    }

    public String getResponse() {
        return response;
    }

    public void connect(String serverAddr, int port) {
        this.serverAddr = serverAddr;
        this.port = port;

        connectingState();
    }

    public void read() {
        readingState();
    }

    public void write(String text) {
        dataToSend = text.getBytes(StandardCharsets.UTF_8);
        writingState();
    }

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        READING,
        WRITING,
        DISCONNECTING
    }

    //TODO throw Exception instead "return"
    private void connectingState() {
        if (currentState != State.DISCONNECTED)
            return;

        setState(State.CONNECTING);
    }

    private void connectedState() {
        if (currentState != State.CONNECTING)
            return;

        setState(State.CONNECTED);
    }

    private void readingState() {
        if (currentState != State.CONNECTED)
            return;

        setState(State.READING);
    }

    private void writingState() {
        if(currentState != State.CONNECTED)
            return;

        setState(State.WRITING);
    }

    private void disconnectingState() {
        if (currentState == State.DISCONNECTED)
            return;

        setState(State.DISCONNECTING);
    }

    private void disconnectedState() {
        if (currentState != State.DISCONNECTING)
            return;

        setState(State.DISCONNECTED);
    }

    private void setState(State newState) {
        currentState = newState;
        try {
            switch (currentState) {
                case DISCONNECTED:
                    break;
                case CONNECTING:
                    connectPrivate();
                    break;
                case CONNECTED:
                    break;
                case READING:
                    readPrivate();
                    break;
                case WRITING:
                    writePrivate();
                    break;
                case DISCONNECTING:
                    disconnectPrivate();
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

    private void readPrivate() throws IOException {
        StringBuilder sb = new StringBuilder();
        int read;

        while ((read = in.read()) != -1) {
            sb.append((char) read);
        }
        response = sb.toString();
        sb.setLength(0);
        disconnectingState();
    }

    private void writePrivate() throws IOException {
        out.write(dataToSend);
        out.flush();
        disconnectingState();
    }

    private void disconnectPrivate() throws IOException {
        if (isConnected())
            server.close();

        server = null;
        in.close();
        in = null;
        out.close();
        out = null;

    }

    public boolean isConnected() {
        return server != null;
    }
}
