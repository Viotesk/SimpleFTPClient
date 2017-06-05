package com.trein.FTPClient.controllers;

/**
 * Created by Viote on 28.05.2017.
 */
public class FTPProtocolConstants {
    private FTPProtocolConstants(){}

    /**
     * Commands
     */
    public static final String LOGIN = "USER ";
    public static final String PASSWORD = "PASS ";
    public static final String LIST = "LIST ";
    public static final String PASSIVE_MOD = "PASV ";
    public static final String DOWNLOAD_FILE = "RETR ";
    public static final String UPLOAD_FILE = "STOR ";

    /**
     * Codes
     */
    public static final int NEED_PASSWORD = 331;
    public static final int LOGGING_SUCCESSFUL = 230;
    public static final int ENTERING_PASSIVE_MOD = 227;
}
