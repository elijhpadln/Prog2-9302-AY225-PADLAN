/**
 * MP04 - Count Valid Dataset Rows
 * Dataset: Pearson VUE Exam Results - University of Perpetual Help System Molino
 * Columns: Candidate, Student/Faculty/NTE, Exam, Language, Exam Date, Score, Result, Time Used
 *
 * Program Logic:
 * Reads the Pearson VUE CSV, skips the preamble, and validates every data row.
 * A row is valid when all meaningful columns (those with non-empty header labels
 * AND at least one non-empty value across the dataset) contain data. The summary
 * shows total rows, valid count, invalid count, and completeness %. Invalid rows
 * are listed with [EMPTY] markers; long values are truncated for clean alignment.
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

function truncate(str, maxLen) {
    const s = String(str);
    return s.length > maxLen ? s.slice(0, maxLen - 1) + '\u2026' : s;
}

function padRight(str, len) { return String(str).padEnd(len, ' '); }

/**
 * isValidRow - Returns true if none of the display columns are empty for this row.
 */
function isValidRow(row, colIdxMap) {
    return COL_CONFIG.every(col => {
        const idx = colIdxMap[col.key];
        if (idx < 0) return true; // Column not found in CSV — skip
        const val = row[idx] !== undefined ? row[idx] : '';
        return val.trim() !== '';
    });
}

function countValidRows(filePath) {
    if (!fs.existsSync(filePath)) { console.error(`Error: File not found -> "${filePath}"`); process.exit(1); }

    let raw;
    try { raw = fs.readFileSync(filePath, 'utf8'); }
    catch (err) { console.error(`Error reading file: ${err.message}`); process.exit(1); }

    const lines = raw.replace(/\uFEFF/g, '').replace(/\r/g, '').split('\n');

    let header    = null;
    let colIdxMap = {};   // display config key -> CSV column index

    const dataBuffer    = [];  // All data rows
    const invalidDetails = []; // Rows that failed validation
    const invalidNumbers = []; // 1-based row numbers

    // ── Pass 1: collect header and data ──────────────────────────────────
    for (const line of lines) {
        if (line.trim() === '') continue;
        const fields = parseCSVLine(line);
        if (header === null) {
            if (fields[0].toLowerCase() === 'candidate') {
                header = fields;
                // Map each display column to its CSV index
                COL_CONFIG.forEach(col => {
                    const idx = header.findIndex(h => h.trim().toLowerCase() === col.key.toLowerCase());
                    colIdxMap[col.key] = idx; // -1 means not found
                });
            }
            continue;
        }
        if (fields[0].trim() === '') continue;
        dataBuffer.push(fields);
    }

    if (!header) { console.error('Error: Header row not found.'); process.exit(1); }

    // ── Pass 2: validate rows ─────────────────────────────────────────────
    let validCount = 0, invalidCount = 0, rowNum = 0;
    for (const fields of dataBuffer) {
        rowNum++;
        if (isValidRow(fields, colIdxMap)) {
            validCount++;
        } else {
            invalidCount++;
            invalidDetails.push(fields);
            invalidNumbers.push(rowNum);
        }
    }

    const totalRows = dataBuffer.length;
    const pct = totalRows > 0 ? ((validCount / totalRows) * 100).toFixed(2) : '0.00';

    // ── Summary ───────────────────────────────────────────────────────────
    console.log('\n============================================================');
    console.log('     MP04 - Count Valid Dataset Rows');
    console.log('     Pearson VUE Exam Results');
    console.log('============================================================');
    console.log(`File   : ${filePath}`);
    console.log('\u2500'.repeat(50));
    console.log(`  ${'Total columns (checked):'.padEnd(32)} ${COL_CONFIG.length}`);
    console.log(`  ${'Total data rows read:'.padEnd(32)} ${totalRows}`);
    console.log(`  ${'Valid rows:'.padEnd(32)} ${validCount}`);
    console.log(`  ${'Invalid (incomplete) rows:'.padEnd(32)} ${invalidCount}`);
    console.log(`  ${'Data completeness:'.padEnd(32)} ${pct}%`);
    console.log('\u2500'.repeat(50));

    if (invalidCount === 0) {
        console.log('\nAll rows are valid. No missing fields detected.'); return;
    }

    // ── Invalid row detail table ──────────────────────────────────────────
    console.log('\nInvalid Row Details ([EMPTY] = missing field):');

    const colW    = COL_CONFIG.map(col => col.key.length);
    const rowNumW = Math.max(4, String(totalRows).length);

    const displayRows = invalidDetails.map(row =>
        COL_CONFIG.map((col, i) => {
            const idx = colIdxMap[col.key];
            const rawVal = (idx >= 0 && idx < row.length) ? row[idx] : '';
            const val = rawVal.trim() === '' ? '[EMPTY]' : truncate(rawVal, col.maxW);
            colW[i] = Math.max(colW[i], val.length);
            return val;
        })
    );

    const separator = '+-' + '-'.repeat(rowNumW) + '-+'
        + colW.map(w => '-'.repeat(w + 2)).join('+') + '+';

    console.log('\n' + separator);
    console.log('| ' + padRight('Row#', rowNumW) + ' | '
        + COL_CONFIG.map((col, i) => padRight(col.key, colW[i])).join(' | ') + ' |');
    console.log(separator);

    displayRows.forEach((row, r) => {
        console.log('| ' + padRight(invalidNumbers[r], rowNumW) + ' | '
            + row.map((v, i) => padRight(v, colW[i])).join(' | ') + ' |');
    });

    console.log(separator);
}

// ─── Entry Point ──────────────────────────────────────────────────────────────
const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
console.log('============================================================');
console.log('     MP04 - Count Valid Dataset Rows');
console.log('     Pearson VUE Exam Results');
console.log('============================================================');
rl.question('Enter the CSV dataset file path: ', answer => {
    rl.close();
    countValidRows(answer.trim());
});
