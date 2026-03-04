/**
 * PROGRAMMING 2 – MACHINE PROBLEM
 * University of Perpetual Help System DALTA – Molino Campus
 * BS Information Technology - Game Development
 * Dataset: https://www.kaggle.com/datasets/asaniczka/video-game-sales-2024
 *
 * Run: node videoGameAnalytics.js
 */

'use strict';

const fs       = require('fs');
const readline = require('readline');
const path     = require('path');

// ─────────────────────────────────────────────────────────────
// Module: CSV Parser
// ─────────────────────────────────────────────────────────────

/**
 * Splits a CSV line respecting double-quoted fields.
 * @param {string} line
 * @returns {string[]}
 */
function splitCsvLine(line) {
    const result = [];
    let current  = '';
    let inQuotes = false;
    for (const ch of line) {
        if (ch === '"')           { inQuotes = !inQuotes; }
        else if (ch === ',' && !inQuotes) { result.push(current); current = ''; }
        else                      { current += ch; }
    }
    result.push(current);
    return result;
}

/**
 * Parses the entire CSV file content into an array of record objects.
 * Expected columns (0-indexed):
 *   0:img  1:title  2:console  3:genre  4:publisher  5:developer
 *   6:critic_score  7:total_sales  8:na_sales  9:jp_sales
 *   10:pal_sales  11:other_sales  12:release_date  13:last_update
 *
 * @param {string} content Raw file content
 * @returns {Object[]} Array of DataRecord objects
 */
function parseDataset(content) {
    const lines   = content.split(/\r?\n/);
    const records = [];

    for (let i = 1; i < lines.length; i++) {   // skip header row
        const line = lines[i].trim();
        if (!line) continue;

        try {
            const parts = splitCsvLine(line);
            if (parts.length < 13) continue;

            const totalSales = parseFloat(parts[7])  || 0;
            const naSales    = parseFloat(parts[8])  || 0;
            const jpSales    = parseFloat(parts[9])  || 0;
            const palSales   = parseFloat(parts[10]) || 0;

            records.push({
                title      : parts[1].trim(),
                console    : parts[2].trim(),
                genre      : parts[3].trim(),
                publisher  : parts[4].trim(),
                developer  : parts[5].trim(),
                totalSales,
                naSales,
                jpSales,
                palSales,
                releaseDate: parts[12].trim(),
            });
        } catch (_err) {
            // Skip malformed lines silently
        }
    }
    return records;
}

// ─────────────────────────────────────────────────────────────
// Module: Analytics
// ─────────────────────────────────────────────────────────────

/**
 * Aggregates total sales by a given key field.
 * @param {Object[]} records
 * @param {string}   field  - key on each record
 * @returns {Map<string, number>}
 */
function sumByField(records, field) {
    const map = new Map();
    for (const r of records) {
        const key = r[field];
        if (!key) continue;
        map.set(key, (map.get(key) || 0) + r.totalSales);
    }
    return map;
}

/**
 * Returns the top-N entries from a Map, sorted descending by value.
 * @param {Map<string, number>} map
 * @param {number} n
 * @returns {Array<[string, number]>}
 */
function topN(map, n) {
    return [...map.entries()]
        .sort((a, b) => b[1] - a[1])
        .slice(0, n);
}

/**
 * Runs all analytics on the dataset.
 * @param {Object[]} records
 * @returns {Object} analytics result bundle
 */
function runAnalytics(records) {
    const totalSales    = records.reduce((s, r) => s + r.totalSales, 0);
    const topGenres     = topN(sumByField(records, 'genre'),     5);
    const topConsoles   = topN(sumByField(records, 'console'),   5);
    const topPublishers = topN(sumByField(records, 'publisher'), 5);
    const top5Games     = [...records]
        .sort((a, b) => b.totalSales - a.totalSales)
        .slice(0, 5);

    return { totalSales, topGenres, topConsoles, topPublishers, top5Games };
}

// ─────────────────────────────────────────────────────────────
// Module: Display
// ─────────────────────────────────────────────────────────────

/**
 * Prints the analytics to stdout in a formatted layout.
 */
function displayReport(records, analytics) {
    const { totalSales, topGenres, topConsoles, topPublishers, top5Games } = analytics;
    const div = '='.repeat(60);

    console.log('\n' + div);
    console.log('       VIDEO GAME SALES ANALYTICS REPORT');
    console.log(div);
    console.log(`  Total Records Loaded : ${records.length.toLocaleString()}`);
    console.log(`  Global Total Sales   : ${totalSales.toFixed(2)} million units`);
    console.log(div);

    console.log('\n  TOP 5 GENRES BY SALES (million units)');
    console.log('  ' + '-'.repeat(40));
    topGenres.forEach(([genre, sales], i) =>
        console.log(`  ${i + 1}. ${genre.padEnd(22)} ${sales.toFixed(2).padStart(8)} M`)
    );

    console.log('\n  TOP 5 CONSOLES BY SALES (million units)');
    console.log('  ' + '-'.repeat(40));
    topConsoles.forEach(([console_, sales], i) =>
        console.log(`  ${i + 1}. ${console_.padEnd(22)} ${sales.toFixed(2).padStart(8)} M`)
    );

    console.log('\n  TOP 5 PUBLISHERS BY SALES (million units)');
    console.log('  ' + '-'.repeat(40));
    topPublishers.forEach(([pub, sales], i) =>
        console.log(`  ${i + 1}. ${pub.padEnd(26)} ${sales.toFixed(2).padStart(8)} M`)
    );

    console.log('\n  TOP 5 BEST-SELLING GAMES');
    console.log('  ' + '-'.repeat(56));
    console.log(`  ${'#'.padEnd(4)}${'Title'.padEnd(36)}${'Console'.padEnd(9)}Sales M`);
    top5Games.forEach((r, i) => {
        const title = r.title.length > 34 ? r.title.slice(0, 33) + '…' : r.title;
        console.log(`  ${String(i + 1).padEnd(4)}${title.padEnd(36)}${r.console.padEnd(9)}${r.totalSales.toFixed(2).padStart(7)}`);
    });

    console.log(div);
}

// ─────────────────────────────────────────────────────────────
// Module: CSV Export
// ─────────────────────────────────────────────────────────────

/**
 * Writes the summary report to summary_report.csv using fs.writeFile().
 * @param {Object[]} records
 * @param {Object}   analytics
 */
function exportSummaryReport(records, analytics) {
    const { totalSales, topGenres, topConsoles, topPublishers, top5Games } = analytics;
    const outputPath = 'summary_report.csv';
    const lines      = [];

    // Section 1 – Overview
    lines.push('SECTION,METRIC,VALUE');
    lines.push(`Overview,Total Records,${records.length}`);
    lines.push(`Overview,Global Total Sales (M),${totalSales.toFixed(2)}`);
    lines.push('');

    // Section 2 – Top Genres
    lines.push('SECTION,RANK,GENRE,TOTAL_SALES_M');
    topGenres.forEach(([genre, sales], i) =>
        lines.push(`Top Genres,${i + 1},${genre},${sales.toFixed(2)}`)
    );
    lines.push('');

    // Section 3 – Top Consoles
    lines.push('SECTION,RANK,CONSOLE,TOTAL_SALES_M');
    topConsoles.forEach(([console_, sales], i) =>
        lines.push(`Top Consoles,${i + 1},${console_},${sales.toFixed(2)}`)
    );
    lines.push('');

    // Section 4 – Top Publishers
    lines.push('SECTION,RANK,PUBLISHER,TOTAL_SALES_M');
    topPublishers.forEach(([pub, sales], i) =>
        lines.push(`Top Publishers,${i + 1},"${pub}",${sales.toFixed(2)}`)
    );
    lines.push('');

    // Section 5 – Top 5 Games
    lines.push('SECTION,RANK,TITLE,CONSOLE,GENRE,PUBLISHER,TOTAL_SALES_M,RELEASE_DATE');
    top5Games.forEach((r, i) =>
        lines.push(`Top Games,${i + 1},"${r.title}",${r.console},${r.genre},"${r.publisher}",${r.totalSales.toFixed(2)},${r.releaseDate}`)
    );

    const content = lines.join('\n');

    fs.writeFile(outputPath, content, 'utf8', (err) => {
        if (err) {
            console.error('Failed to write summary_report.csv:', err.message);
        } else {
            console.log(`\n  Summary report exported → ${outputPath}`);
        }
    });
}

// ─────────────────────────────────────────────────────────────
// Module: File-Path Input Loop
// ─────────────────────────────────────────────────────────────

const rl = readline.createInterface({ input: process.stdin, output: process.stdout });

/**
 * Recursively asks the user for a valid CSV file path,
 * then kicks off the full analysis pipeline.
 */
function askFilePath() {
    rl.question('Enter dataset file path: ', (inputPath) => {
        const trimmed = inputPath.trim();

        if (!fs.existsSync(trimmed)) {
            console.log('Invalid file path. Please try again.');
            return askFilePath();
        }

        const stat = fs.statSync(trimmed);
        if (!stat.isFile()) {
            console.log('Path is not a file. Please try again.');
            return askFilePath();
        }

        const ext = path.extname(trimmed).toLowerCase();
        if (ext !== '.csv') {
            console.log('File does not appear to be a CSV. Please try again.');
            return askFilePath();
        }

        console.log('File found. Processing...\n');
        rl.close();

        // ── Load ──────────────────────────────────────────────
        try {
            const content = fs.readFileSync(trimmed, 'utf8');
            const records = parseDataset(content);
            console.log(`Loaded ${records.length.toLocaleString()} records.\n`);

            // ── Analyse ───────────────────────────────────────
            const analytics = runAnalytics(records);

            // ── Display ───────────────────────────────────────
            displayReport(records, analytics);

            // ── Export ────────────────────────────────────────
            exportSummaryReport(records, analytics);

        } catch (err) {
            console.error('Error processing file:', err.message);
        }
    });
}

// ── Entry Point ───────────────────────────────────────────────
askFilePath();
