package edu.drexel.se320;

import java.io.IOException;

public interface ServerConnection {

    /*
     * Attempt to connect to the specified server.
     * Returns true if connection succeeded, false otherwise.
     */
    boolean connectTo(String address) throws IOException;

    /*
     * Request that the server send the specified file.
     * Returns true if the server agrees that the next bytes sent
     * will come from that file, and false otherwise (e.g., if
     * the file does not exist)
     */
    boolean requestFileContents(String filename) throws IOException;

    /*
     * Read the next portion of the file from the server.
     * The server may return the whole rest of the file,
     * or only a prefix of the remainder.
     *
     * The server will only send each part of the file once,
     * in order with respect to other parts.
     *
     * This may return null.
     *
     * It is an error to call this method after {@code moreBytes()}
     * returns false, until a new file is requested.
     */
    String read() throws IOException;

    /*
     * Ask the server if there are more bytes in the file
     * that was requested.  If the server has not yet sent
     * the full file, this should return true.  Otherwise,
     * it will return false, and no subsequent calls should
     * be made to {@code read()} until a new file is requested.
     */
    boolean moreBytes() throws IOException;

    /*
     * Terminate the connection.
     *
     * It is an error to call this before the connection is open.
     *
     * It is an error to call any other method after this one,
     * except for a call to {@code connectTo} (after which, the
     * other methods become valid again.
     */
    void closeConnection() throws IOException;

}

