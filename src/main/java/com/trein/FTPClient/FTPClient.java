package com.trein.FTPClient;

import com.trein.FTPClient.controllers.ControlConnection;
import com.trein.FTPClient.controllers.DataConnection;
import com.trein.FTPClient.controllers.FTPProtocolConstants;
import com.trein.FTPClient.util.ConnectionData;
import com.trein.FTPClient.util.MessageResponseParser;
import org.apache.log4j.Logger;

import java.io.*;


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

    public void download(String fileName, String pathDesc) throws IOException {
        if(!openPassiveDataConnection())
            return;

        controlConnection.sendToServer(FTPProtocolConstants.DOWNLOAD_FILE + fileName);
        dataConnection.read();

        try(FileWriter writer = new FileWriter(pathDesc + fileName, false)) {
            writer.write(dataConnection.getResponse());
        } catch (IOException e) {
            log.warn("Exception " + e);
        }

//        try(Writer fileWriter = new OutputStreamWriter(new FileOutputStream(pathDesc + fileName), StandardCharsets.UTF_8)) {
//            fileWriter.write(dataConnection.getResponse());
//        } catch (IOException e) {
//            log.warn("Exception " + e);
//        }
    }

    public void upload(String fileName, String pathSrc) throws IOException {
        StringBuilder sb = new StringBuilder();
        String dataToSend;

        try(BufferedReader reader = new BufferedReader(new FileReader(pathSrc + fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n\n");
            }
            dataToSend = sb.toString();
            sb.setLength(0);
        } catch (IOException e) {
            dataToSend = null;
            log.warn("Exception " + e);
        }

        if(!openPassiveDataConnection())
            return;
        controlConnection.sendToServer(FTPProtocolConstants.UPLOAD_FILE + fileName);

        dataConnection.write(dataToSend);
    }


    //TODO throw exception without return null.
    public String printList() throws IOException {
        if (!openPassiveDataConnection())
            return null;

        controlConnection.sendToServer(FTPProtocolConstants.LIST);
        dataConnection.read();


        return dataConnection.getResponse();
    }

    public boolean openPassiveDataConnection() throws IOException {
        controlConnection.sendToServer(FTPProtocolConstants.PASSIVE_MOD);
        if(controlConnection.getLastReplyCode() != FTPProtocolConstants.ENTERING_PASSIVE_MOD)
            return false;

        ConnectionData connectionData = MessageResponseParser.parsePasv(controlConnection.getLastMessage());
        dataConnection.connect(connectionData.serverAddr, connectionData.port);

        return dataConnection.isConnected();
    }

    public String getLogin() {
        return login;
    }

    public boolean isLogged() {
        return logged;
    }

    public boolean connect(String serverAddr) throws IOException {
        return this.connect(serverAddr, DEFAULT_PORT);
    }

    public boolean connect(String serverAddr, int port) throws IOException {
        controlConnection.tryToConnect(serverAddr, port);
        return controlConnection.isConnected();
    }

    //TODO throw exception if current state not CONNECTED_IDLE
    public void login(String login, char[] password) throws IOException {
        this.login = login;

        controlConnection.sendToServer(FTPProtocolConstants.LOGIN + login);
        if (controlConnection.getLastReplyCode() == FTPProtocolConstants.NEED_PASSWORD)
            controlConnection.sendToServer(FTPProtocolConstants.PASSWORD + password);

        logged = controlConnection.getLastReplyCode() == FTPProtocolConstants.LOGGING_SUCCESSFUL;

        //TODO throw exception if not logged
    }

    public void sendCommand(String command) throws IOException {
        controlConnection.sendToServer(command);
    }

    public void disconnect() throws IOException {
        controlConnection.tryToDisconnect();
    }


}
