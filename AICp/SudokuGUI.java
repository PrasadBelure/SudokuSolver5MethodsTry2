
// SudokuGUI.java
package AICp;

import javax.swing.*;
import java.awt.*;

public class SudokuGUI extends JFrame {
    private final SudokuLogicBackTracking solver;
    private final JTextField[][] cells;
    private final int SIZE;
    private final int SUBGRID;
    private int[][] board;

    public SudokuGUI() {
        solver = new SudokuLogicBackTracking();
        SIZE = solver.getSize();
        SUBGRID = solver.getSubgrid();
        cells = new JTextField[SIZE][SIZE];
        board = new int[SIZE][SIZE];

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
                
                if ((i / SUBGRID + j / SUBGRID) % 2 == 0) {
                    cells[i][j].setBackground(new Color(173, 216, 230));
                } else {
                    cells[i][j].setBackground(Color.WHITE);
                }
                
                gridPanel.add(cells[i][j]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
    }

    private void addButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.LIGHT_GRAY);

        addButton(buttonPanel, "Generate", e -> generatePuzzle());
        addButton(buttonPanel, "Solve", e -> solveSudoku());
        addButton(buttonPanel, "Validate", e -> validateSolution());
        addButton(buttonPanel, "Hint", e -> provideHint());

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addButton(JPanel panel, String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 50));
        button.addActionListener(listener);
        panel.add(button);
    }

    private void solveSudoku() {
        String[] options = {"Backtracking", "Constraint Programming","MRV","Simulated Anneling","Genetic Algo"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Select Solving Method",
            "Choose Algorithm",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        readBoard();
        boolean solved = false;

        if (choice == 0) {
            // Backtracking method
            solved = solver.solve(board);
        } else if (choice == 1) {
            // Constraint Programming method
            solved = SudokuSolverCP.solveSudokuConstraint(board);
        } else if(choice==2){
            //Minimum Remaining Huristic 
            solved = SudokuSolverMRV.solve(board);
        } else if(choice == 3){
            solved = SimulatedAnnealingSudoku.solve(board);
        } else if(choice==4){
            solved = SudokuGA.solve(board);
        }

        if (solved) {
            updateBoard();
            JOptionPane.showMessageDialog(this, "Solved!");
        } else {
            JOptionPane.showMessageDialog(this, "No solution exists.");
        }
    }

    private void generatePuzzle() {
        board = solver.generatePuzzle(getDifficulty());
        updateBoard();
    }

    private int getDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        return JOptionPane.showOptionDialog(
                this, "Select Difficulty Level", "Generate Puzzle",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]
        );
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
                cells[i][j].setBackground((i / SUBGRID + j / SUBGRID) % 2 == 0 ? 
                    new Color(173, 216, 230) : Color.WHITE);
            }
        }
    }

    private void validateSolution() {
        boolean valid = true;
        readBoard();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != 0 && !solver.isValid(board, i, j, board[i][j])) {
                    cells[i][j].setBackground(Color.RED);
                    valid = false;
                } else {
                    cells[i][j].setBackground((i / SUBGRID + j / SUBGRID) % 2 == 0 ? 
                        new Color(173, 216, 230) : Color.WHITE);
                }
            }
        }

        JOptionPane.showMessageDialog(this, 
            valid ? "Valid Solution!" : "Invalid entries highlighted in red.");
    }

    private void provideHint() {
        readBoard();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= SIZE; num++) {
                        if (solver.isValid(board, row, col, num)) {
                            board[row][col] = num;
                            if (solver.solve(board)) {
                                cells[row][col].setText(String.valueOf(num));
                                cells[row][col].setBackground(Color.YELLOW);
                                return;
                            }
                            board[row][col] = 0;
                        }
                    }
                }
            }
        }
        JOptionPane.showMessageDialog(this, "No hints available.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SudokuGUI::new);
    }
}
