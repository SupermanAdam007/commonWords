package commonwords;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 *
 * @author Adam
 */
public class Tmp {

    private static final String tmpDir = "tmp";
    private static final String tmpFileName = "tmp.txt";

    public Tmp() {
    }

    public static void parseFileToTmp(File f) {
        BufferedReader buffr;
        try {
            buffr = new BufferedReader(new FileReader(f));
            FileWriter fw = new FileWriter(f.getParent() + "\\" + tmpDir + "\\" + tmpFileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            String line;
//            int[] len = new int[]{10};
            int[] len = new int[]{10};
            ArrayList<String> blackList = getBlackList();
            boolean blackListed = false;

            while ((line = buffr.readLine()) != null) {
                for (int i : len) {
                    for (int j = 0; j < line.length() - i; j += i) {
                        //for (int k = 0; k < i / 2; k++) {
                        //String resResLine = line.substring(Math.max(j + k, 0), Math.min(j + i + k, line.length()));
                        String resResLine = line.substring(Math.max(j, 0), Math.min(j + i, line.length()));
                        blackListed = false;
                        for (String s : blackList) {
                            if (resResLine.contains(s)) {
                                blackListed = true;
                                break;
                            }
                        }
                        resResLine = resResLine.trim();
                        if (blackListed || resResLine.replace(" ", "").length() < len[0]) {
                            continue;
                        }

//                            System.out.println(resResLine);
                        out.write(resResLine + "\n");
                        //  }
                    }
                }
            }
            out.close();
            bw.close();
            fw.close();
            buffr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> getBlackList() {
        ArrayList<String> blackList = new ArrayList<>();
        blackList.add("CTIO");
        blackList.add("TION");
        blackList.add("ION ");
        blackList.add("FUNC");
        blackList.add("TRIN");
        blackList.add("STRI");
        blackList.add("RING");
        blackList.add("ETUR");
        blackList.add("DEFI");
        blackList.add("NUMB");
        blackList.add("UMBE");
        blackList.add("VAR ");
        blackList.add("�");
        blackList.add("MANIF");
        blackList.add("OCTYP");
        blackList.add("EXT/HTM");
        blackList.add("FAQ");
        blackList.add("REPLA");
        blackList.add("PLACE");
        return blackList;
    }

    public static void makeHist(FilesManager fm) {
        try {
            int numOfFiles = fm.getFiles().length;
//            System.out.println("fm.getDir() + \"\\\\\" + tmpDir + \"\\\\\" + tmpFileName = " + fm.getDir() + "\\" + tmpDir + "\\" + tmpFileName);
            BufferedReader tmpReader = new BufferedReader(new FileReader(fm.getDir() + "\\" + tmpDir + "\\" + tmpFileName));

            PrintWriter[] writers = new PrintWriter[numOfFiles];
            for (int i = 0; i < numOfFiles; i++) {
                writers[i] = new PrintWriter(new BufferedWriter(new FileWriter(fm.getDir() + "\\res\\" + (i + 1) + ".txt", true)));
            }

            boolean[] isInFile = new boolean[numOfFiles];
            for (int i = 0; i < numOfFiles; i++) {
                isInFile[i] = false;
            }

            BufferedReader[] readersTmp;

            String lineTmp, lineFile;
            while ((lineTmp = tmpReader.readLine()) != null) {
                BufferedReader[] readers = new BufferedReader[numOfFiles];
                for (int i = 0; i < numOfFiles; i++) {
//                System.out.println("--- " + fm.getFiles()[i].getAbsolutePath());
                    readers[i] = new BufferedReader(new FileReader(fm.getFiles()[i].getAbsolutePath()));
                }
                for (int i = 0; i < numOfFiles; i++) {
                    while ((lineFile = readers[i].readLine()) != null) {
//                        System.out.println("[lineTmp: " + lineTmp + ", lineFile: " + lineFile + "]");
                        if (lineFile.indexOf(lineTmp) != -1) {
                            isInFile[i] = true;
//                            System.out.println("+++prdel");
                            break;
                        }
                    }
                }

                int sum = 0;
                for (int i = 0; i < numOfFiles; i++) {
                    if (isInFile[i]) {
                        sum++;
                    }
                }
                if (sum != 0) {
//                    System.out.println("sum = " + sum);
                    writers[sum - 1].write(lineTmp + "\n");
                }
                for (int i = 0; i < numOfFiles; i++) {
                    isInFile[i] = false;
                }
            }

            for (int i = 0; i < numOfFiles; i++) {
                writers[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int MAPSIZE = 4 * 1024; // 4K - make this * 1024 to 4MB in a real system.

    private static boolean searchFor(String grepfor, Path path) throws IOException {
        final byte[] tosearch = grepfor.getBytes(StandardCharsets.UTF_8);
        StringBuilder report = new StringBuilder();
        int padding = 1; // need to scan 1 character ahead in case it is a word boundary.
        int linecount = 0;
        int matches = 0;
        boolean inword = false;
        boolean scantolineend = false;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            final long length = channel.size();
            int pos = 0;
            while (pos < length) {
                long remaining = length - pos;
                // int conversion is safe because of a safe MAPSIZE.. Assume a reaosnably sized tosearch.
                int trymap = MAPSIZE + tosearch.length + padding;
                int tomap = (int) Math.min(trymap, remaining);
                // different limits depending on whether we are the last mapped segment.
                int limit = trymap == tomap ? MAPSIZE : (tomap - tosearch.length);
                MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, pos, tomap);
//                System.out.println("Mapped from " + pos + " for " + tomap);
                pos += (trymap == tomap) ? MAPSIZE : tomap;
                for (int i = 0; i < limit; i++) {
                    final byte b = buffer.get(i);
                    if (scantolineend) {
                        if (b == '\n') {
                            scantolineend = false;
                            inword = false;
                            linecount++;
                        }
                    } else if (b == '\n') {
                        linecount++;
                        inword = false;
                    } else if (b == '\r' || b == ' ') {
                        inword = false;
                    } else if (!inword) {
                        if (wordMatch(buffer, i, tomap, tosearch)) {
                            matches++;
                            i += tosearch.length - 1;
                            if (report.length() > 0) {
                                report.append(", ");
                            }
                            report.append(linecount);
                            scantolineend = true;
                        } else {
                            inword = true;
                            break;
                        }
                    }
                }

            }
        }
        return inword;
    }

    private static boolean wordMatch(MappedByteBuffer buffer, int pos, int tomap, byte[] tosearch) {
        //assume at valid word start.
        for (int i = 0; i < tosearch.length; i++) {
            if (tosearch[i] != buffer.get(pos + i)) {
                return false;
            }
        }
        byte nxt = (pos + tosearch.length) == tomap ? (byte) ' ' : buffer.get(pos + tosearch.length);
        return nxt == ' ' || nxt == '\n' || nxt == '\r';
    }
}
