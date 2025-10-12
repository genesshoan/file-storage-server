package dev.shoangenes.model;

/**
 * Enum representing various operation codes (OpCodes) for file operations.
 * Each enum constant is associated with an integer code.
 */
public enum OpCode {
    /*============================ Enum Constants ============================*/

    PUT(1),
    GET(2),
    DELETE(3);

    /*================================ Fields =================================*/

    private final int code;

    /*=========================== Constructor ================================*/

    OpCode(int code) {
        this.code = code;
    }

    /*=========================== Public Methods =============================*/

    /**
     * Get the integer code associated with this OpCode.
     *
     * @return The integer code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the OpCode enum corresponding to the given integer code.
     *
     * @param code The integer code to look up.
     * @return The matching OpCode enum.
     * @throws IllegalArgumentException If no matching OpCode is found.
     */
    public static OpCode fromCode(int code) {
        for (OpCode op : OpCode.values()) {
            if (op.getCode() == code) {
                return op;
            }
        }
        throw new IllegalArgumentException("No matching OpCode for code: " + code);
    }
}
