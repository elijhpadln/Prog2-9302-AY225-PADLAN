import java.io.*;
import java.util.*;

/**
 * MP02 - Display First 10 Rows of Dataset
 * Dataset: Pearson VUE Exam Results - University of Perpetual Help System Molino
 *
 * Program Logic:
 * Reads the Pearson VUE CSV export, skips the preamble metadata lines, locates
 * the true header row ("Candidate"), then reads the first 10 data rows. Each
 * column is capped at a maximum display width (long exam names are truncated
 * with an ellipsis) so the table stays aligned on a standard terminal.
 */
public class MP02_DisplayFirst10Rows {

    // ── Column display configuration ──────────────────────────────────────────
    // label: header text to match in the CSV; maxW: max chars to display per cell
    static final String[] COL_LABELS = {
        "Candidate", "Student/ Faculty/ NTE", "Exam",
        "Language", "Exam Date", "Score", "Result", "Time Used"
    };
    static final int[] COL_MAX = { 20, 10, 30, 8, 10, 5, 6, 13 };

    /** parseCSVLine - Splits a CSV line into fields, respecting quoted values. */
    static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (char c : line.toCharArray()) {
            if (c == '"')              { inQ = !inQ; }
            else if (c == ',' && !inQ) { fields.add(cur.toString().trim()); cur.setLength(0); }
            else                       { cur.append(c); }
        }
        fields.add(cur.toString().trim());
        return fields.toArray(new String[0]);
    }

    /**
     * truncate - Clips str to maxLen characters; appends ellipsis if clipped.
     * Keeps the table width predictable regardless of long exam names.
     */
    static String truncate(String str, int maxLen) {
        if (str == null) str = "";
        return str.length() > maxLen ? str.substring(0, maxLen - 1) + "\u2026" : str;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("============================================================");
        System.out.println("     MP02 - Display First 10 Rows of Dataset                ");
        System.out.println("     Pearson VUE Exam Results                               ");
        System.out.println("============================================================");
        System.out.print("Enter the CSV dataset file path: ");
        String filePath = scanner.nextLine().trim();

        String[] csvHeader = null;       // Full header row from CSV
        int[]    colIdx    = new int[COL_LABELS.length]; // Maps each display col to CSV index
        Arrays.fill(colIdx, -1);

        ArrayList<String[]> rows = new ArrayList<>(); // First 10 data rows (raw)

        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.replace("\uFEFF", "").replace("\r", "");
                if (line.trim().isEmpty()) continue;
                String[] fields = parseCSVLine(line);

                // Detect true header row
                if (csvHeader == null) {
                    if (fields.length > 0 && fields[0].equalsIgnoreCase("Candidate")) {
                        csvHeader = fields;
                        // Map display column labels to their CSV column indices
                        for (int d = 0; d < COL_LABELS.length; d++) {
                            for (int c = 0; c < csvHeader.length; c++) {
                                if (csvHeader[c].trim().equalsIgnoreCase(COL_LABELS[d])) {
                                    colIdx[d] = c; break;
                                }
                            }
                        }
                    }
                    continue; // Preamble — keep scanning
                }

                if (fields[0].trim().isEmpty()) continue;
                if (rows.size() < 10) rows.add(fields);
                else break;
            }
            reader.close();

            if (csvHeader == null) { System.out.println("Error: Header row not found."); return; }
            if (rows.isEmpty())    { System.out.println("No data rows found.");           return; }

            // ── Build display cells with truncation ───────────────────────
            int numCols = COL_LABELS.length;
            int[] colW  = new int[numCols];
            for (int d = 0; d < numCols; d++) colW[d] = COL_LABELS[d].length(); // seed with label width

            String[][] displayRows = new String[rows.size()][numCols];
            for (int r = 0; r < rows.size(); r++) {
                String[] row = rows.get(r);
                for (int d = 0; d < numCols; d++) {
                    int ci  = colIdx[d];
                    String raw = (ci >= 0 && ci < row.length) ? row[ci] : "";
                    String val = truncate(raw, COL_MAX[d]);
                    displayRows[r][d] = val;
                    colW[d] = Math.max(colW[d], val.length());
                }
            }

            // ── Build separator ───────────────────────────────────────────
            StringBuilder sb = new StringBuilder("+");
            for (int w : colW) sb.append("-".repeat(w + 2)).append("+");
            String sep = sb.toString();

            // ── Print table ───────────────────────────────────────────────
            System.out.println("\nFile  : " + filePath);
            System.out.println("Rows  : First " + rows.size() + " data rows\n");
            System.out.println(sep);

            System.out.print("|");
            for (int d = 0; d < numCols; d++)
                System.out.printf(" %-" + colW[d] + "s |", COL_LABELS[d]);
            System.out.println();
            System.out.println(sep);

            for (String[] row : displayRows) {
                System.out.print("|");
                for (int d = 0; d < numCols; d++)
                    System.out.printf(" %-" + colW[d] + "s |", row[d]);
                System.out.println();
            }
            System.out.println(sep);
            System.out.println("Total rows displayed: " + rows.size());

        } catch (FileNotFoundException e) { System.out.println("Error: File not found -> " + filePath);
        } catch (IOException e)           { System.out.println("Error reading file: " + e.getMessage());
        } finally                         { scanner.close(); }
    }
}
