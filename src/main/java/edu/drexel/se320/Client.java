package edu.drexel.se320;

import java.io.IOException;

public class Client {

    private final ServerConnection connection;
    private String lastResult;
    StringBuilder sb = null;

    public Client(ServerConnection connection){
        this.connection = connection;
        lastResult = "";
    }

    public String requestFile(String server, String file) {
        if (server == null)
            throw new IllegalArgumentException("Null server address");
        if (file == null)
            throw new IllegalArgumentException("Null file");

	// This ServerConnection is here only as a placeholder --- the real dependency
	// doesn't exist yet.  Your tests will need to make sure the code below this
	// definition of conn interacts with connections correctly despite not having
	// a real ServerConnection.
	//
	// To be clear: do NOT implement the methods below.  Instead, make it possible
	// to run the code below with a mock, rather than this dummy implementation.
    /* 
        ServerConnection conn = new ServerConnection() {
            public boolean connectTo(String address) throws IOException {
                throw new UnsupportedOperationException();
            }
            public boolean requestFileContents(String filename) throws IOException {
                throw new UnsupportedOperationException();
            }
            public String read() throws IOException {
                throw new UnsupportedOperationException();
            }
            public boolean moreBytes() throws IOException {
                throw new UnsupportedOperationException();
            }
            public void closeConnection() throws IOException {
                throw new UnsupportedOperationException();
            }
        };*/


	// We'll use a StringBuilder to construct large strings more efficiently
	// than repeated linear calls to string concatenation.
        sb = new StringBuilder();

        try {
            if (connection.connectTo(server)) {
                boolean validFile = connection.requestFileContents(file);
                if (validFile) {
                    while (connection.moreBytes()) {
                        String tmp = connection.read();
                        if (tmp != null) {
                            sb.append(tmp);
                        }
                    }
                    connection.closeConnection();
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        String result = sb.toString();
        lastResult = result;
        return result;
    }
}

