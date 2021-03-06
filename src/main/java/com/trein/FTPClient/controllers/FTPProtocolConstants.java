package com.trein.FTPClient.controllers;

public class FTPProtocolConstants {
    private FTPProtocolConstants(){}

    /**
     * Commands
     */
    public static final String LOGIN = "USER ";
    public static final String PASSWORD = "PASS ";
    public static final String LIST = "LIST ";
    public static final String MLSD = "MLSD ";
    public static final String PASSIVE_MOD = "PASV ";
    public static final String DOWNLOAD_FILE = "RETR ";
    public static final String UPLOAD_FILE = "STOR ";
    public static final String MOVE_TO_DIR = "CWD ";
    public static final String CREATE_DIR = "MKD ";
    public static final String ABORT = "ABOR ";

    /**
     * Codes
     */
    public static final int NEED_PASSWORD = 331;
    public static final int LOGGING_SUCCESSFUL = 230;
    public static final int ENTERING_PASSIVE_MOD = 227;
    public static final int START_TRANSFER = 150;
    public static final int SUCCESSFUL_TRANSMISSION = 226;
    public static final int SUCCESS_MOVE = 250;
    public static final int SUCCESS_CREATE = 257;

}
