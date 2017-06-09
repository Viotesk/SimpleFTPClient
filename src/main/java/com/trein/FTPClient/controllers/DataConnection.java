package com.trein.FTPClient.controllers;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DataConnection {
    private Logger log = Logger.getLogger(DataConnection.class);

    private State currentState = State.DISCONNECTED;
    private static final char EOL = 10;
    private static final int BUFFER_SIZE = 1024;

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    private InputStream clientIn;
    private OutputStream clientOut;

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

    public void read(OutputStream out) {
        this.clientOut = out;
        readingState();
    }

    public void write(InputStream in) {
        this.clientIn = in;
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
        if (currentState != State.CONNECTED)
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
            disconnectedState();
        }
    }

    private void connectPrivate() throws IOException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(serverAddr, port));
        } catch (IOException e) {
            log.error("Connection exception: ", e);
            throw e;
        }

        if (!isConnected()) {
            disconnectedState();
            return;
        }

        try {
            in = socket.getInputStream();
            out = new PrintStream(socket.getOutputStream(), true);
        } catch (IOException e) {
            log.error("Exception while opening streams: ", e);
            throw e;
        }
        connectedState();
    }

    private void readPrivate() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;

        try {
            while ((length = in.read(buffer)) != -1) {
                clientOut.write(buffer, 0, length);
            }
        } catch (IOException e) {
            log.warn("Exception while reading data: ", e);
            throw e;
        }

        disconnectingState();
    }

    private void writePrivate()  {
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;

        try {
            while ((length = clientIn.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            log.warn("Exception while writing date: ", e);
        }

        disconnectingState();
    }

    private void disconnectPrivate() throws IOException {
        in.close();
        out.close();

        if (isConnected())
            socket.close();

        socket = null;
        in = null;
        out = null;

        clientIn = null;
        clientOut = null;
    }

    public boolean isConnected() {
        return ((socket != null) && (socket.isConnected()));
    }
}
