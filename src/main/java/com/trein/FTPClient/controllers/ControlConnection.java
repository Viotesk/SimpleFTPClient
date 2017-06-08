package com.trein.FTPClient.controllers;

import com.trein.FTPClient.util.Message;
import com.trein.FTPClient.util.MessageResponseParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ControlConnection {
    private Logger log = Logger.getLogger(ControlConnection.class);
    private Socket socket;

    private InputStream in;
    private PrintStream out;

    private int port;
    private String hostname;
    private int timeout = 0;

    private State currentState = State.DISCONNECTED;

    private static final char EOL = 10;
    private static final String EOL_SEND = "\r\n";
    private Message lastMessage;
    private String currentCmd;

    /**
     * @return the last code from the server response
     */
    public int getLastReplyCode() {
        if (lastMessage == null)
            return 0;

        return lastMessage.getCode();
    }

    /**
     * @return the last message from the server response
     */
    public String getLastMessage() {
        if (lastMessage == null)
            return "null";

        return lastMessage.getMessage();
    }

    /**
     * @return the server response containing the code and the message
     */
    public String getLastResponse() {
        if (lastMessage == null)
            return "null";

        return lastMessage.toString();
    }

    /**
     * @return current state
     */
    public State getCurrentState() {
        return currentState;
    }

    public void tryToConnect(String serverAddr, int port, int timeout) throws IOException {
        if (currentState != State.DISCONNECTED)
            tryToDisconnect();

        this.hostname = serverAddr;
        this.port = port;
        this.timeout = timeout;

        connectingState();
    }

    public void tryToDisconnect() throws IOException {
        if (currentState == State.DISCONNECTED)
            return;

        disconnectingState();
    }


    public void sendToServer(String command) throws IOException {
        if (currentState != State.CONNECTED_IDLE)
            return;

        currentCmd = command;
        writingState();
    }

    public void read() throws IOException {
        readingState();
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
    private void disconnectingState() throws IOException {
        if (currentState != State.CONNECTED_IDLE)
            return;

        setState(State.DISCONNECTING);
    }

    private void disconnectedState() throws IOException {
        setState(State.DISCONNECTED);
    }

    private void connectingState() throws IOException {
        if (currentState != State.DISCONNECTED)
            return;

        setState(State.CONNECTING);
    }

    private void connectedState() throws IOException {
        if (currentState == State.DISCONNECTED)
            return;

        setState(State.CONNECTED);
    }

    private void connectedIdleState() throws IOException {
        if (currentState == State.DISCONNECTED || currentState == State.DISCONNECTING)
            return;

        setState(State.CONNECTED_IDLE);
    }

    private void readingState() throws IOException {
        setState(State.READING);
    }

    private void writingState() throws IOException {
        if (currentState != State.CONNECTED_IDLE)
            return;

        setState(State.WRITING);
    }


    private void setState(State newState) throws IOException {

        currentState = newState;

        switch (currentState) {
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
    }

    private void connectPrivate() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(hostname, port));
        if (isConnected()) {
            in = socket.getInputStream();
            out = new PrintStream(socket.getOutputStream());
            connectedState();
        } else {
            disconnectedState();
        }
    }

    private void disconnectPrivate() throws IOException {
        if (!isConnected())
            return;

        socket.close();
        socket = null;
        in.close();
        in = null;
        out.close();
        out = null;

        disconnectedState();
    }

    private void readServerResponse() throws IOException {
        do {
            readInput();

        } while (lastMessage.isMultiline());

        connectedIdleState();
    }

    private String readInput() throws IOException {
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = in.read()) != -1) {
            sb.append((char) read);
            if (read == EOL)
                break;
        }

        lastMessage = MessageResponseParser.parseResponse(sb.toString());
        log.info("Server >> " + lastMessage.toString());

        return sb.toString();
    }

    private void writingPrivate() throws IOException {
        out.print(currentCmd + EOL_SEND);
        log.info("Client << " + currentCmd);
        readingState();
    }


    public boolean isConnected() {
        return ((socket != null) && (socket.isConnected()));
    }
}
