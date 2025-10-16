package dev.shoangenes.model;

/**
 * Enum representing headers used in FSMessage.
 */
public enum FSHeader {
    /*============================ Enum Values ============================*/
    FILE_NAME(1),
    ID(2),
    MESSAGE(3);

    /*============================ Fields ============================*/

    private final int code;

    /*============================ Constructors ============================*/

    FSHeader(int code) { this.code = code; }

    /*============================ Getters ============================*/

    public int getCode() { return code; }

    /**
     * Get FSHeader enum from code.
     * @param code the code to look for
     * @return the corresponding FSHeader enum
     * @throws IllegalArgumentException if the code is invalid
     */
    public static FSHeader fromCode(int code) {
        for (FSHeader header : FSHeader.values()) {
            if (header.code == code) {
                return header;
            }
        }
        throw new IllegalArgumentException("Invalid FSHeader code: " + code);
    }
}
