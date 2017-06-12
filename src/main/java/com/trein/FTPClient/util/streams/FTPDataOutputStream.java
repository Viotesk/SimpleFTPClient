package com.trein.FTPClient.util.streams;

import com.trein.FTPClient.controllers.DataConnection;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FTPDataOutputStream extends FilterOutputStream {
    private DataConnection data;

    public FTPDataOutputStream(OutputStream out, DataConnection data) {
        super(out);
        this.data = data;
    }

    @Override
    public void close() throws IOException {
        data.disconnect();
        super.close();
    }
}
