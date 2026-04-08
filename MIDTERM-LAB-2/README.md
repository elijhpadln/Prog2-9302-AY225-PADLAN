# Programming Assignment 1 — 3x3 Matrix Determinant Solver

## Student Information
- **Name:** Padlan, Elijah Lean Eliska P.
- **Section:** BSIT-GD
- **Course:** Math 101 — Linear Algebra, UPHSD Molino Campus
- **Assignment:** Programming Assignment 1 — 3x3 Matrix Determinant Solver

---

## Assigned Matrix

```
| 4  5  3 |
| 2  6  1 |
| 5  3  4 |
```

---

## How to Run

### Java
**1. Compile the program:**
```
javac DeterminantSolver.java
```
**2. Run the program:**
```
java DeterminantSolver
```

### JavaScript (Node.js)
**1. Make sure Node.js is installed:**
```
node -v
```
If no version appears, download and install Node.js from https://nodejs.org

**2. Run the program:**
```
node determinant_solver.js
```

---

## Sample Output

```
====================================================
  3x3 MATRIX DETERMINANT SOLVER
  Student: Padlan, Elijah Lean Eliska P.
  Assigned Matrix:
====================================================
+------------------+
|   4    5    3    |
|   2    6    1    |
|   5    3    4    |
+------------------+
====================================================

Expanding along Row 1 (cofactor expansion):

  Step 1 - Minor M11: det([6,1],[3,4]) = (6x4) - (1x3) = 21
  Step 2 - Minor M12: det([2,1],[5,4]) = (2x4) - (1x5) = 3
  Step 3 - Minor M13: det([2,6],[5,3]) = (2x3) - (6x5) = -24

  Cofactor C11 = (+1) x 4 x 21 = 84
  Cofactor C12 = (-1) x 5 x 3 = -15
  Cofactor C13 = (+1) x 3 x -24 = -72

  det(M) = 84 + (-15) + -72
====================================================
  [RESULT]  DETERMINANT = -3
====================================================
```

---

## Final Determinant Value

**det(M) = -3**

The matrix is **not singular** — it has an inverse.

---

## File Structure

```
uphsd-cs-padlan-elijahlean/
|-- linear-algebra/
|   |-- assignment-01/
|   |   |-- DeterminantSolver.java
|   |   |-- determinant_solver.js
|   |   |-- README.md
|-- README.md
```
