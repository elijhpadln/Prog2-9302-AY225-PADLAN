/**
 * =====================================================
 * Student Name    : Padlan, Elijah Lean Eliska P.
 * Course          : Math 101 - Linear Algebra
 * Assignment      : Programming Assignment 1 - 3x3 Matrix Determinant Solver
 * School          : University of Perpetual Help System DALTA, Molino Campus
 * Date            : April 8, 2026
 * GitHub Repo     : https://github.com/[your-username]/uphsd-cs-padlan-elijahlean
 * Runtime         : Node.js (run with: node determinant_solver.js)
 *
 * Description:
 *   JavaScript equivalent of DeterminantSolver.java. This script computes
 *   the determinant of the assigned 3x3 matrix for student #28 using cofactor
 *   expansion along the first row. All intermediate steps are logged to the
 *   console using console.log() for complete transparency of the solution process.
 * =====================================================
 */

// ── SECTION 1: Matrix Declaration ───────────────────────────────────
// The 3x3 matrix assigned to Padlan, Elijah Lean Eliska P. (student #28).
// Declared as a 2D JavaScript array -- outer array holds rows, each inner
// array holds the three column values for that row.
const matrix = [
    [4, 5, 3],   // Row 1
    [2, 6, 1],   // Row 2
    [5, 3, 4]    // Row 3
];

// ── SECTION 2: Matrix Printer ────────────────────────────────────────
// Prints the 3x3 matrix using Unicode box-drawing characters so it matches
// the expected output format shown in the assignment sheet.
function printMatrix(m) {
    console.log('\u250c               \u2510');
    m.forEach(row => {
        const fmt = row.map(v => v.toString().padStart(3)).join('  ');
        console.log(`\u2502 ${fmt}  \u2502`);
    });
    console.log('\u2514               \u2518');
}

// ── SECTION 3: 2x2 Determinant Helper ───────────────────────────────
// Computes the determinant of a 2x2 sub-matrix from its four scalar inputs.
// Called once for each of the three minors during cofactor expansion.
function computeMinor(a, b, c, d) {
    // 2x2 determinant: multiply the main diagonal and subtract the anti-diagonal
    return (a * d) - (b * c);
}

// ── SECTION 4: Step-by-Step Determinant Solver ──────────────────────
// Core function that performs the full cofactor expansion along row 1.
// Prints the matrix, each minor calculation, each signed cofactor term,
// the final sum, and a singular-matrix warning if the determinant is zero.
function solveDeterminant(m) {
    const line = '='.repeat(52);

    console.log(line);
    console.log('  3x3 MATRIX DETERMINANT SOLVER');
    console.log('  Student: Padlan, Elijah Lean Eliska P.');
    console.log('  Assigned Matrix:');
    console.log(line);
    printMatrix(m);
    console.log(line);
    console.log();
    console.log('Expanding along Row 1 (cofactor expansion):');
    console.log();

    // ── Step 1: Minor M11 ──
    // Remove row 0 and column 0; remaining block: [1][1],[1][2],[2][1],[2][2].
    const minor11 = computeMinor(m[1][1], m[1][2], m[2][1], m[2][2]);
    console.log(
        `  Step 1 - Minor M11: det([${m[1][1]},${m[1][2]}],[${m[2][1]},${m[2][2]}])` +
        ` = (${m[1][1]}x${m[2][2]}) - (${m[1][2]}x${m[2][1]}) = ${minor11}`
    );

    // ── Step 2: Minor M12 ──
    // Remove row 0 and column 1; remaining block: [1][0],[1][2],[2][0],[2][2].
    const minor12 = computeMinor(m[1][0], m[1][2], m[2][0], m[2][2]);
    console.log(
        `  Step 2 - Minor M12: det([${m[1][0]},${m[1][2]}],[${m[2][0]},${m[2][2]}])` +
        ` = (${m[1][0]}x${m[2][2]}) - (${m[1][2]}x${m[2][0]}) = ${minor12}`
    );

    // ── Step 3: Minor M13 ──
    // Remove row 0 and column 2; remaining block: [1][0],[1][1],[2][0],[2][1].
    const minor13 = computeMinor(m[1][0], m[1][1], m[2][0], m[2][1]);
    console.log(
        `  Step 3 - Minor M13: det([${m[1][0]},${m[1][1]}],[${m[2][0]},${m[2][1]}])` +
        ` = (${m[1][0]}x${m[2][1]}) - (${m[1][1]}x${m[2][0]}) = ${minor13}`
    );

    // ── Cofactor Terms ──
    // C11 is positive, C12 is negative, C13 is positive (alternating sign rule).
    const c11 =  m[0][0] * minor11;
    const c12 = -m[0][1] * minor12;
    const c13 =  m[0][2] * minor13;

    console.log();
    console.log(`  Cofactor C11 = (+1) x ${m[0][0]} x ${minor11} = ${c11}`);
    console.log(`  Cofactor C12 = (-1) x ${m[0][1]} x ${minor12} = ${c12}`);
    console.log(`  Cofactor C13 = (+1) x ${m[0][2]} x ${minor13} = ${c13}`);

    // ── Final Determinant ──
    // Sum the three cofactor terms to get the determinant of the 3x3 matrix.
    const det = c11 + c12 + c13;
    console.log();
    console.log(`  det(M) = ${c11} + (${c12}) + ${c13}`);
    console.log(line);
    console.log(`  [RESULT]  DETERMINANT = ${det}`);

    // ── Singular Matrix Check ──
    // If the determinant equals zero, the matrix cannot be inverted.
    if (det === 0) {
        console.log('  [!] The matrix is SINGULAR -- it has no inverse.');
    }
    console.log(line);
}

// ── SECTION 5: Program Entry Point ──────────────────────────────────
// Kick off the solver with the student's assigned matrix declared above.
solveDeterminant(matrix);
