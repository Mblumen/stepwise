package de.hd.stepwise.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class AtomicFileDownloaderTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void successfulDownloadAtomicallyReplacesTarget() throws Exception {
        File target = temporaryFolder.newFile("asset.bin");
        Files.write(target.toPath(), "old".getBytes(StandardCharsets.UTF_8));
        AtomicFileDownloader downloader = new AtomicFileDownloader(url ->
                new ByteArrayInputStream("new".getBytes(StandardCharsets.UTF_8)));

        downloader.download("https://example.com/asset", target);

        assertEquals("new", read(target));
        assertFalse(new File(target.getPath() + ".part").exists());
    }

    @Test
    public void failedDownloadKeepsExistingTargetAndRemovesPartialFile() throws Exception {
        File target = temporaryFolder.newFile("asset.bin");
        Files.write(target.toPath(), "old".getBytes(StandardCharsets.UTF_8));
        AtomicFileDownloader downloader = new AtomicFileDownloader(url -> failingStream());

        try {
            downloader.download("https://example.com/asset", target);
        } catch (IOException expected) {
            assertEquals("old", read(target));
            assertFalse(new File(target.getPath() + ".part").exists());
            return;
        }
        throw new AssertionError("Expected download to fail");
    }

    private static InputStream failingStream() {
        return new InputStream() {
            private int reads;

            @Override
            public int read() throws IOException {
                if (reads++ < 3) return 'x';
                throw new IOException("simulated interruption");
            }
        };
    }

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
