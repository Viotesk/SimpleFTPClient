package com.trein.FTPClient.util.streams;

import com.trein.FTPClient.controllers.DataConnection;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FTPDataInputStream extends FilterInputStream {
    private DataConnection data;

    public FTPDataInputStream(InputStream in, DataConnection data) {
        super(in);
        this.data = data;
    }


    @Override
    public void close() throws IOException {
        data.disconnect();
        super.close();
    }
}
