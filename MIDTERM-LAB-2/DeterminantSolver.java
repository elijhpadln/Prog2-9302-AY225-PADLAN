/**
 * =====================================================
 * Student Name    : Padlan, Elijah Lean Eliska P.
 * Course          : Math 101 - Linear Algebra
 * Assignment      : Programming Assignment 1 - 3x3 Matrix Determinant Solver
 * School          : University of Perpetual Help System DALTA, Molino Campus
 * Date            : April 8, 2026
 * GitHub Repo     : https://github.com/[your-username]/uphsd-cs-padlan-elijahlean
 *
 * Description:
 *   This program computes the determinant of a hardcoded 3x3 matrix assigned
 *   to Elijah Lean Eliska P. Padlan for Math 101. The solution is computed
 *   using cofactor expansion along the first row. Each intermediate step
 *   (2x2 minor, cofactor term, running sum) is printed to the console in a
 *   readable format.
 * =====================================================
 */
public class DeterminantSolver {

    // ── SECTION 1: Matrix Declaration ───────────────────────────────────
    // This is the 3x3 matrix assigned to student #28, Padlan, Elijah Lean Eliska P.
    // Values are hardcoded as a 2D integer array in row-major order.
    static int[][] matrix = {
        { 4, 5, 3 },   // Row 1 of assigned matrix
        { 2, 6, 1 },   // Row 2 of assigned matrix
        { 5, 3, 4 }    // Row 3 of assigned matrix
    };

    // ── SECTION 2: 2x2 Determinant Helper ───────────────────────────────
    // Computes the determinant of a 2x2 matrix from its four corner values.
    // The formula is (top-left x bottom-right) minus (top-right x bottom-left).
    static int computeMinor(int a, int b, int c, int d) {
        return (a * d) - (b * c);
    }

    // ── SECTION 3: Matrix Printer ────────────────────────────────────────
    // Displays the 3x3 matrix using Unicode box-drawing characters so it
    // matches the expected output format from the assignment sheet.
    static void printMatrix(int[][] m) {
        System.out.println("\u250c               \u2510");
        for (int[] row : m) {
            System.out.printf("\u2502  %2d  %2d  %2d  \u2502%n", row[0], row[1], row[2]);
        }
        System.out.println("\u2514               \u2518");
    }

    // ── SECTION 4: Step-by-Step Determinant Solver ──────────────────────
    // Walks through the full cofactor expansion along the first row, printing
    // every intermediate result so the solution process is completely transparent.
    static void solveDeterminant(int[][] m) {

        System.out.println("=".repeat(52));
        System.out.println("  3x3 MATRIX DETERMINANT SOLVER");
        System.out.println("  Student: Padlan, Elijah Lean Eliska P.");
        System.out.println("  Assigned Matrix:");
        System.out.println("=".repeat(52));
        printMatrix(m);
        System.out.println("=".repeat(52));
        System.out.println();
        System.out.println("Expanding along Row 1 (cofactor expansion):");
        System.out.println();

        // ── Step 1: Compute minor M11 ──
        // Cross out row 0 and column 0; remaining 2x2 block:
        // elements at positions [1][1], [1][2], [2][1], [2][2].
        int minor11 = computeMinor(m[1][1], m[1][2], m[2][1], m[2][2]);
        System.out.printf("  Step 1 - Minor M11: det([%d,%d],[%d,%d]) = (%dx%d)-(%dx%d) = %d%n",
            m[1][1], m[1][2], m[2][1], m[2][2],
            m[1][1], m[2][2], m[1][2], m[2][1], minor11);

        // ── Step 2: Compute minor M12 ──
        // Cross out row 0 and column 1; remaining block:
        // [1][0], [1][2], [2][0], [2][2].
        int minor12 = computeMinor(m[1][0], m[1][2], m[2][0], m[2][2]);
        System.out.printf("  Step 2 - Minor M12: det([%d,%d],[%d,%d]) = (%dx%d)-(%dx%d) = %d%n",
            m[1][0], m[1][2], m[2][0], m[2][2],
            m[1][0], m[2][2], m[1][2], m[2][0], minor12);

        // ── Step 3: Compute minor M13 ──
        // Cross out row 0 and column 2; remaining block:
        // [1][0], [1][1], [2][0], [2][1].
        int minor13 = computeMinor(m[1][0], m[1][1], m[2][0], m[2][1]);
        System.out.printf("  Step 3 - Minor M13: det([%d,%d],[%d,%d]) = (%dx%d)-(%dx%d) = %d%n",
            m[1][0], m[1][1], m[2][0], m[2][1],
            m[1][0], m[2][1], m[1][1], m[2][0], minor13);

        // ── Cofactor Terms ──
        // Apply the checkerboard sign pattern: + for C11, - for C12, + for C13.
        int c11 =  m[0][0] * minor11;
        int c12 = -m[0][1] * minor12;
        int c13 =  m[0][2] * minor13;

        System.out.println();
        System.out.printf("  Cofactor C11 = (+1) x %d x %d = %d%n", m[0][0], minor11, c11);
        System.out.printf("  Cofactor C12 = (-1) x %d x %d = %d%n", m[0][1], minor12, c12);
        System.out.printf("  Cofactor C13 = (+1) x %d x %d = %d%n", m[0][2], minor13, c13);

        // ── Final Determinant ──
        // Sum all three cofactor terms to get the final determinant.
        int det = c11 + c12 + c13;
        System.out.printf("%n  det(M) = %d + (%d) + %d%n", c11, c12, c13);
        System.out.println("=".repeat(52));
        System.out.printf("  [RESULT]  DETERMINANT = %d%n", det);

        // ── Singular Matrix Check ──
        // A zero determinant means the matrix has no inverse.
        if (det == 0) {
            System.out.println("  [!] The matrix is SINGULAR -- it has no inverse.");
        }
        System.out.println("=".repeat(52));
    }

    // ── SECTION 5: Entry Point ───────────────────────────────────────────
    // Program starts here; hands off to the solver with the assigned matrix.
    public static void main(String[] args) {
        solveDeterminant(matrix);
    }

}
