/**
 * MP03 - Search for a Keyword in the Dataset
 * Dataset: Pearson VUE Exam Results - University of Perpetual Help System Molino
 * Columns: Candidate, Student/Faculty/NTE, Exam, Language, Exam Date, Score, Result, Time Used
 *
 * Program Logic:
 * Prompts for a CSV path and search keyword. Skips the Pearson VUE preamble,
 * then performs a case-insensitive search across every meaningful field of every
 * data row. Matching rows are displayed in a formatted table with a "Matched In"
 * column. Long values are truncated to keep the table aligned on a standard terminal.
 */

const fs       = require('fs');
const readline = require('readline');

// ── Column display configuration ─────────────────────────────────────────────
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
const MATCH_COL_MAXW = 25; // Max width for the "Matched In" column

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
 * truncate - Clips a string to maxLen, appending … if needed.
 */
function truncate(str, maxLen) {
    const s = String(str);
    return s.length > maxLen ? s.slice(0, maxLen - 1) + '\u2026' : s;
}

function padRight(str, len) { return String(str).padEnd(len, ' '); }

function searchKeyword(filePath, keyword) {
    if (!keyword) { console.error('Error: Keyword cannot be empty.'); process.exit(1); }
    if (!fs.existsSync(filePath)) { console.error(`Error: File not found -> "${filePath}"`); process.exit(1); }

    let raw;
    try { raw = fs.readFileSync(filePath, 'utf8'); }
    catch (err) { console.error(`Error reading file: ${err.message}`); process.exit(1); }

    const lines    = raw.replace(/\uFEFF/g, '').replace(/\r/g, '').split('\n');
    const kwLower  = keyword.toLowerCase();

    let header    = null;
    let colIdxMap = {};   // display key -> CSV column index
    let usedCols  = [];   // all CSV column indices with non-empty header labels

    const allRows     = [];
    const matchedRows = [];
    const matchInfo   = [];

    for (const line of lines) {
        if (line.trim() === '') continue;
        const fields = parseCSVLine(line);

        if (header === null) {
            if (fields[0].toLowerCase() === 'candidate') {
                header = fields;
                // Map display columns to CSV indices
                COL_CONFIG.forEach(col => {
                    const idx = header.findIndex(h => h.trim().toLowerCase() === col.key.toLowerCase());
                    colIdxMap[col.key] = idx;
                });
                // All non-empty header columns (for search across ALL fields)
                usedCols = header.map((h, i) => ({ h, i }))
                                 .filter(({ h }) => h.trim() !== '')
                                 .map(({ i }) => i);
            }
            continue;
        }

        if (fields[0].trim() === '') continue;
        allRows.push(fields);

        // Search every non-empty column for the keyword
        const hits = [];
        usedCols.forEach(colIdx => {
            const val = fields[colIdx] !== undefined ? fields[colIdx] : '';
            if (val.toLowerCase().includes(kwLower)) hits.push(header[colIdx]);
        });

        if (hits.length > 0) {
            matchedRows.push(fields);
            matchInfo.push(truncate(hits.join(', '), MATCH_COL_MAXW));
        }
    }

    if (!header) { console.error('Error: Header row not found.'); process.exit(1); }

    // ── Summary ──────────────────────────────────────────────────────────
    console.log('\n============================================================');
    console.log('     MP03 - Keyword Search Results');
    console.log('     Pearson VUE Exam Results');
    console.log('============================================================');
    console.log(`File     : ${filePath}`);
    console.log(`Keyword  : "${keyword}"`);
    console.log(`Searched : ${allRows.length} records`);
    console.log(`Matches  : ${matchedRows.length} records`);

    if (matchedRows.length === 0) {
        console.log(`\nNo records matched the keyword "${keyword}".`); return;
    }

    // ── Build display rows with truncation ───────────────────────────────
    const colW = COL_CONFIG.map(col => col.key.length);
    let matchColW = 'Matched In'.length;

    const displayRows = matchedRows.map((row, r) => {
        const cells = COL_CONFIG.map((col, i) => {
            const idx = colIdxMap[col.key];
            const raw = (idx >= 0 && idx < row.length) ? row[idx] : '';
            const val = truncate(raw, col.maxW);
            colW[i] = Math.max(colW[i], val.length);
            return val;
        });
        matchColW = Math.max(matchColW, matchInfo[r].length);
        return cells;
    });

    // ── Build separator ──────────────────────────────────────────────────
    const separator = '+' + colW.map(w => '-'.repeat(w + 2)).join('+') + '+' + '-'.repeat(matchColW + 2) + '+';

    // ── Print table ──────────────────────────────────────────────────────
    console.log('\n' + separator);
    console.log('| ' + COL_CONFIG.map((col, i) => padRight(col.key, colW[i])).join(' | ')
        + ' | ' + padRight('Matched In', matchColW) + ' |');
    console.log(separator);

    displayRows.forEach((row, r) => {
        console.log('| ' + row.map((v, i) => padRight(v, colW[i])).join(' | ')
            + ' | ' + padRight(matchInfo[r], matchColW) + ' |');
    });

    console.log(separator);
    console.log(`Total matching rows: ${matchedRows.length}`);
}

// ─── Entry Point ──────────────────────────────────────────────────────────────
const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
console.log('============================================================');
console.log('     MP03 - Search for a Keyword');
console.log('     Pearson VUE Exam Results');
console.log('============================================================');
rl.question('Enter the CSV dataset file path: ', path => {
    rl.question('Enter keyword to search: ', kw => {
        rl.close();
        searchKeyword(path.trim(), kw.trim());
    });
});
