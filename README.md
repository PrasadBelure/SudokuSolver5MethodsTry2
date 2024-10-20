# SudokuSolver5MethodsTry2


## Sudoku Solver & Generator
This project is a Sudoku Solver and Generator built using Java Swing for the GUI. It supports multiple algorithms, including Backtracking, Constraint Programming, MRV (Minimum Remaining Values heuristic), Simulated Annealing, and Genetic Algorithm for solving puzzles. Users can also generate new puzzles at different difficulty levels and request hints.

## Features
- Solve Sudoku using multiple algorithms:
  - Backtracking
  - Constraint Programming
  - MRV Heuristic
  - Simulated Annealing
  - Genetic Algorithm
- Generate Sudoku puzzles with Easy, Medium, and Hard difficulty levels.
- Validate Sudoku solutions with visual feedback for invalid entries.
- Hint feature to provide suggestions for valid moves.
- User-friendly GUI built with Java Swing for easy interaction.

## Project Structure
- `SudokuGUI.java`: Implements the graphical user interface (GUI) using Java Swing.
- `SudokuLogicBackTracking.java`: Contains the backtracking algorithm for solving Sudoku.
- `SudokuSolverCP.java`: Uses constraint programming techniques.
- `SudokuSolverMRV.java`: Implements MRV heuristic for solving Sudoku.
- `SimulatedAnnealingSudoku.java`: Applies simulated annealing for optimization.
- `SudokuGA.java`: Uses a genetic algorithm to find solutions.

## Installation & Setup
1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/sudoku-solver.git
    cd sudoku-solver
    ```
2. Compile the code:
    ```bash
    javac -d bin src/AICp/*.java
    ```
3. Run the application:
    ```bash
    java -cp bin AICp.SudokuGUI
    ```

## How to Use
- **Generate Puzzle**: Click the "Generate" button and select a difficulty level.
- **Solve Puzzle**: Manually fill the board or use the "Solve" button to select an algorithm.
- **Validate Solution**: Use the "Validate" button to ensure your solution is correct.
- **Get a Hint**: If stuck, click "Hint" for a helpful suggestion.

## Requirements
- Java Development Kit (JDK) 8 or higher
- Works on Windows, macOS, and Linux

---

# Methods Used for Solving Sudoku

### 1. Backtracking Algorithm  
The backtracking algorithm is a **brute-force approach** where we attempt to place numbers in empty cells sequentially. If a conflict is found (i.e., a duplicate in the row, column, or subgrid), it "backtracks" and tries a different number. It continues this way until a valid solution is found or all possibilities are exhausted.

- **Pros:** Simple to implement and guaranteed to find a solution if one exists.
- **Cons:** Can be slow for larger or more complex puzzles due to the exhaustive search.

### 2. Constraint Programming (CP)  
Constraint programming treats the Sudoku puzzle as a **constraint satisfaction problem** (CSP). It eliminates impossible values for each cell based on rules (like no duplicates in a row/column/subgrid). The solution emerges by narrowing down options through **domain reduction**.

- **Pros:** Reduces search space significantly.
- **Cons:** Might still require backtracking in some cases, though much less frequently than the naive approach.

### 3. Minimum Remaining Values (MRV) Heuristic  
The MRV heuristic selects the cell with the **fewest possible candidates** (valid numbers) at any point during solving. This strategy ensures that the most constrained cell is solved first, minimizing guesswork and making the solution more efficient.

- **Pros:** Helps avoid dead ends by focusing on difficult cells early.
- **Cons:** Works best with puzzles that have a lot of constraints.

### 4. Simulated Annealing  
Simulated Annealing is a **probabilistic optimization technique** inspired by the annealing process in metallurgy. It starts with a random solution and tries to improve it by making small random changes. It allows for occasional "worse" moves to escape local minima, gradually reducing the frequency of such moves.

- **Pros:** Effective for finding good-enough solutions when exact solutions are hard to compute.
- **Cons:** May not always find the optimal solution; depends on configuration (temperature schedule).

### 5. Genetic Algorithm (GA)  
The genetic algorithm mimics **natural selection** by evolving a population of candidate solutions. Each candidate is evaluated based on a **fitness function** (how close it is to solving the puzzle). Over generations, the population evolves through crossover (combining two solutions) and mutation (random changes) to produce better solutions.

- **Pros:** Works well for complex puzzles where other methods struggle.
- **Cons:** Requires careful tuning of parameters (population size, mutation rate) and may take time to converge.

---

# Performance Analysis of Sudoku Solving Algorithms

In our experiments, we tested the performance of five different Sudoku solving methods across three difficulty levels: Easy, Medium, and Hard. The results are based on ten random Sudoku puzzles for each method at each difficulty level.

## Summary of Findings
- **Constraint Propagation** does not perform well for some Medium puzzles and fails entirely on Hard puzzles.
- **Simulated Annealing** takes noticeably more time for Medium puzzles.
- **Genetic Algorithm** requires excessive time for both Easy and Medium puzzles, struggling significantly on Hard.

| Method                                          | Easy (45 Filled) | Medium (36 Filled) | Hard (27 Filled) |
|-------------------------------------------------|------------------|--------------------|-------------------|
| Backtracking                                    | 10/10            | 10/10              | 10/10             |
| Constraint Propagation                          | 10/10            | 10/10              | 10/10             |
| Minimum Remaining Value + Degree Heuristic      | 10/10            | 10/10              | 10/10             |
| Simulated Annealing                             | 10/10            | 10/10              | 10/10             |
| Genetic Algorithm                               | 9/10 (More T)    | 0/10               | 0/10              |

### Conclusion
Overall, the Backtracking and Minimum Remaining Value methods performed consistently across all levels of difficulty, while Constraint Propagation, Simulated Annealing, and the Genetic Algorithm showed limitations, especially at higher difficulty levels.

This project showcases how different AI and algorithmic techniques can be applied to solve the same problem, demonstrating their strengths and limitations.
