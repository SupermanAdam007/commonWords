package commonwords;

import java.io.File;

/**
 *
 * @author Adam
 */
public class CommonWords {

    public static String mainPath = "c:\\Users\\Adam\\Documents\\NetBeansProjects\\commonWords\\test\\";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FilesManager fileMan = new FilesManager(new File(mainPath));

        System.out.println("Path: " + mainPath);
        System.out.println("Number of files: " + fileMan.getNumOfFiles());

        if (fileMan.isDir()) {
            if (fileMan.existTmpOrRes()) {
                fileMan.deleteTmpAndResFolder();
            }
            fileMan.createTmpFolder();
            fileMan.createResFolder();
            System.out.println("Starting first scan of files...");
            for (File f : fileMan.getFiles()) {
                if (f.isFile()) {
                    System.out.println("file: " + f.getName());
                    Tmp.parseFileToTmp(f);
                }
            }
            Tmp.makeHist(fileMan);
            fileMan.deleteTmpFolder();
        }
    }

}
