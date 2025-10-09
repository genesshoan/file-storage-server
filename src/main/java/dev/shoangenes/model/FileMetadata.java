package dev.shoangenes.model;

public class FileMetadata {
    /*============================== Fields ============================*/

    private int id;
    private  String name;

    /*=========================== Constructors =========================*/

    /**
     * Constructs a FileMetadata object with the specified ID and name.
     * @param id the file ID.
     * @param name the file name.
     */
    public FileMetadata(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /*=========================== Public Methods ========================*/

    /**
     * Returns the file ID.
     * @return the file ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the file name.
     * @return the file name.
     */
    public String getName() {
        return name;
    }
}
