package AICp;

import java.util.*;

public class SudokuSolverMRV {
    private static final int SIZE = 9;
    
    /**
     * Solves the given Sudoku board in-place.
     * @param board 9x9 Sudoku board where 0 represents empty cells
     * @return true if a solution was found, false otherwise
     */
    public static boolean solve(int[][] board) {
        // First, try to fill obvious cells
        boolean progress;
        do {
            progress = false;
            // Fill single possibilities
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (board[i][j] == 0) {
                        List<Integer> possibleValues = getPossibleValues(board, i, j);
                        if (possibleValues.size() == 1) {
                            board[i][j] = possibleValues.get(0);
                            progress = true;
                        }
                    }
                }
            }
            
            // Check for hidden singles in rows, columns, and boxes
            progress |= fillHiddenSingles(board);
            
        } while (progress);

        // Then use backtracking with MRV for remaining cells
        int[] cell = findMRV(board);
        if (cell == null) {
            return isComplete(board);
        }

        int row = cell[0], col = cell[1];
        List<Integer> possibleValues = getPossibleValues(board, row, col);
        Collections.sort(possibleValues); // Try values in ascending order for consistency

        for (int value : possibleValues) {
            if (isValid(board, row, col, value)) {
                board[row][col] = value;
                if (solve(board)) {
                    return true;
                }
                board[row][col] = 0; // Backtrack
            }
        }
        return false;
    }

    private static boolean fillHiddenSingles(int[][] board) {
        boolean progress = false;
        
        // Check rows
        for (int row = 0; row < SIZE; row++) {
            progress |= findHiddenSinglesInUnit(board, row, true);
        }
        
        // Check columns
        for (int col = 0; col < SIZE; col++) {
            progress |= findHiddenSinglesInUnit(board, col, false);
        }
        
        // Check 3x3 boxes
        for (int box = 0; box < SIZE; box++) {
            progress |= findHiddenSinglesInBox(board, (box / 3) * 3, (box % 3) * 3);
        }
        
        return progress;
    }

    private static boolean findHiddenSinglesInUnit(int[][] board, int index, boolean isRow) {
        boolean progress = false;
        for (int num = 1; num <= SIZE; num++) {
            int count = 0;
            int lastPos = -1;
            
            for (int i = 0; i < SIZE; i++) {
                int value = isRow ? board[index][i] : board[i][index];
                if (value == 0 && isValid(board, isRow ? index : i, isRow ? i : index, num)) {
                    count++;
                    lastPos = i;
                }
            }
            
            if (count == 1) {
                if (isRow) {
                    board[index][lastPos] = num;
                } else {
                    board[lastPos][index] = num;
                }
                progress = true;
            }
        }
        return progress;
    }

    private static boolean findHiddenSinglesInBox(int[][] board, int startRow, int startCol) {
        boolean progress = false;
        for (int num = 1; num <= SIZE; num++) {
            int count = 0;
            int lastRow = -1;
            int lastCol = -1;
            
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[startRow + i][startCol + j] == 0 && 
                        isValid(board, startRow + i, startCol + j, num)) {
                        count++;
                        lastRow = startRow + i;
                        lastCol = startCol + j;
                    }
                }
            }
            
            if (count == 1) {
                board[lastRow][lastCol] = num;
                progress = true;
            }
        }
        return progress;
    }

    private static boolean isComplete(int[][] board) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0 || !isValid(board, i, j, board[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValid(int[][] board, int row, int col, int num) {
        // Check row
        for (int x = 0; x < SIZE; x++) {
            if (x != col && board[row][x] == num) return false;
        }

        // Check column
        for (int x = 0; x < SIZE; x++) {
            if (x != row && board[x][col] == num) return false;
        }

        // Check 3x3 box
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((startRow + i != row || startCol + j != col) && 
                    board[startRow + i][startCol + j] == num) return false;
            }
        }

        return true;
    }

    private static int[] findMRV(int[][] board) {
        int minOptions = Integer.MAX_VALUE;
        int[] cell = null;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    List<Integer> possibleValues = getPossibleValues(board, i, j);
                    int options = possibleValues.size();
                    if (options == 0) {
                        continue;  // Skip cells with no valid options
                    }
                    if (options < minOptions) {
                        minOptions = options;
                        cell = new int[]{i, j};
                    } else if (options == minOptions) {
                        // Use Degree Heuristic if MRV is tied
                        if (cell != null && getDegree(board, i, j) > getDegree(board, cell[0], cell[1])) {
                            cell = new int[]{i, j};
                        }
                    }
                }
            }
        }
        return cell;
    }

    private static List<Integer> getPossibleValues(int[][] board, int row, int col) {
        List<Integer> values = new ArrayList<>();
        for (int num = 1; num <= SIZE; num++) {
            if (isValid(board, row, col, num)) {
                values.add(num);
            }
        }
        return values;
    }

    private static int getDegree(int[][] board, int row, int col) {
        Set<String> unfilledNeighbors = new HashSet<>();

        // Check row and column
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == 0) unfilledNeighbors.add(row + "," + i);
            if (board[i][col] == 0) unfilledNeighbors.add(i + "," + col);
        }

        // Check 3x3 box
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[startRow + i][startCol + j] == 0) {
                    unfilledNeighbors.add((startRow + i) + "," + (startCol + j));
                }
            }
        }

        return unfilledNeighbors.size();
    }
}