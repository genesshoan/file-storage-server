package dev.shoangenes.server;

import dev.shoangenes.exception.DatabaseException;
import dev.shoangenes.file.FileManager;
import dev.shoangenes.model.FSHeader;
import dev.shoangenes.model.FSMessage;
import dev.shoangenes.model.OpCode;
import dev.shoangenes.model.ResultCode;
import dev.shoangenes.repository.DBFileRepository;
import dev.shoangenes.repository.IFileRepository;
import dev.shoangenes.utils.LoggerUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementación vacía de IFileService.
 */
public class FileService implements IFileService, AutoCloseable {
    /*========================== Fields ===========================*/

    private IFileRepository fileRepository;
    private FileManager fileManager;

    /*========================== Logger ===========================*/

    private final Logger logger = LoggerUtil.getLogger(FileService.class);

    /*======================== Constructors =======================*/

    /**
     * Constructs a FileService with the specified file repository and file manager.
     * This constructor allows for dependency injection of the repository and manager.
     * ONLY FOR TESTING PURPOSES.
     *
     * @param fileRepository the file repository to use for database operations
     * @param fileManager    the file manager to use for file system operations
     */
    public FileService(IFileRepository fileRepository, FileManager fileManager) {
        this.fileRepository = fileRepository;
        this.fileManager = fileManager;
    }

    /**
     * Constructs a FileService with default implementations of file repository and file manager.
     * This constructor initializes the service with a DBFileRepository and a FileManager.
     * FOR PRODUCTION USE. It should be used with try-with-resources to ensure proper resource management.
     *
     * @throws DatabaseException if there is an error initializing the database connection
     */
    public FileService() throws DatabaseException {
        this(new DBFileRepository(), new FileManager());
    }

    /*======================= Public Methods =======================*/

    /**
     * Processes a file system message and returns a response.
     * @param message the incoming FSMessage
     * @return the response FSMessage
     */
    @Override
    public FSMessage processRequest(FSMessage message) {
        try {
            message.validateMessage();
        } catch (IllegalArgumentException e) {
            logger.info("Invalid request received: " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.BAD_REQUEST, "Invalid request: " + e.getMessage());
        }

        OpCode opCode = OpCode.fromCode(message.getOpCodeOrResult());
        logger.info("Processing request with OpCode: " + opCode);

        Map<FSHeader, String> headers = message.getHeaders();

        String name = headers.get(FSHeader.FILE_NAME);
        String idStr = headers.get(FSHeader.ID);
        Integer id = null;

        try {
            if (name == null) {
                id = Integer.parseInt(idStr);
                name = fileRepository.getFileName(id);
                if (name == null) {
                    logger.warning("File not found for ID: " + id);
                    return FSMessage.createErrorResponse(ResultCode.NOT_FOUND, "File not found for ID: " + id);
                }
            } else {
                id = fileRepository.getId(name);
                if (id == -1) {
                    logger.warning("File not found: " + name);
                    return FSMessage.createErrorResponse(ResultCode.NOT_FOUND, "File not found: " + name);
                }
            }
        } catch (NumberFormatException e) {
            logger.warning("Invalid ID format: " + idStr);
            return FSMessage.createErrorResponse(ResultCode.BAD_REQUEST, "Invalid ID format: " + idStr);
        } catch (Exception e) {
            logger.severe("Database error during request processing: " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "Database error: " + e.getMessage());
        }

        switch (opCode) {
            case PUT -> {
                logger.info("Handling PUT operation for file: " + name);
                return handlePut(name, message.getBody());
            }
            case GET -> {
                logger.info("Handling GET operation for file: " + name + ", ID: " + id);
                return handleGet(id, name);
            }
            case DELETE -> {
                logger.info("Handling DELETE operation for file: " + name + ", ID: " + id);
                return handleDelete(id, name);
            }
            default -> {
                logger.warning("Unsupported operation: " + opCode);
                return FSMessage.createErrorResponse(ResultCode.BAD_REQUEST, "Unsupported operation");
            }
        }
    }

    /**
     * Sends a response message through the given DataOutputStream.
     * @param out the DataOutputStream to send the response
     * @param message the FSMessage to send
     */
    @Override
    public void sendResponse(DataOutputStream out, FSMessage message) {
        try {
            message.writeTo(out);
            logger.info("Response sent to client. ResultCode: " + message.getOpCodeOrResult());
        } catch (IOException e) {
            logger.severe("Failed to send response (the current client is probably disconnected): " + e.getMessage());
        }
    }

    /**
     * Closes the file service, releasing any resources held by the file repository and file manager.
     */
    @Override
    public void close() throws Exception {
        if (fileRepository instanceof AutoCloseable) {
            ((AutoCloseable) fileRepository).close();
            logger.info("FileService closed - repository connection closed");
        }
    }

    /*====================== Private Methods ======================*/

    /**
     * Handles the DELETE operation to remove a file by name.
     *
     * @param fileName The name of the file to delete.
     * @return An FSMessage indicating success or failure of the operation.
     */
    private FSMessage handleDelete(int id, String fileName) {
        Optional<byte[]> content = Optional.empty();
        try {
            if (!fileRepository.fileExists(id)) {
                logger.warning("File not found for deletion: " + fileName + " (ID: " + id + ")");
                return FSMessage.createErrorResponse(ResultCode.NOT_FOUND, "File not found: " + fileName);
            }

            content =  fileManager.deleteFile(fileName);
            fileRepository.removeMapping(id);

            logger.info("File deleted successfully: " + fileName + " (ID: " + id + ")");
            return FSMessage.createOkResponse(id, fileName);
        } catch (DatabaseException e) {
            content.ifPresent(bytes -> {
                try { fileManager.saveFile(fileName, bytes); }
                catch (IOException ignored) {
                    logger.severe("Failed to restore file after database error: " + fileName + " - " + ignored.getMessage());
                }
            });
            logger.severe("Database error during file deletion: " + fileName + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (IOException e) {
            logger.severe("Failed to delete file: " + fileName + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "File system error: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error during file deletion: " + fileName + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Handles the PUT operation to store a file.
     *
     * @param name    The name of the file to store.
     * @param content The content of the file as a byte array.
     * @return An FSMessage indicating success or failure of the operation.
     */
    private FSMessage handlePut(String name, byte[] content) {
        try {
            if (fileRepository.fileExists(name)) {
                logger.warning("Attempt to overwrite existing file: " + name);
                return FSMessage.createErrorResponse(ResultCode.BAD_REQUEST, "File already exists: " + name);
            }

            fileManager.saveFile(name, content);
            int id = fileRepository.saveMapping(name);

            logger.info("File stored successfully: " + name + " (ID: " + id + ")");
            return FSMessage.createOkResponse(id, name);
        } catch (DatabaseException e) {
            // Attempt to clean up the file if database operation fails
            try { fileManager.deleteFile(name); }
            catch (IOException ignored) {
                logger.severe("Failed to clean up file after database error: " + name + " - " + ignored.getMessage());
            }
            logger.severe("Database error during file storage: " + name + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (IOException e) {
            logger.severe("Failed to save file: " + name + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "File system error: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error during file storage: " + name + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Handles the GET operation to retrieve a file by ID.
     *
     * @param id The ID of the file to retrieve.
     * @return An FSMessage containing the file content or an error message.
     */
    private FSMessage handleGet(int id, String fileName) {
        try {
            if (!fileRepository.fileExists(id)) {
                logger.warning("File not found for retrieval: " + fileName + " (ID: " + id + ")");
                return FSMessage.createErrorResponse(ResultCode.NOT_FOUND, "File not found: " + id);
            }

            Optional<byte[]> content = fileManager.getFile(fileName);
            if (content.isPresent()) {
                logger.info("File retrieved successfully: " + fileName + " (ID: " + id + ")");
                return FSMessage.createOkGetResponse(id, fileName, content.get());
            } else {
                logger.warning("File content not found: " + fileName + " (ID: " + id + ")");
                return FSMessage.createErrorResponse(ResultCode.NOT_FOUND, "File content not found: " + fileName);
            }
        } catch (DatabaseException e) {
            logger.severe("Failed to retrieve file: " + fileName + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (IOException e) {
            logger.severe("Failed to get file: " + fileName + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "File system error: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error during file retrieval: " + fileName + " - " + e.getMessage());
            return FSMessage.createErrorResponse(ResultCode.SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }
}
