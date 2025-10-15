package dev.shoangenes.model;

/**
 * Enum representing headers used in FSMessage.
 */
public enum FSHeader {
    FILE_NAME("File-Name"),
    ID("ID"),
    MESSAGE("Message");

    /*========================== Fields ==========================*/

    private final String key;

    /*======================= Constructors =======================*/

    FSHeader(String key) {
        this.key = key;
    }

    /*======================== Getters ===========================*/

    public String key() {
        return key;
    }
}
