/**
 * MP02 - Display First 10 Rows of Dataset
 * Dataset: Pearson VUE Exam Results - University of Perpetual Help System Molino
 * Columns: Candidate, Student/Faculty/NTE, Exam, Language, Exam Date, Score, Result, Time Used
 *
 * Program Logic:
 * Reads the Pearson VUE CSV export, skips the preamble metadata lines, finds
 * the true header row ("Candidate"), then reads the first 10 data rows.
 * Long field values (especially Exam names) are truncated to a max width so
 * the table renders properly in a standard terminal without line-wrapping.
 */

const fs       = require('fs');
const readline = require('readline');

// ── Column display configuration ─────────────────────────────────────────────
// maxW: maximum characters to show per column (longer values get truncated with …)
const COL_CONFIG = [
    { key: 'Candidate',              maxW: 20 },
    { key: 'Student/ Faculty/ NTE',  maxW: 10 },
    { key: 'Exam',                   maxW: 30 },
    { key: 'Language',               maxW: 8  },
    { key: 'Exam Date',              maxW: 10 },
    { key: 'Score',                  maxW: 5  },
    { key: 'Result',                 maxW: 6  },
    { key: 'Time Used',              maxW: 13 },
];

/**
 * parseCSVLine - Parses one CSV line into fields, handling quoted values.
 * @param {string} line
 * @returns {string[]}
 */
function parseCSVLine(line) {
    const fields = [];
    let current = '', insideQuote = false;
    for (const char of line) {
        if (char === '"') { insideQuote = !insideQuote; }
        else if (char === ',' && !insideQuote) { fields.push(current.trim()); current = ''; }
        else { current += char; }
    }
    fields.push(current.trim());
    return fields;
}

/**
 * truncate - Clips a string to maxLen characters, appending … if clipped.
 * @param {string} str
 * @param {number} maxLen
 * @returns {string}
 */
function truncate(str, maxLen) {
    const s = String(str);
    return s.length > maxLen ? s.slice(0, maxLen - 1) + '\u2026' : s;
}

/** padRight - Pads string to fixed width with spaces. */
function padRight(str, len) { return String(str).padEnd(len, ' '); }

/**
 * displayFirst10Rows - Main logic: reads CSV, finds header, prints first 10 rows.
 * @param {string} filePath
 */
function displayFirst10Rows(filePath) {
    if (!fs.existsSync(filePath)) {
        console.error(`Error: File not found -> "${filePath}"`); process.exit(1);
    }
    let raw;
    try { raw = fs.readFileSync(filePath, 'utf8'); }
    catch (err) { console.error(`Error reading file: ${err.message}`); process.exit(1); }

    const lines = raw.replace(/\uFEFF/g, '').replace(/\r/g, '').split('\n');

    let header   = null;   // Full header array from CSV
    let colIdxMap = {};    // Maps COL_CONFIG key -> CSV column index
    const dataRows = [];   // Up to 10 data rows

    for (const line of lines) {
        if (line.trim() === '') continue;
        const fields = parseCSVLine(line);

        // Detect header row
        if (header === null) {
            if (fields[0].toLowerCase() === 'candidate') {
                header = fields;
                // Build a map from display-name to column index (case-insensitive trim match)
                COL_CONFIG.forEach(col => {
                    const idx = header.findIndex(h => h.trim().toLowerCase() === col.key.toLowerCase());
                    colIdxMap[col.key] = idx; // -1 if not found
                });
            }
            continue; // Skip preamble
        }

        if (fields[0].trim() === '') continue; // Skip blank rows
        if (dataRows.length < 10) dataRows.push(fields);
        else break;
    }

    if (!header) {
        console.error('Error: Header row not found.'); process.exit(1);
    }
    if (dataRows.length === 0) {
        console.log('No data rows found.'); return;
    }

    // ── Build display rows (apply truncation) ────────────────────────────
    // colW[i] = actual display width for COL_CONFIG[i]
    const colW = COL_CONFIG.map(col => col.key.length); // start with header label width

    const displayRows = dataRows.map(row =>
        COL_CONFIG.map((col, i) => {
            const idx = colIdxMap[col.key];
            const raw = (idx >= 0 && idx < row.length) ? row[idx] : '';
            const val = truncate(raw, col.maxW); // Clip to max width
            colW[i] = Math.max(colW[i], val.length); // Update column width
            return val;
        })
    );

    // ── Build separator ──────────────────────────────────────────────────
    const separator = '+' + colW.map(w => '-'.repeat(w + 2)).join('+') + '+';

    // ── Print table ──────────────────────────────────────────────────────
    console.log('\n============================================================');
    console.log('     MP02 - Display First 10 Rows of Dataset');
    console.log('     Pearson VUE Exam Results');
    console.log('============================================================');
    console.log(`File  : ${filePath}`);
    console.log(`Rows  : First ${dataRows.length} data rows\n`);

    console.log(separator);
    // Header row
    console.log('| ' + COL_CONFIG.map((col, i) => padRight(col.key, colW[i])).join(' | ') + ' |');
    console.log(separator);
    // Data rows
    displayRows.forEach(row => {
        console.log('| ' + row.map((v, i) => padRight(v, colW[i])).join(' | ') + ' |');
    });
    console.log(separator);
    console.log(`Total rows displayed: ${dataRows.length}`);
}

// ─── Entry Point ──────────────────────────────────────────────────────────────
const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
console.log('============================================================');
console.log('     MP02 - Display First 10 Rows of Dataset');
console.log('     Pearson VUE Exam Results');
console.log('============================================================');
rl.question('Enter the CSV dataset file path: ', answer => {
    rl.close();
    displayFirst10Rows(answer.trim());
});
