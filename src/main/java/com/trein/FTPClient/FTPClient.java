package com.trein.FTPClient;

import com.trein.FTPClient.controllers.ControlConnection;
import com.trein.FTPClient.controllers.DataConnection;
import com.trein.FTPClient.controllers.FTPProtocolConstants;
import com.trein.FTPClient.util.ConnectionData;
import com.trein.FTPClient.util.MessageResponseParser;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FTPClient {
    private Logger log = Logger.getLogger(FTPClient.class);

    private static final int DEFAULT_PORT = 21;
    private static final String LOCALHOST = "127.0.0.1";

    private ControlConnection controlConnection;
    private DataConnection dataConnection;

    private String login;
    private boolean logged;


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

    public boolean isConnected() {
        return controlConnection.isConnected();
    }

    public boolean upload(String fileName, InputStream inputStream) throws IOException {
        if(fileName.isEmpty() || inputStream == null || !controlConnection.isConnected())
            return false;

        if(!dataConnection.isConnected())
            if(!openPassiveDataConnection())
                return false;

        controlConnection.sendToServer(FTPProtocolConstants.UPLOAD_FILE + fileName);

        if( controlConnection.getLastReplyCode() != FTPProtocolConstants.START_TRANSFER )
            return false;

        dataConnection.write(inputStream);
        controlConnection.read();

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESSFUL_TRANSMISSION;
    }

    public boolean download(String fileName, OutputStream outputStream) throws IOException {
        if(fileName == null || fileName.isEmpty() || outputStream == null || !controlConnection.isConnected())
            return false;

        if(!dataConnection.isConnected())
            if(!openPassiveDataConnection())
                return false;

        controlConnection.sendToServer(FTPProtocolConstants.DOWNLOAD_FILE + fileName);

        if(controlConnection.getLastReplyCode() != FTPProtocolConstants.START_TRANSFER )
            return false;

        dataConnection.read(outputStream);
        controlConnection.read();
        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESSFUL_TRANSMISSION;
    }

    public String currentDirectory() throws IOException {
        if(!controlConnection.isConnected())
            return "null";

        controlConnection.sendToServer("PWD");

        Pattern p = Pattern.compile("(\".+\")(.*)");
        Matcher m = p.matcher(controlConnection.getLastMessage());
        m.find();
        return m.group(1);
    }

    public boolean setDirectory(String path) throws IOException {
        if(!controlConnection.isConnected())
            return false;

        controlConnection.sendToServer("CWD " + path);

        return true;
    }

    public boolean printList(OutputStream outputStream) throws IOException {
        if(!dataConnection.isConnected())
            if (!openPassiveDataConnection())
                return false;

        controlConnection.sendToServer(FTPProtocolConstants.LIST);

        if(controlConnection.getLastReplyCode() != FTPProtocolConstants.START_TRANSFER)
            return false;

        dataConnection.read(outputStream);
        controlConnection.read();

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESSFUL_TRANSMISSION;
    }

    public boolean readData(OutputStream out) throws IOException {
        dataConnection.read(out);
        controlConnection.read();

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESSFUL_TRANSMISSION;
    }



    public boolean openPassiveDataConnection() throws IOException {
        if(!controlConnection.isConnected())
            return false;

        controlConnection.sendToServer(FTPProtocolConstants.PASSIVE_MOD);

        if(controlConnection.getLastReplyCode() != FTPProtocolConstants.ENTERING_PASSIVE_MOD)
            return false;

        ConnectionData connectionData = MessageResponseParser.parsePasv(controlConnection.getLastMessage());
        dataConnection.connect(connectionData.hostname, connectionData.port);

        return dataConnection.isConnected();
    }

    public String getLogin() {
        return login;
    }

    public boolean isLogged() {
        return logged;
    }

    public boolean connect(String hostname) throws IOException {
        return this.connect(hostname, DEFAULT_PORT);
    }

    public boolean connect(String hostname, int port) throws IOException {
        return connect(hostname, port, 0);
    }

    public boolean connect(String hostname, int port, int timeout) throws IOException {
        if (hostname.isEmpty() || (port < 1 || port > 65535) || timeout < 0)
            return false;

        controlConnection.tryToConnect(hostname, port, timeout);
        return controlConnection.isConnected();
    }

    public boolean login(String login, char[] password) throws IOException {
        if (login == null || login.isEmpty())
            return false;

        this.login = login;

        controlConnection.sendToServer(FTPProtocolConstants.LOGIN + login);

        if (controlConnection.getLastReplyCode() == FTPProtocolConstants.NEED_PASSWORD)
            controlConnection.sendToServer(FTPProtocolConstants.PASSWORD + password);

        logged = controlConnection.getLastReplyCode() == FTPProtocolConstants.LOGGING_SUCCESSFUL;
        return logged;
    }

    public boolean sendCommand(String command) throws IOException {
        if (command == null || command.isEmpty() || !controlConnection.isConnected())
            return false;

        controlConnection.sendToServer(command);
        return true;
    }

    public void disconnect() throws IOException {
        controlConnection.tryToDisconnect();
    }


}
