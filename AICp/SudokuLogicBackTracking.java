// SudokuLogic.java
package AICp;

import java.util.Random;

public class SudokuLogicBackTracking {
    private static final int SIZE = 9;
    private static final int SUBGRID = 3;

    public boolean isValid(int[][] board, int row, int col, int num) {
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

    public boolean solve(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= SIZE; num++) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num;
                            if (solve(board)) return true;
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] generatePuzzle(int difficulty) {
        int[][] fullBoard = new int[SIZE][SIZE];
        fillDiagonal(fullBoard);
        solve(fullBoard);

        int[][] puzzle = copyBoard(fullBoard);
        int removeCount = difficulty == 0 ? 36 : difficulty == 1 ? 45 : 54;
        // easy = 45 ; medium = 36 ; hard = 27 

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

    public int getSize() {
        return SIZE;
    }

    public int getSubgrid() {
        return SUBGRID;
    }
}