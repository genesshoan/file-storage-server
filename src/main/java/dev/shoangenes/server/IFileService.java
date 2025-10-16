package dev.shoangenes.server;

import dev.shoangenes.model.FSMessage;
import java.io.DataOutputStream;

/**
 * Interface for file service operations.
 */
public interface IFileService {
    /**
     * Processes a file system message and returns a response.
     * @param message the incoming FSMessage
     * @return the response FSMessage
     */
    FSMessage processRequest(FSMessage message);

    /**
     * Sends a response message through the given DataOutputStream.
     * @param out the DataOutputStream to send the response
     * @param message the FSMessage to send
     * @return true if the response was sent successfully, false otherwise
     */
    boolean sendResponse(DataOutputStream out, FSMessage message);
}
