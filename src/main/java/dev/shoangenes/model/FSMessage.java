package dev.shoangenes.model;

import dev.shoangenes.config.StorageProperties;
import dev.shoangenes.utils.FileValidator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * FSMessage represents a message in a custom file storage protocol.
 * It can be either a request or a response, containing headers and an optional body.
 */
public class FSMessage {
    /*============================ Constants ============================*/

    public static final int MAGIC_NUMBER = 0xABCD1234;

    /*============================ Fields ============================*/

    private int magicNumber;
    private byte type; // 0 = request, 1 = response
    private int opCodeOrResult; // OpCode for request, ResultCode for response
    private Map<String, String> headers; // Metadata as key-value pairs
    private long bodyLength;
    private byte[] body;

    /*=========================== Constructor ================================*/

    /**
     * Private constructor to initialize an FSMessage.
     *
     * @param type            The type of the message (0 for request, 1 for response).
     * @param opCodeOrResult The operation code (for requests) or result code (for responses).
     */
    private FSMessage(byte type, int opCodeOrResult) {
        this.magicNumber = MAGIC_NUMBER;
        this.type = type;
        this.opCodeOrResult = opCodeOrResult;
        headers = new HashMap<>();
        bodyLength = 0;
        body = new byte[0];
    }

    /*=========================== Request Factory =============================*/

    /**
     * Factory method to create a PUT request FSMessage.
     *
     * @param fileName The name of the file to be stored.
     * @param body     The content of the file as a byte array.
     * @return An FSMessage representing the PUT request.
     */
    public static FSMessage createPutRequest(String fileName, byte[] body) {
        FSMessage put = new FSMessage((byte)0, OpCode.PUT.getCode());
        put.addHeader("File-Name", fileName);
        put.setBody(body);
        return put;
    }

    /**
     * Factory method to create a GET request FSMessage.
     *
     * @param fileName The name of the file to be retrieved.
     * @return An FSMessage representing the GET request.
     */
    public static FSMessage createGetRequest(String fileName) {
        FSMessage get = new FSMessage((byte)0, OpCode.GET.getCode());
        get.addHeader("File-Name", fileName);
        return get;
    }

    /**
     * Factory method to create a GET request FSMessage by ID.
     *
     * @param id The ID of the file to be retrieved.
     * @return An FSMessage representing the GET request.
     */
    public static FSMessage createGetRequest(int id) {
        FSMessage get = new FSMessage((byte)0, OpCode.GET.getCode());
        get.addHeader("ID", String.valueOf(id));
        return get;
    }

    /**
     * Factory method to create a DELETE request FSMessage.
     *
     * @param fileName The name of the file to be deleted.
     * @return An FSMessage representing the DELETE request.
     */
    public static FSMessage createDeleteRequest(String fileName) {
        FSMessage delete = new FSMessage((byte)0, OpCode.DELETE.getCode());
        delete.addHeader("File-Name", fileName);
        return delete;
    }

    /**
     * Factory method to create a DELETE request FSMessage by ID.
     *
     * @param id The ID of the file to be deleted.
     * @return An FSMessage representing the DELETE request.
     */
    public static FSMessage createDeleteRequest(int id) {
        FSMessage delete = new FSMessage((byte)0, OpCode.DELETE.getCode());
        delete.addHeader("ID", String.valueOf(id));
        return delete;
    }

    /*=========================== Response Factory ============================*/

    /**
     * Factory method to create a successful response FSMessage.
     *
     * @param id       The ID of the file.
     * @param fileName The name of the file.
     * @return An FSMessage representing a successful response.
     */
    public static FSMessage createOkResponse(int id, String fileName) {
        FSMessage ok = new FSMessage((byte)1, ResultCode.SUCCESS.getCode());
        ok.addHeader("ID", String.valueOf(id));
        ok.addHeader("File-Name", fileName);
        return ok;
    }

    /**
     * Factory method to create a successful GET response FSMessage with body.
     *
     * @param id       The ID of the file.
     * @param fileName The name of the file.
     * @param body     The content of the file as a byte array.
     * @return An FSMessage representing a successful GET response with body.
     */
    public static FSMessage createOkGetResponse(int id, String fileName, byte[] body) {
        FSMessage ok = createOkResponse(id, fileName);
        ok.setBody(body);
        return ok;
    }

    /**
     * Factory method to create an error response FSMessage.
     *
     * @param code    The result code indicating the type of error.
     * @param message A descriptive error message.
     * @return An FSMessage representing an error response.
     */
    public static FSMessage createErrorResponse(ResultCode code, String message) {
        FSMessage error = new FSMessage((byte)1, code.getCode());
        error.addHeader("Message", message);
        return error;
    }

    /*=========================== Getters ============================*/

    /**
     * Gets the length of the message body.
     * @return The length of the body in bytes.
     */
    public long getBodyLength() {
        return bodyLength;
    }

    /**
     * Gets the message body.
     * @return The body as a byte array.
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Gets the operation code or result code of the message.
     * @return The operation code (for requests) or result code (for responses).
     */
    public int getOpCodeOrResult() {
        return opCodeOrResult;
    }

    /**
     * Gets the headers of the message.
     * @return A map containing the headers as key-value pairs.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets the type of the message.
     * @return The type (0 for request, 1 for response).
     */
    public byte getType() {
        return type;
    }

    /*=========================== Setters ============================*/

    /**
     * Sets the body of the message and updates the body length.
     *
     * @param body The content of the message as a byte array.
     */
    private void setBody(byte[] body) {
        this.body = body;
        this.bodyLength = body.length;
    }

    /**
     * Adds a header to the message.
     *
     * @param key      The header key.
     * @param fileName The header value.
     */
    private void addHeader(String key, String fileName) {
        headers.put(key, fileName);
    }

    /*=========================== Serialization ============================*/

    /**
     * Serializes the FSMessage to a DataOutputStream.
     *
     * @param out The DataOutputStream to write the message to.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(magicNumber);
        out.writeByte(type);
        out.writeInt(opCodeOrResult);
        out.writeInt(headers.size());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
            out.writeInt(keyBytes.length);
            out.write(keyBytes);
            out.writeInt(valueBytes.length);
            out.write(valueBytes);
        }
        out.writeLong(bodyLength);
        if (bodyLength > 0) {
            out.write(body);
        }
        out.flush();
    }

    /*=========================== Deserialization ============================*/

    /**
     * Deserializes an FSMessage from a DataInputStream.
     *
     * @param in The DataInputStream to read the message from.
     * @return The deserialized FSMessage.
     * @throws IOException If an I/O error occurs or if the magic number is invalid.
     */
    public static FSMessage readFrom(DataInputStream in) throws IOException {
        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            throw new IOException("Invalid magic number: " + Integer.toHexString(magic));
        }
        byte type = in.readByte();
        int opCodeOrResult = in.readInt();
        FSMessage message = new FSMessage(type, opCodeOrResult);

        int headerCount = in.readInt();
        for (int i = 0; i < headerCount; i++) {
            int keyLength = in.readInt();
            byte[] keyBytes = new byte[keyLength];
            in.readFully(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);

            int valueLength = in.readInt();
            byte[] valueBytes = new byte[valueLength];
            in.readFully(valueBytes);
            String value = new String(valueBytes, StandardCharsets.UTF_8);

            message.addHeader(key, value);
        }

        long bodyLength = in.readLong();
        message.bodyLength = bodyLength;
        if (bodyLength > 0) {
            byte[] body = new byte[(int) bodyLength];
            in.readFully(body);
            message.body = body;
        }

        return message;
    }

    /*=========================== Validation ============================*/
    /**
     * Validates the FSMessage based on its type (request or response).
     * Throws IllegalArgumentException if the message is invalid.
     */
    public void validateMessage() {
        switch (opCodeOrResult) {
            case 0 -> {
                validateRequest();
            }
            case 1 -> {
                validateResponse();
            }
            default -> throw new IllegalArgumentException("Unknown message type");
        }
    }

    /**
     * Validates a request FSMessage.
     * Ensures required headers are present and valid based on the operation code.
     * Throws IllegalArgumentException if validation fails.
     */
    private void validateRequest() {
        OpCode opCode = OpCode.fromCode(opCodeOrResult);

        String fileName = headers.get(FSHeader.FILE_NAME.key());
        String idStr = headers.get(FSHeader.ID.key());

        if (fileName == null && idStr == null) {
            throw new IllegalArgumentException("Either File-Name or ID header must be present");
        }

        if (fileName != null) {
            FileValidator.validateFileName(headers.get(FSHeader.FILE_NAME.key()));
        }

        if (idStr != null) {
            FileValidator.validateFileId(idStr);
        }

        if (opCode == OpCode.PUT) {
            FileValidator.validateFileSize(bodyLength);
            FileValidator.validateFileData(body, bodyLength);
        }
    }

    /**
     * Validates a response FSMessage.
     * Ensures required headers are present and valid based on the result code.
     * Throws IllegalArgumentException if validation fails.
     */
    private void validateResponse() {
        ResultCode resultCode = resultCode = ResultCode.fromCode(opCodeOrResult);

        switch (resultCode) {
            case SUCCESS -> {
                String idStr = headers.get(FSHeader.ID.key());
                String fileName = headers.get(FSHeader.FILE_NAME.key());

                FileValidator.validateFileName(fileName);
                FileValidator.validateFileId(idStr);
                FileValidator.validateFileData(body, bodyLength);
            }
            case NOT_FOUND, BAD_REQUEST, FORBIDDEN, SERVER_ERROR -> {
                String message = headers.get("Message");
                if (message == null || message.isEmpty()) {
                    throw new IllegalArgumentException("Missing error message in response");
                }
            }
        }
    }
}
