import java.io.*;
import java.util.*;

/**
 * MP04 - Count Valid Dataset Rows
 * Dataset: Pearson VUE Exam Results - University of Perpetual Help System Molino
 *
 * Program Logic:
 * Reads the Pearson VUE CSV, skips the preamble, and validates every data row
 * against the 8 meaningful display columns. A row is valid when none of those
 * columns are empty. A two-pass approach is used: the first pass collects all
 * data rows and detects which columns are actually used; the second pass validates
 * and categorizes each row. Invalid rows are listed in a table with [EMPTY]
 * markers, and all long values are truncated for clean terminal alignment.
 */
public class MP04_CountValidRows {

    static final String[] COL_LABELS = {
        "Candidate", "Student/ Faculty/ NTE", "Exam",
        "Language", "Exam Date", "Score", "Result", "Time Used"
    };
    static final int[] COL_MAX = { 20, 10, 30, 8, 10, 5, 6, 13 };

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

    static String truncate(String str, int maxLen) {
        if (str == null) str = "";
        return str.length() > maxLen ? str.substring(0, maxLen - 1) + "\u2026" : str;
    }

    /** isValidRow - Returns true if all display columns have a non-empty value. */
    static boolean isValidRow(String[] row, int[] colIdx) {
        for (int d = 0; d < colIdx.length; d++) {
            int ci  = colIdx[d];
            String v = (ci >= 0 && ci < row.length) ? row[ci] : "";
            if (v.trim().isEmpty()) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("============================================================");
        System.out.println("     MP04 - Count Valid Dataset Rows                        ");
        System.out.println("     Pearson VUE Exam Results                               ");
        System.out.println("============================================================");
        System.out.print("Enter the CSV dataset file path: ");
        String filePath = scanner.nextLine().trim();

        String[] csvHeader = null;
        int[]    colIdx    = new int[COL_LABELS.length];
        Arrays.fill(colIdx, -1);

        ArrayList<String[]> dataBuffer  = new ArrayList<>();
        ArrayList<String[]> invalidRows = new ArrayList<>();
        ArrayList<Integer>  invalidNums = new ArrayList<>();
        int validCount = 0, invalidCount = 0;

        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            String line;

            // ── Pass 1: collect header + data rows ────────────────────────
            while ((line = reader.readLine()) != null) {
                line = line.replace("\uFEFF", "").replace("\r", "");
                if (line.trim().isEmpty()) continue;
                String[] fields = parseCSVLine(line);

                if (csvHeader == null) {
                    if (fields.length > 0 && fields[0].equalsIgnoreCase("Candidate")) {
                        csvHeader = fields;
                        for (int d = 0; d < COL_LABELS.length; d++)
                            for (int c = 0; c < csvHeader.length; c++)
                                if (csvHeader[c].trim().equalsIgnoreCase(COL_LABELS[d])) { colIdx[d] = c; break; }
                    }
                    continue;
                }
                if (fields[0].trim().isEmpty()) continue;
                dataBuffer.add(fields);
            }
            reader.close();

            if (csvHeader == null) { System.out.println("Error: Header row not found."); return; }

            // ── Pass 2: validate rows ─────────────────────────────────────
            int rowNum = 0;
            for (String[] fields : dataBuffer) {
                rowNum++;
                if (isValidRow(fields, colIdx)) {
                    validCount++;
                } else {
                    invalidCount++;
                    invalidRows.add(fields);
                    invalidNums.add(rowNum);
                }
            }

            int totalRows = dataBuffer.size();
            double pct = (totalRows > 0) ? (validCount * 100.0 / totalRows) : 0;

            // ── Summary ───────────────────────────────────────────────────
            System.out.println("\nFile   : " + filePath);
            System.out.println("\u2500".repeat(50));
            System.out.printf("  %-32s %d%n",     "Total columns (checked):", COL_LABELS.length);
            System.out.printf("  %-32s %d%n",     "Total data rows read:",    totalRows);
            System.out.printf("  %-32s %d%n",     "Valid rows:",              validCount);
            System.out.printf("  %-32s %d%n",     "Invalid (incomplete) rows:", invalidCount);
            System.out.printf("  %-32s %.2f%%%n", "Data completeness:",       pct);
            System.out.println("\u2500".repeat(50));

            if (invalidCount == 0) {
                System.out.println("\nAll rows are valid. No missing fields detected."); return;
            }

            // ── Invalid row detail table ──────────────────────────────────
            System.out.println("\nInvalid Row Details ([EMPTY] = missing field):");

            int numCols = COL_LABELS.length;
            int[] colW  = new int[numCols];
            for (int d = 0; d < numCols; d++) colW[d] = Math.max(COL_LABELS[d].length(), 7);

            String[][] displayRows = new String[invalidRows.size()][numCols];
            for (int r = 0; r < invalidRows.size(); r++) {
                String[] row = invalidRows.get(r);
                for (int d = 0; d < numCols; d++) {
                    int ci  = colIdx[d];
                    String raw = (ci >= 0 && ci < row.length) ? row[ci] : "";
                    String val = raw.trim().isEmpty() ? "[EMPTY]" : truncate(raw, COL_MAX[d]);
                    displayRows[r][d] = val;
                    colW[d] = Math.max(colW[d], val.length());
                }
            }

            int rowNumW = Math.max(4, String.valueOf(totalRows).length());
            StringBuilder sb = new StringBuilder("+-" + "-".repeat(rowNumW) + "-+");
            for (int w : colW) sb.append("-".repeat(w + 2)).append("+");
            String sep = sb.toString();

            System.out.println("\n" + sep);
            System.out.printf("| %-" + rowNumW + "s |", "Row#");
            for (int d = 0; d < numCols; d++) System.out.printf(" %-" + colW[d] + "s |", COL_LABELS[d]);
            System.out.println();
            System.out.println(sep);

            for (int r = 0; r < displayRows.length; r++) {
                System.out.printf("| %-" + rowNumW + "d |", invalidNums.get(r));
                for (int d = 0; d < numCols; d++) System.out.printf(" %-" + colW[d] + "s |", displayRows[r][d]);
                System.out.println();
            }
            System.out.println(sep);

        } catch (FileNotFoundException e) { System.out.println("Error: File not found -> " + filePath);
        } catch (IOException e)           { System.out.println("Error reading file: " + e.getMessage());
        } finally                         { scanner.close(); }
    }
}
