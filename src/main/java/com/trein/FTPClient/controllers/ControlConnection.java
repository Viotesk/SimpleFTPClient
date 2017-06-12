package com.trein.FTPClient.controllers;

import com.trein.FTPClient.ExceptionHandler;
import com.trein.FTPClient.util.Message;
import com.trein.FTPClient.util.MessageResponseParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ControlConnection {
    private Logger log = Logger.getLogger(ControlConnection.class);
    private ExceptionHandler exceptionHandler;
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

    /**Set exception handler for handle exceptions
     * @param exceptionHandler class for handle exceptions
     */
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void tryToConnect(String serverAddr, int port, int timeout) {
        if (currentState != State.DISCONNECTED)
            tryToDisconnect();

        this.hostname = serverAddr;
        this.port = port;
        this.timeout = timeout;

        connectingState();
    }

    public void tryToDisconnect() {
        if (currentState == State.DISCONNECTED)
            return;

        disconnectingState();
    }


    public void sendToServer(String command) {
        if (currentState != State.CONNECTED_IDLE)
            return;

        currentCmd = command;
        writingState();
    }

    public void read() {
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
    private void disconnectingState() {
        if (currentState != State.CONNECTED_IDLE)
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
        if (currentState == State.DISCONNECTED || currentState == State.DISCONNECTING)
            return;

        setState(State.CONNECTED_IDLE);
    }

    private void readingState() {
        setState(State.READING);
    }

    private void writingState() {
        if (currentState != State.CONNECTED_IDLE)
            return;

        setState(State.WRITING);
    }


    private void setState(State newState) {
        currentState = newState;

        try {
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
        } catch (Exception e) {
            exceptionHandler.catchException(Level.DEBUG, e);
            disconnectingState();
        }
    }

    private void connectPrivate() throws IOException {
        socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(hostname, port), timeout);
        } catch (IOException e) {
            exceptionHandler.catchException(Level.ERROR, e);
            disconnectedState();
            return;
        }

        try {
            in = socket.getInputStream();
            out = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            exceptionHandler.catchException(Level.ERROR, e);
            disconnectingState();
            return;
        }

        connectedState();
    }

    private void disconnectPrivate() throws IOException {
        if (!isConnected())
            return;

        try {
            socket.close();
            in.close();
        } catch (IOException e) {
            exceptionHandler.catchException(Level.ERROR, e);
        } finally {
            out.close();
            socket = null;
            in = null;
            out = null;

            disconnectedState();
        }
    }

    private void readServerResponse() throws IOException {
        do {
            readInput();

        } while (lastMessage.isMultiline());

        connectedIdleState();
    }

    private void readInput() throws IOException {
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = in.read()) != -1) {
            sb.append((char) read);
            if (read == EOL)
                break;
        }

        lastMessage = MessageResponseParser.parseResponse(sb.toString());
        log.info("Server >> " + lastMessage.toString());

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
