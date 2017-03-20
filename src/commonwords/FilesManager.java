package commonwords;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 *
 * @author Adam
 */
public class FilesManager {

    private final File dir;
    private boolean isDir = false;
    private File[] files;

    public FilesManager(File dir) {
        this.dir = dir;
        this.files = null;
        if (dir.isDirectory()) {
            this.files = dir.listFiles((File current, String name) -> new File(current, name).isFile());
            this.isDir = true;
        } else {
            this.isDir = false;
            System.err.println("Error: \"" + dir.getAbsolutePath() + "\" is not directory...");
        }
    }

    public File[] getFiles() {
        return files;
    }

    public File getDir() {
        return dir;
    }

    public boolean isDir() {
        return isDir;
    }

    public int getNumOfFiles() {
        return this.files.length;
    }

    public boolean existTmpOrRes() {
        return new File(dir.getAbsoluteFile() + "\\tmp").exists() | new File(dir.getAbsoluteFile() + "\\res").exists();
    }

    public boolean createTmpFolder() {
        return new File(dir.getAbsoluteFile() + "\\tmp").mkdir();
    }

    public boolean createResFolder() {
        return new File(dir.getAbsoluteFile() + "\\res").mkdir();
    }

    public void deleteTmpAndResFolder() {
        deleteDirectory(new File(dir.getAbsoluteFile() + "\\tmp"));
        deleteDirectory(new File(dir.getAbsoluteFile() + "\\res"));
    }

    public void deleteTmpFolder() {
        deleteDirectory(new File(dir.getAbsoluteFile() + "\\tmp"));
    }

    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
