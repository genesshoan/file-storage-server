package dev.shoangenes.file;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages concurrent access to files using read-write locks.
 * Implements a singleton pattern to ensure a single instance.
 */
class FileAccessManager {
    /*============================= Singleton Instance ========================*/

    private static final FileAccessManager instance = new FileAccessManager();

    /*================================ Fields =================================*/

    private ConcurrentHashMap<String, ReentrantReadWriteLock> fileLocks;

    /*=========================== Constructor ================================*/

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private FileAccessManager() {
        fileLocks = new ConcurrentHashMap<>();
    }

    /*=========================== Public Methods =============================*/

    /**
     * Get the singleton instance of FileAccessManager.
     *
     * @return The singleton instance.
     */
    public static FileAccessManager getInstance() {
        return instance;
    }

    /**
     * Acquire a read lock for the specified file.
     *
     * @param fileName The name of the file to lock.
     */
    public void acquireReadLock(String fileName) {
        ReentrantReadWriteLock lock = fileLocks.computeIfAbsent(fileName, k -> new ReentrantReadWriteLock());
        lock.readLock().lock();
    }

    /**
     * Release a read lock for the specified file.
     *
     * @param fileName The name of the file to unlock.
     */
    public void releaseReadLock(String fileName) {
        if (fileLocks.containsKey(fileName)) {
            fileLocks.get(fileName).readLock().unlock();
        }
    }

    /**
     * Acquire a write lock for the specified file.
     *
     * @param fileName The name of the file to lock.
     */
    public void acquireWriteLock(String fileName) {
        ReentrantReadWriteLock lock = fileLocks.computeIfAbsent(fileName, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
    }

    /**
     * Release a write lock for the specified file.
     *
     * @param fileName The name of the file to unlock.
     */
    public void releaseWriteLock(String fileName) {
        if (fileLocks.containsKey(fileName)) {
            fileLocks.get(fileName).writeLock().unlock();
        }
    }
}
