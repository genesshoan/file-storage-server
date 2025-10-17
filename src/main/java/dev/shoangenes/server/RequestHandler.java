package dev.shoangenes.server;

import dev.shoangenes.model.FSMessage;
import dev.shoangenes.utils.LoggerUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Handles client requests in a separate thread.
 * Reads FSMessage objects from the input stream, processes them using the provided FileService,
 * and sends responses back to the client until the connection is closed.
 */
class RequestHandler implements Runnable {
    /*============================= Fields =============================*/
    private final Socket socket;
    private final IFileService fileService;

    /*============================= Logger =============================*/

    Logger logger = LoggerUtil.getLogger(RequestHandler.class);

    /*=========================== Constructors ==========================*/

    /**
     * Constructs a RequestHandler with the given socket and file service.
     * @param socket the client socket
     * @param fileService the file service to handle requests
     */
    public RequestHandler(Socket socket, IFileService fileService) {
        this.socket = socket;
        this.fileService = fileService;
    }

    /*=========================== Public Methods ========================*/

    /**
     * Handles incoming requests from the client.
     * Reads FSMessage objects from the input stream, processes them,
     * and sends responses back to the client until the connection is closed.
     */
    @Override
    public void run() {
        try (socket;
             fileService;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            boolean shouldRun = true;

            while (shouldRun) {
                FSMessage message = FSMessage.readFrom(input);
                logger.info(String.format("Received FSMessage from %s", socket.getRemoteSocketAddress()));

                FSMessage response = fileService.processRequest(message);
                logger.info(String.format("Processed FSMessage from %s", socket.getRemoteSocketAddress()));

                shouldRun = fileService.sendResponse(output, response);
                logger.info(String.format("Sent FSMessage to %s", socket.getRemoteSocketAddress()));
            }
        } catch (Exception e) {
            logger.severe(String.format("Closing connection to %s: %s", socket.getRemoteSocketAddress(), e.getMessage()));
        }

    }
}
