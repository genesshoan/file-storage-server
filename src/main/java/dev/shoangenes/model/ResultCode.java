package dev.shoangenes.model;

/**
 * Enum representing various result codes for operations.
 * Each enum constant is associated with an integer code.
 */
public enum ResultCode {
    /*============================ Enum Constants ============================*/
    SUCCESS(200),
    BAD_REQUEST(400),
    FORBIDDEN(403),
    NOT_FOUND(404),
    SERVER_ERROR(500);

    /*================================ Fields =================================*/

    private final int code;

    /*=========================== Constructor ================================*/

    /**
     * Constructor to initialize the ResultCode with its associated integer code.
     *
     * @param code The integer code representing the result.
     */
    ResultCode(int code) {
        this.code = code;
    }

    /*=========================== Public Methods =============================*/

    /**
     * Get the integer code associated with this ResultCode.
     *
     * @return The integer code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the ResultCode enum corresponding to the given integer code.
     *
     * @param code The integer code to look up.
     * @return The matching ResultCode enum.
     * @throws IllegalArgumentException If no matching ResultCode is found.
     */
    public ResultCode fromCode(int code) {
        for (ResultCode rc : ResultCode.values()) {
            if (rc.getCode() == code) {
                return rc;
            }
        }
        throw new IllegalArgumentException("No matching ResultCode for code: " + code);
    }
}
