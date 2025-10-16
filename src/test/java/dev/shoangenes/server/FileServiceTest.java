package dev.shoangenes.server;

import dev.shoangenes.exception.DatabaseException;
import dev.shoangenes.file.FileManager;
import dev.shoangenes.model.FSMessage;
import dev.shoangenes.model.FileMetadata;
import dev.shoangenes.model.ResultCode;
import dev.shoangenes.repository.IFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.util.Optional;

/**
 * Unit tests for the FileService class.
 * Uses Mockito to mock dependencies and JUnit 5 for structuring tests.
 */
@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
    @Mock
    IFileRepository fileRepository;

    @Mock
    FileManager fileManager;

    FileService fileService;

    String fileName;
    byte[] fileContent;
    int fileId;

    @BeforeEach
    void setUp() throws DatabaseException {
        fileService = new FileService(fileRepository, fileManager);
        fileName = "testFile.txt";
        fileContent = fileName.getBytes();
        fileId = 1;
        when(fileRepository.getId(fileName)).thenReturn(fileId);
    }

    /**
     * Unit tests for FileService PUT operation.
     * Covers scenarios including new file save, existing file check, save failures, and invalid messages.
     */
    @Nested
    class putFileTest {
        FSMessage fsMessage;
        @BeforeEach
        void setup() {
            fsMessage = FSMessage.createPutRequest(fileName, fileContent);
        }
        /**
         * Test that a new file is saved successfully when it does not already exist.
         * Verifies that the repository and file manager are called as expected, and the response is SUCCESS.
         */
        @Test
        void testNewFile() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileName)).thenReturn(false);
            when(fileRepository.saveMapping(fileName)).thenReturn(1);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SUCCESS);
            verify(fileManager).saveFile(fileName, fileContent);
             verify(fileRepository).fileExists(fileName);
            verify(fileRepository).saveMapping(fileName);
        }

        /**
         * Test that attempting to save a file that already exists returns BAD_REQUEST and does not call saveFile or saveMapping.
         */
        @Test
        void testAnExistingFile() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileName)).thenReturn(true);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.BAD_REQUEST);
             verify(fileRepository).fileExists(fileName);
            verify(fileManager, never()).saveFile(fileName, fileContent);
            verify(fileRepository, never()).saveMapping(fileName);
        }

        /**
         * Test that if saving the file fails (IOException), the response is SERVER_ERROR and no mapping is saved.
         */
        @Test
        void testSaveFileOperationFailed() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileName)).thenReturn(false);
            doThrow(IOException.class).when(fileManager).saveFile(fileName, fileContent);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
            verify(fileRepository).fileExists(fileName);
            verify(fileManager).saveFile(fileName, fileContent);
            verify(fileRepository, never()).saveMapping(fileName);
        }

        /**
         * Test that if saving the mapping fails (DatabaseException), the file is deleted and the response is SERVER_ERROR.
         */
        @Test
        void testSaveMappingOperationFailed() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileName)).thenReturn(false);
            doNothing().when(fileManager).saveFile(fileName, fileContent);
            when(fileRepository.saveMapping(fileName)).thenThrow(DatabaseException.class);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
            verify(fileRepository).fileExists(fileName);
            verify(fileManager).saveFile(fileName, fileContent);
            verify(fileRepository).saveMapping(fileName);
            verify(fileManager).deleteFile(fileName);
        }

        /**
         * Test that an unexpected runtime exception during file save returns SERVER_ERROR.
         */
        @Test
        void testUnexpectedExceptionReturnsServerError() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileName)).thenReturn(false);
            doThrow(new RuntimeException("Unexpected error")).when(fileManager).saveFile(fileName, fileContent);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
             verify(fileRepository).fileExists(fileName);
            verify(fileManager).saveFile(fileName, fileContent);
            verify(fileRepository, never()).saveMapping(fileName);
        }
    }

    /**
     * Unit tests for FileService GET operation.
     * Covers scenarios including existing file retrieval, non-existing file handling, retrieval failures, and database exceptions.
     */
    @Nested
    class getFileTest {
        FSMessage fsMessage;

        @BeforeEach
        void setup() {
            fsMessage = FSMessage.createGetRequest(fileName);
        }

        /**
         * Test that retrieving an existing file returns SUCCESS and the correct file content.
         */
        @Test
        void testGetExistingFile() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(true);
            when(fileManager.getFile(fileName)).thenReturn(Optional.of(fileContent));

            FSMessage response = fileService.processRequest(fsMessage);

            assertThat(ResultCode.fromCode(response.getOpCodeOrResult())).isEqualTo(ResultCode.SUCCESS);
            assertThat(response.getBody()).isEqualTo(fileContent);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager).getFile(fileName);
        }

        /**
         * Test that attempting to retrieve a non-existing file returns NOT_FOUND.
         */
        @Test
        void testGetNonExistingFile() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(false);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.NOT_FOUND);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager, never()).getFile(fileName);
        }

        /**
         * Test that if the file is not found in storage despite existing in the repository, the response is NOT_FOUND.
         */
        @Test
        void testGetFileNotFoundInStorage() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(true);
            when(fileManager.getFile(fileName)).thenReturn(Optional.empty());

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.NOT_FOUND);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager).getFile(fileName);
        }

        /**
         * Test that if retrieving the file fails (IOException), the response is SERVER_ERROR.
         */
        @Test
        void testGetFileOperationFailed() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(true);
            when(fileManager.getFile(fileName)).thenThrow(IOException.class);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager).getFile(fileName);
        }

        /**
         * Test that if checking file existence fails (DatabaseException), the response is SERVER_ERROR.
         */
        @Test
        void testGetFileDatabaseException() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenThrow(DatabaseException.class);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager, never()).getFile(fileName);
        }
    }

    @Nested
    class deleteFileTest {
        FSMessage fsMessage;

        @BeforeEach
        void setup() {
            fsMessage = FSMessage.createDeleteRequest(fileName);
        }

        /**
         * Test that deleting an existing file returns SUCCESS and removes both file and mapping.
         */
        @Test
        void testDeleteExistingFile() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(true);
            when(fileManager.deleteFile(fileName)).thenReturn(Optional.of(fileContent));
            when(fileRepository.removeMapping(fileId)).thenReturn(new FileMetadata(fileId, fileName));

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SUCCESS);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager).deleteFile(fileName);
            verify(fileRepository).removeMapping(fileId);
        }

        /**
         * Test that deleting a non-existing file returns NOT_FOUND and does not call deleteFile or removeMapping.
         */
        @Test
        void testDeleteNonExistingFile() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(false);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.NOT_FOUND);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager, never()).deleteFile(fileName);
            verify(fileRepository, never()).removeMapping(fileId);
        }

        /**
         * Test that if deleting the file in storage fails (IOException), the response is SERVER_ERROR and mapping is not removed.
         */
        @Test
        void testDeleteNonExistingFileInStorage() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(true);
            doThrow(IOException.class).when(fileManager).deleteFile(fileName);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager).deleteFile(fileName);
            verify(fileRepository, never()).removeMapping(fileId);
        }

        /**
         * Test that if checking file existence throws DatabaseException, the response is SERVER_ERROR and no further actions are taken.
         */
        @Test
        void testDeleteFileDatabaseExceptionOnExists() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenThrow(DatabaseException.class);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager, never()).deleteFile(fileName);
            verify(fileRepository, never()).removeMapping(fileId);
        }

        /**
         * Test that if an IOException occurs during file deletion, the response is SERVER_ERROR and mapping is not removed.
         */
        @Test
        void testDeleteFileIOExeptionOnDelete() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(true);
            doThrow(IOException.class).when(fileManager).deleteFile(fileName);

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager).deleteFile(fileName);
            verify(fileRepository, never()).removeMapping(fileId);
        }

        /**
         * Test that if a DatabaseException occurs during removeMapping, the file is restored and SERVER_ERROR is returned.
         */
        @Test
        void testDeleteFileDatabaseExceptionOnRemoveMapping() throws DatabaseException, IOException {
            when(fileRepository.fileExists(fileId)).thenReturn(true);
            when(fileManager.deleteFile(fileName)).thenReturn(Optional.of(fileContent));
            doThrow(DatabaseException.class).when(fileRepository).removeMapping(fileId);
            doNothing().when(fileManager).saveFile(fileName, fileContent); // Para restaurar el archivo

            ResultCode code = ResultCode.fromCode(
                    fileService.processRequest(fsMessage)
                            .getOpCodeOrResult());

            assertThat(code).isEqualTo(ResultCode.SERVER_ERROR);
            verify(fileRepository).fileExists(fileId);
            verify(fileManager).deleteFile(fileName);
            verify(fileRepository).removeMapping(fileId);
            verify(fileManager).saveFile(fileName, fileContent);
        }
    }
}
