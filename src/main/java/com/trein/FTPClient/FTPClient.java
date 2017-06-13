package com.trein.FTPClient;

import com.trein.FTPClient.controllers.ControlConnection;
import com.trein.FTPClient.controllers.DataConnection;
import com.trein.FTPClient.controllers.FTPProtocolConstants;
import com.trein.FTPClient.util.ConnectionData;
import com.trein.FTPClient.util.MessageResponseParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FTPClient {
    private Logger log = Logger.getLogger(FTPClient.class);

    private static final int DEFAULT_PORT = 21;

    private int bufferSize = 1024;

    private ControlConnection controlConnection;
    private DataConnection dataConnection;

    private ExceptionHandler exceptionHandler;

    private String login;
    private boolean logged;

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public FTPClient() {
        exceptionHandler = new ExceptionHandler();
        controlConnection = new ControlConnection();
        controlConnection.setExceptionHandler(exceptionHandler);
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

    public boolean startUpload(String fileName) {
        controlConnection.sendToServer(FTPProtocolConstants.UPLOAD_FILE + fileName);

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.START_TRANSFER;
    }

    public boolean startDownload(String fileName) {
        controlConnection.sendToServer(FTPProtocolConstants.DOWNLOAD_FILE + fileName);

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.START_TRANSFER;
    }

    public boolean transferIsSuccessfull() {
        controlConnection.read();
        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESSFUL_TRANSMISSION;
    }

    public boolean upload(String fileName, InputStream inputStream) {
        if (fileName.isEmpty() || inputStream == null || !controlConnection.isConnected())
            return false;

        if (!dataConnection.isConnected())
            if (!openPassiveDataConnection())
                return false;

        controlConnection.sendToServer(FTPProtocolConstants.UPLOAD_FILE + fileName);

        if (controlConnection.getLastReplyCode() != FTPProtocolConstants.START_TRANSFER)
            return false;

        byte[] buffer = new byte[bufferSize];

        try (OutputStream out = dataConnection.write()) {
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            exceptionHandler.catchException(Level.WARN, e);
            return false;
        }


        controlConnection.read();

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESSFUL_TRANSMISSION;
    }

    public boolean abort() {
        if (!controlConnection.isConnected())
            return false;

        controlConnection.sendToServer(FTPProtocolConstants.ABORT);

        return true;
    }

    public boolean download(String fileName, OutputStream outputStream) {
        if (fileName == null || fileName.isEmpty() || outputStream == null || !controlConnection.isConnected())
            return false;

        if (!dataConnection.isConnected())
            if (!openPassiveDataConnection())
                return false;

        controlConnection.sendToServer(FTPProtocolConstants.DOWNLOAD_FILE + fileName);

        if (controlConnection.getLastReplyCode() != FTPProtocolConstants.START_TRANSFER)
            return false;

        byte[] buffer = new byte[bufferSize];

        try (InputStream in = dataConnection.read()) {

            int length;
            while ((length = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            exceptionHandler.catchException(Level.WARN, e);
            return false;
        }

        controlConnection.read();

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESSFUL_TRANSMISSION;
    }

    public String currentDirectory() {
        if (!controlConnection.isConnected())
            return "null";

        controlConnection.sendToServer("PWD");

        Pattern p = Pattern.compile("(\".+\")(.*)");
        Matcher m = p.matcher(controlConnection.getLastMessage());
        m.find();
        return m.group(1);
    }

    public boolean setDirectory(String path) {
        if (!controlConnection.isConnected())
            return false;

        controlConnection.sendToServer(FTPProtocolConstants.MOVE_TO_DIR + path);

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESS_MOVE;
    }

    public boolean createDir(String name) {
        if (!controlConnection.isConnected())
            return false;

        controlConnection.sendToServer(FTPProtocolConstants.CREATE_DIR + name);

        return controlConnection.getLastReplyCode() == FTPProtocolConstants.SUCCESS_CREATE;
    }

    public String printList() {
        if (!dataConnection.isConnected())
            if (!openPassiveDataConnection())
                return null;

        controlConnection.sendToServer(FTPProtocolConstants.LIST);

        if (controlConnection.getLastReplyCode() != FTPProtocolConstants.START_TRANSFER)
            return null;

        byte[] buffer = new byte[bufferSize];


        int length;
        try (InputStream in = dataConnection.read();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.flush();
            controlConnection.read();
            return out.toString();
        } catch (IOException e) {
            exceptionHandler.catchException(Level.ERROR, e);
            return null;
        }
    }

    public String commandMLSD() {
        if (!dataConnection.isConnected())
            if (!openPassiveDataConnection()) {
                log.debug("passiv connection close");
                return null;
            }

        controlConnection.sendToServer(FTPProtocolConstants.MLSD);

        if (controlConnection.getLastReplyCode() != FTPProtocolConstants.START_TRANSFER)
            return null;

        byte[] buffer = new byte[bufferSize];


        int length;
        try (InputStream in = dataConnection.read();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.flush();
            controlConnection.read();
            return out.toString();
        } catch (IOException e) {
            exceptionHandler.catchException(Level.ERROR, e);
            return null;
        }
    }

    public InputStream readData() {
        return dataConnection.read();
    }

    public OutputStream writeData() {
        return dataConnection.write();
    }


    public boolean openPassiveDataConnection() {
        if (!controlConnection.isConnected())
            return false;

        controlConnection.sendToServer(FTPProtocolConstants.PASSIVE_MOD);

        if (controlConnection.getLastReplyCode() != FTPProtocolConstants.ENTERING_PASSIVE_MOD)
            return false;


        ConnectionData connectionData = MessageResponseParser.parsePasv(controlConnection.getLastMessage());
        dataConnection.connect(connectionData.hostname, connectionData.port);

        return dataConnection.isConnected();
    }

    public boolean dataIsConnected() {
        return dataConnection.isConnected();
    }

    public String getLogin() {
        return login;
    }

    public boolean isLogged() {
        return logged;
    }

    public boolean connect(String hostname) {
        return this.connect(hostname, DEFAULT_PORT);
    }

    public boolean connect(String hostname, int port) {
        return connect(hostname, port, 0);
    }

    public boolean connect(String hostname, int port, int timeout) {
        if (hostname.isEmpty() || (port < 1 || port > 65535) || timeout < 0)
            return false;

        controlConnection.tryToConnect(hostname, port, timeout);
        return controlConnection.isConnected();
    }

    public boolean login(String login, char[] password) {
        if (login == null || login.isEmpty())
            return false;

        this.login = login;

        controlConnection.sendToServer(FTPProtocolConstants.LOGIN + login);

        if (controlConnection.getLastReplyCode() == FTPProtocolConstants.NEED_PASSWORD)
            controlConnection.sendToServer(FTPProtocolConstants.PASSWORD + password);

        logged = controlConnection.getLastReplyCode() == FTPProtocolConstants.LOGGING_SUCCESSFUL;
        return logged;
    }

    public boolean sendCommand(String command) {
        if (command == null || command.isEmpty() || !controlConnection.isConnected())
            return false;

        controlConnection.sendToServer(command);
        return true;
    }

    public void disconnect() throws IOException {
        controlConnection.tryToDisconnect();
    }


}
