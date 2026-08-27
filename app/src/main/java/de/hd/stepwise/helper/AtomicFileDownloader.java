package de.hd.stepwise.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AtomicFileDownloader {

    interface StreamOpener {
        InputStream open(String url) throws IOException;
    }

    private final StreamOpener streamOpener;

    AtomicFileDownloader(StreamOpener streamOpener) {
        this.streamOpener = streamOpener;
    }

    public String download(String url, File target) throws IOException {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create asset directory " + parent);
        }
        File partial = new File(target.getPath() + ".part");
        try {
            try (InputStream input = streamOpener.open(url);
                 FileOutputStream output = new FileOutputStream(partial)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
                output.getFD().sync();
            }
            replace(partial, target);
            return target.getAbsolutePath();
        } finally {
            if (partial.exists() && !partial.delete()) {
                partial.deleteOnExit();
            }
        }
    }

    private static void replace(File partial, File target) throws IOException {
        try {
            Files.move(partial.toPath(), target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(partial.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
