import java.io.*;
import java.util.*;
class DataRecord {
    private final String title;
    private final String console;
    private final String genre;
    private final String publisher;
    private final double totalSales;
    private final double naSales;
    private final double jpSales;
    private final double palSales;
    private final String releaseDate;

    public DataRecord(String title, String console, String genre,
                      String publisher, double totalSales,
                      double naSales, double jpSales, double palSales,
                      String releaseDate) {
        this.title       = title;
        this.console     = console;
        this.genre       = genre;
        this.publisher   = publisher;
        this.totalSales  = totalSales;
        this.naSales     = naSales;
        this.jpSales     = jpSales;
        this.palSales    = palSales;
        this.releaseDate = releaseDate;
    }

    public String getTitle()       { return title; }
    public String getConsole()     { return console; }
    public String getGenre()       { return genre; }
    public String getPublisher()   { return publisher; }
    public double getTotalSales()  { return totalSales; }
    public double getNaSales()     { return naSales; }
    public double getJpSales()     { return jpSales; }
    public double getPalSales()    { return palSales; }
    public String getReleaseDate() { return releaseDate; }

    /** Parse a CSV line into a DataRecord (handles quoted fields). */
    public static DataRecord fromCsvLine(String line) {
        String[] parts = splitCsv(line);
        if (parts.length < 14) return null;

        // columns: img,title,console,genre,publisher,developer,
        //          critic_score,total_sales,na_sales,jp_sales,pal_sales,
        //          other_sales,release_date,last_update
        String title       = parts[1].trim();
        String console     = parts[2].trim();
        String genre       = parts[3].trim();
        String publisher   = parts[4].trim();
        double totalSales  = parseDouble(parts[7]);
        double naSales     = parseDouble(parts[8]);
        double jpSales     = parseDouble(parts[9]);
        double palSales    = parseDouble(parts[10]);
        String releaseDate = parts[12].trim();

        return new DataRecord(title, console, genre, publisher,
                              totalSales, naSales, jpSales, palSales, releaseDate);
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    /** Minimal CSV splitter that respects double-quoted fields. */
    private static String[] splitCsv(String line) {
        List<String> result = new ArrayList<String>();
        StringBuilder sb    = new StringBuilder();
        boolean inQuotes    = false;
        for (char c : line.toCharArray()) {
            if (c == '"')                   { inQuotes = !inQuotes; }
            else if (c == ',' && !inQuotes) { result.add(sb.toString()); sb.setLength(0); }
            else                            { sb.append(c); }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }
}

// ─────────────────────────────────────────────
// Main analytics class
// ─────────────────────────────────────────────
public class VideoGameAnalytics {

    // ── Helper: repeat a character n times (Java 8 compatible) ────────
    private static String repeat(char ch, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(ch);
        return sb.toString();
    }

    // ── Helper: sum all sales in a list (Java 8 compatible) ───────────
    private static double sumSales(List<DataRecord> records) {
        double total = 0;
        for (DataRecord r : records) total += r.getTotalSales();
        return total;
    }

    // ── 1. File-path prompt loop ───────────────────────────────────────
    private static File promptForFile() {
        Scanner input = new Scanner(System.in);
        File file;
        while (true) {
            System.out.print("Enter dataset file path: ");
            String path = input.nextLine().trim();
            file = new File(path);
            if (file.exists() && file.isFile()) {
                System.out.println("File found. Processing...\n");
                break;
            } else {
                System.out.println("Invalid file path. Please try again.");
            }
        }
        return file;
    }

    // ── 2. CSV loader ─────────────────────────────────────────────────
    private static List<DataRecord> loadDataset(File file) throws IOException {
        List<DataRecord> records = new ArrayList<DataRecord>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            br.readLine(); // skip header row
            String line;
            while ((line = br.readLine()) != null) {
                DataRecord rec = DataRecord.fromCsvLine(line);
                if (rec != null) records.add(rec);
            }
        } finally {
            if (br != null) br.close();
        }
        return records;
    }

    // ── 3. Analytics helpers ──────────────────────────────────────────

    /** Sums total_sales grouped by a field: "genre", "console", or "publisher". */
    private static Map<String, Double> sumByField(List<DataRecord> records, String field) {
        Map<String, Double> map = new LinkedHashMap<String, Double>();
        for (DataRecord r : records) {
            String key;
            if      (field.equals("genre"))     key = r.getGenre();
            else if (field.equals("console"))   key = r.getConsole();
            else if (field.equals("publisher")) key = r.getPublisher();
            else continue;

            if (key == null || key.trim().isEmpty()) continue;
            Double current = map.get(key);
            map.put(key, (current == null ? 0.0 : current) + r.getTotalSales());
        }
        return map;
    }

    /** Returns top-N entries from a map, sorted descending by value. */
    private static List<Map.Entry<String, Double>> topN(Map<String, Double> map, int n) {
        List<Map.Entry<String, Double>> list =
            new ArrayList<Map.Entry<String, Double>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
                return Double.compare(b.getValue(), a.getValue());
            }
        });
        return list.subList(0, Math.min(n, list.size()));
    }

    // ── 4. Console display ────────────────────────────────────────────
    private static void displayReport(List<DataRecord> records,
            List<Map.Entry<String, Double>> topGenres,
            List<Map.Entry<String, Double>> topConsoles,
            List<Map.Entry<String, Double>> topPublishers,
            List<DataRecord> top5Games) {

        String divider = repeat('=', 60);
        String dash40  = repeat('-', 40);
        String dash55  = repeat('-', 55);
        double totalSales = sumSales(records);

        System.out.println(divider);
        System.out.println("       VIDEO GAME SALES ANALYTICS REPORT");
        System.out.println(divider);
        System.out.printf("  Total Records Loaded : %,d%n", records.size());
        System.out.printf("  Global Total Sales   : %.2f million units%n", totalSales);
        System.out.println(divider);

        System.out.println("\n  TOP 5 GENRES BY SALES (million units)");
        System.out.println("  " + dash40);
        for (int i = 0; i < topGenres.size(); i++)
            System.out.printf("  %d. %-20s  %8.2f M%n",
                    i + 1, topGenres.get(i).getKey(), topGenres.get(i).getValue());

        System.out.println("\n  TOP 5 CONSOLES BY SALES (million units)");
        System.out.println("  " + dash40);
        for (int i = 0; i < topConsoles.size(); i++)
            System.out.printf("  %d. %-20s  %8.2f M%n",
                    i + 1, topConsoles.get(i).getKey(), topConsoles.get(i).getValue());

        System.out.println("\n  TOP 5 PUBLISHERS BY SALES (million units)");
        System.out.println("  " + dash40);
        for (int i = 0; i < topPublishers.size(); i++)
            System.out.printf("  %d. %-25s  %8.2f M%n",
                    i + 1, topPublishers.get(i).getKey(), topPublishers.get(i).getValue());

        System.out.println("\n  TOP 5 BEST-SELLING GAMES");
        System.out.println("  " + dash55);
        System.out.printf("  %-3s %-35s %-8s %8s%n", "#", "Title", "Console", "Sales M");
        for (int i = 0; i < top5Games.size(); i++) {
            DataRecord r = top5Games.get(i);
            System.out.printf("  %-3d %-35s %-8s %8.2f%n",
                    i + 1, truncate(r.getTitle(), 33), r.getConsole(), r.getTotalSales());
        }
        System.out.println(divider);
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "..." : s;
    }

    // ── 5. CSV export ─────────────────────────────────────────────────
    private static void exportSummaryReport(List<DataRecord> records,
            List<Map.Entry<String, Double>> topGenres,
            List<Map.Entry<String, Double>> topConsoles,
            List<Map.Entry<String, Double>> topPublishers,
            List<DataRecord> top5Games) throws IOException {

        String outputPath = "summary_report.csv";
        double totalSales = sumSales(records);
        FileWriter fw = null;

        try {
            fw = new FileWriter(outputPath);

            // Section 1: Overview
            fw.write("SECTION,METRIC,VALUE\n");
            fw.write("Overview,Total Records," + records.size() + "\n");
            fw.write(String.format("Overview,Global Total Sales (M),%.2f\n", totalSales));
            fw.write("\n");

            // Section 2: Top Genres
            fw.write("SECTION,RANK,GENRE,TOTAL_SALES_M\n");
            for (int i = 0; i < topGenres.size(); i++)
                fw.write(String.format("Top Genres,%d,%s,%.2f\n",
                        i + 1, topGenres.get(i).getKey(), topGenres.get(i).getValue()));
            fw.write("\n");

            // Section 3: Top Consoles
            fw.write("SECTION,RANK,CONSOLE,TOTAL_SALES_M\n");
            for (int i = 0; i < topConsoles.size(); i++)
                fw.write(String.format("Top Consoles,%d,%s,%.2f\n",
                        i + 1, topConsoles.get(i).getKey(), topConsoles.get(i).getValue()));
            fw.write("\n");

            // Section 4: Top Publishers
            fw.write("SECTION,RANK,PUBLISHER,TOTAL_SALES_M\n");
            for (int i = 0; i < topPublishers.size(); i++)
                fw.write(String.format("Top Publishers,%d,\"%s\",%.2f\n",
                        i + 1, topPublishers.get(i).getKey(), topPublishers.get(i).getValue()));
            fw.write("\n");

            // Section 5: Top 5 Games
            fw.write("SECTION,RANK,TITLE,CONSOLE,GENRE,PUBLISHER,TOTAL_SALES_M,RELEASE_DATE\n");
            for (int i = 0; i < top5Games.size(); i++) {
                DataRecord r = top5Games.get(i);
                fw.write(String.format("Top Games,%d,\"%s\",%s,%s,\"%s\",%.2f,%s\n",
                        i + 1, r.getTitle(), r.getConsole(), r.getGenre(),
                        r.getPublisher(), r.getTotalSales(), r.getReleaseDate()));
            }

        } finally {
            if (fw != null) fw.close();
        }

        System.out.println("\n  Summary report exported -> " + outputPath);
    }

    // ── 6. Entry point ────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            // Step 1 - ask for file path
            File file = promptForFile();

            // Step 2 - load dataset
            List<DataRecord> records = loadDataset(file);
            System.out.printf("Loaded %,d records.%n%n", records.size());

            // Step 3 - compute analytics
            Map<String, Double> genreMap     = sumByField(records, "genre");
            Map<String, Double> consoleMap   = sumByField(records, "console");
            Map<String, Double> publisherMap = sumByField(records, "publisher");

            List<Map.Entry<String, Double>> topGenres     = topN(genreMap,     5);
            List<Map.Entry<String, Double>> topConsoles   = topN(consoleMap,   5);
            List<Map.Entry<String, Double>> topPublishers = topN(publisherMap, 5);

            List<DataRecord> top5Games = new ArrayList<DataRecord>(records);
            Collections.sort(top5Games, new Comparator<DataRecord>() {
                public int compare(DataRecord a, DataRecord b) {
                    return Double.compare(b.getTotalSales(), a.getTotalSales());
                }
            });
            top5Games = top5Games.subList(0, 5);

            // Step 4 - display report
            displayReport(records, topGenres, topConsoles, topPublishers, top5Games);

            // Step 5 - export CSV
            exportSummaryReport(records, topGenres, topConsoles, topPublishers, top5Games);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}   