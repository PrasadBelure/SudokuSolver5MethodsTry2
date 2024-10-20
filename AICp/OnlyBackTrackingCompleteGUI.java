package AICp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Queue;
import AICp.SudokuSolverCP;

public class OnlyBackTrackingCompleteGUI extends JFrame {
    private static final int SIZE = 9;
    private static final int SUBGRID = 3;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private int[][] board = new int[SIZE][SIZE];

    public OnlyBackTrackingCompleteGUI() {
        setTitle("Sudoku Solver & Generator");
        setSize(700, 700);
        setLayout(new BorderLayout());
        createBoard();
        addButtons();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void createBoard() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));
        
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j] = new JTextField();
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                
                // Set background color for 3x3 grids
                if ((i / SUBGRID + j / SUBGRID) % 2 == 0) {
                    cells[i][j].setBackground(new Color(173, 216, 230)); // Light blue
                } else {
                    cells[i][j].setBackground(Color.WHITE); // White
                }
                
                gridPanel.add(cells[i][j]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
    }

    private void addButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.LIGHT_GRAY); // Background color for the button panel

        JButton solveButton = new JButton("Solve");
        solveButton.setFont(new Font("Arial", Font.BOLD, 16));
        solveButton.setPreferredSize(new Dimension(150, 50));
        solveButton.addActionListener(e -> solveSudoku());

        JButton generateButton = new JButton("Generate");
        generateButton.setFont(new Font("Arial", Font.BOLD, 16));
        generateButton.setPreferredSize(new Dimension(150, 50));
        generateButton.addActionListener(e -> generatePuzzle());

        JButton validateButton = new JButton("Validate");
        validateButton.setFont(new Font("Arial", Font.BOLD, 16));
        validateButton.setPreferredSize(new Dimension(150, 50));
        validateButton.addActionListener(e -> validateSolution());

        JButton hintButton = new JButton("Hint");
        hintButton.setFont(new Font("Arial", Font.BOLD, 16));
        hintButton.setPreferredSize(new Dimension(150, 50));
        hintButton.addActionListener(e -> provideHint());

        buttonPanel.add(generateButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(validateButton);
        buttonPanel.add(hintButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void solveSudoku() {
        readBoard();
        if (backtrackingSolve(board)) {  // Use the class name
            updateBoard();
            JOptionPane.showMessageDialog(this, "Solved!");
        } else {
            JOptionPane.showMessageDialog(this, "No solution exists.");
        }
    }
    

    private void generatePuzzle() {
        board = generateValidSudoku(getDifficulty());
        updateBoard();
    }

    private int getDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
                this, "Select Difficulty Level", "Generate Puzzle",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]
        );
        return choice;
    }

    private void readBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String text = cells[i][j].getText();
                board[i][j] = text.isEmpty() ? 0 : Integer.parseInt(text);
            }
        }
    }

    private void updateBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j].setText(board[i][j] == 0 ? "" : String.valueOf(board[i][j]));
                cells[i][j].setBackground((i / SUBGRID + j / SUBGRID) % 2 == 0 ? new Color(173, 216, 230) : Color.WHITE);
            }
        }
    }

    private void validateSolution() {
        boolean valid = true;
        readBoard();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != 0 && !isValid(board, i, j, board[i][j])) {
                    cells[i][j].setBackground(Color.RED);
                    valid = false;
                } else {
                    cells[i][j].setBackground((i / SUBGRID + j / SUBGRID) % 2 == 0 ? new Color(173, 216, 230) : Color.WHITE);
                }
            }
        }

        if (valid) {
            JOptionPane.showMessageDialog(this, "Valid Solution!");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid entries highlighted in red.");
        }
    }

    private void provideHint() {
        readBoard();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) { // Find an empty cell
                    for (int num = 1; num <= SIZE; num++) { // Check numbers 1-9
                        if (isValid(board, row, col, num)) {
                            // Temporarily place the number
                            board[row][col] = num;
                            if (backtrackingSolve(board)) { // Check if it leads to a solution
                                cells[row][col].setText(String.valueOf(num));
                                cells[row][col].setBackground(Color.YELLOW);
                                return; // Return the hint immediately
                            }
                            // Remove the number and try the next
                            board[row][col] = 0;
                        }
                    }
                }
            }
        }
        JOptionPane.showMessageDialog(this, "No hints available.");
    }
    
    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num && i != col || // Check row
                board[i][col] == num && i != row || // Check column
                board[row / SUBGRID * SUBGRID + i / SUBGRID][col / SUBGRID * SUBGRID + i % SUBGRID] == num &&
                (row / SUBGRID * SUBGRID + i / SUBGRID != row ||
                        col / SUBGRID * SUBGRID + i % SUBGRID != col)) { // Check subgrid
                return false;
            }
        }
        return true;
    }

    private boolean backtrackingSolve(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= SIZE; num++) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num;
                            if (backtrackingSolve(board)) return true;
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    // Updated to include randomization and unique puzzle generation
    private int[][] generateValidSudoku(int difficulty) {
        int[][] fullBoard = new int[SIZE][SIZE];
        fillDiagonal(fullBoard); // Fill the diagonal 3x3 subgrids
        backtrackingSolve(fullBoard); // Generate a solved board

        int[][] puzzle = copyBoard(fullBoard);
        int removeCount = difficulty == 0 ? 36 : difficulty == 1 ? 45 : 54;

        Random rand = new Random();
        while (removeCount > 0) {
            int row = rand.nextInt(SIZE);
            int col = rand.nextInt(SIZE);
            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0;
                removeCount--;
            }
        }
        return puzzle;
    }

    private void fillDiagonal(int[][] board) {
        for (int i = 0; i < SIZE; i += SUBGRID) {
            fillSubGrid(board, i, i);
        }
    }

    private void fillSubGrid(int[][] board, int row, int col) {
        Random rand = new Random();
        int[] numbers = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            numbers[i] = i + 1;
        }
        shuffleArray(numbers);
        for (int r = 0; r < SUBGRID; r++) {
            for (int c = 0; c < SUBGRID; c++) {
                board[row + r][col + c] = numbers[r * SUBGRID + c];
            }
        }
    }

    private void shuffleArray(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    private int[][] copyBoard(int[][] original) {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, SIZE);
        }
        return copy;
    }



    

    public static void main(String[] args) {
        new OnlyBackTrackingCompleteGUI();
    }
}
