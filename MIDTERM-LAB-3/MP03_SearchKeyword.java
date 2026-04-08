import java.io.*;
import java.util.*;

/**
 * MP03 - Search for a Keyword in the Dataset
 * Dataset: Pearson VUE Exam Results - University of Perpetual Help System Molino
 *
 * Program Logic:
 * Prompts for a CSV path and a search keyword. Skips the Pearson VUE preamble,
 * then performs a case-insensitive search across every meaningful field of every
 * data row. Matching rows are collected and displayed in a formatted table with
 * a "Matched In" column showing which field(s) triggered the match. All long
 * values are truncated to keep the output aligned in a standard terminal.
 */
public class MP03_SearchKeyword {

    // ── Column display configuration ──────────────────────────────────────────
    static final String[] COL_LABELS = {
        "Candidate", "Student/ Faculty/ NTE", "Exam",
        "Language", "Exam Date", "Score", "Result", "Time Used"
    };
    static final int[] COL_MAX = { 20, 10, 30, 8, 10, 5, 6, 13 };
    static final int MATCH_MAX = 25; // max width for "Matched In" column

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

    /** truncate - Clips str to maxLen; appends ellipsis if clipped. */
    static String truncate(String str, int maxLen) {
        if (str == null) str = "";
        return str.length() > maxLen ? str.substring(0, maxLen - 1) + "\u2026" : str;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("============================================================");
        System.out.println("     MP03 - Search for a Keyword                            ");
        System.out.println("     Pearson VUE Exam Results                               ");
        System.out.println("============================================================");
        System.out.print("Enter the CSV dataset file path: ");
        String filePath = scanner.nextLine().trim();
        System.out.print("Enter keyword to search: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) { System.out.println("Error: Keyword cannot be empty."); scanner.close(); return; }

        String kwLower = keyword.toLowerCase();

        String[] csvHeader = null;
        int[]    colIdx    = new int[COL_LABELS.length]; // CSV column indices for display cols
        int[]    allIdx    = null;                        // All non-empty CSV column indices (for search)
        Arrays.fill(colIdx, -1);

        ArrayList<String[]> allRows     = new ArrayList<>();
        ArrayList<String[]> matchedRows = new ArrayList<>();
        ArrayList<String>   matchInfo   = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.replace("\uFEFF", "").replace("\r", "");
                if (line.trim().isEmpty()) continue;
                String[] fields = parseCSVLine(line);

                if (csvHeader == null) {
                    if (fields.length > 0 && fields[0].equalsIgnoreCase("Candidate")) {
                        csvHeader = fields;
                        // Map display labels to CSV indices
                        for (int d = 0; d < COL_LABELS.length; d++)
                            for (int c = 0; c < csvHeader.length; c++)
                                if (csvHeader[c].trim().equalsIgnoreCase(COL_LABELS[d])) { colIdx[d] = c; break; }
                        // Build list of ALL non-empty header column indices (for search scope)
                        List<Integer> tmp = new ArrayList<>();
                        for (int c = 0; c < csvHeader.length; c++)
                            if (!csvHeader[c].trim().isEmpty()) tmp.add(c);
                        allIdx = tmp.stream().mapToInt(Integer::intValue).toArray();
                    }
                    continue;
                }

                if (fields[0].trim().isEmpty()) continue;
                allRows.add(fields);

                // Search every non-empty column for the keyword
                List<String> hits = new ArrayList<>();
                for (int ci : allIdx) {
                    String val = (ci < fields.length) ? fields[ci] : "";
                    if (val.toLowerCase().contains(kwLower)) hits.add(csvHeader[ci]);
                }

                if (!hits.isEmpty()) {
                    matchedRows.add(fields);
                    matchInfo.add(truncate(String.join(", ", hits), MATCH_MAX));
                }
            }
            reader.close();

            if (csvHeader == null) { System.out.println("Error: Header row not found."); return; }

            // ── Summary ───────────────────────────────────────────────────
            System.out.println("\nFile     : " + filePath);
            System.out.println("Keyword  : \"" + keyword + "\"");
            System.out.println("Searched : " + allRows.size() + " records");
            System.out.println("Matches  : " + matchedRows.size() + " records");

            if (matchedRows.isEmpty()) {
                System.out.println("\nNo records matched the keyword \"" + keyword + "\"."); return;
            }

            // ── Build display cells with truncation ───────────────────────
            int numCols = COL_LABELS.length;
            int[] colW  = new int[numCols];
            for (int d = 0; d < numCols; d++) colW[d] = COL_LABELS[d].length();
            int matchColW = "Matched In".length();

            String[][] displayRows = new String[matchedRows.size()][numCols];
            for (int r = 0; r < matchedRows.size(); r++) {
                String[] row = matchedRows.get(r);
                for (int d = 0; d < numCols; d++) {
                    int ci  = colIdx[d];
                    String raw = (ci >= 0 && ci < row.length) ? row[ci] : "";
                    String val = truncate(raw, COL_MAX[d]);
                    displayRows[r][d] = val;
                    colW[d] = Math.max(colW[d], val.length());
                }
                matchColW = Math.max(matchColW, matchInfo.get(r).length());
            }

            // ── Build separator ───────────────────────────────────────────
            StringBuilder sb = new StringBuilder("+");
            for (int w : colW) sb.append("-".repeat(w + 2)).append("+");
            sb.append("-".repeat(matchColW + 2)).append("+");
            String sep = sb.toString();

            // ── Print table ───────────────────────────────────────────────
            System.out.println("\n" + sep);
            System.out.print("|");
            for (int d = 0; d < numCols; d++) System.out.printf(" %-" + colW[d] + "s |", COL_LABELS[d]);
            System.out.printf(" %-" + matchColW + "s |%n", "Matched In");
            System.out.println(sep);

            for (int r = 0; r < displayRows.length; r++) {
                System.out.print("|");
                for (int d = 0; d < numCols; d++) System.out.printf(" %-" + colW[d] + "s |", displayRows[r][d]);
                System.out.printf(" %-" + matchColW + "s |%n", matchInfo.get(r));
            }

            System.out.println(sep);
            System.out.println("Total matching rows: " + matchedRows.size());

        } catch (FileNotFoundException e) { System.out.println("Error: File not found -> " + filePath);
        } catch (IOException e)           { System.out.println("Error reading file: " + e.getMessage());
        } finally                         { scanner.close(); }
    }
}
