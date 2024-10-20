package AICp;
import java.util.*;
import java.util.stream.IntStream;

public class SimulatedAnnealingSudoku {
    private static final int SIZE = 9;
    private static final double COOLING_RATE = 0.99;
    private static final int MAX_ATTEMPTS = 10; // Maximum number of attempts to solve

    public static boolean solve(int[][] board) {
        try {
            int[][] fixedSudoku = markFixedCells(board);
            List<List<int[]>> blocks = create3x3Blocks();
            
            // Fill the board in-place
            fillBoardInPlace(board, blocks);
            
            double sigma = calculateInitialSigma(board, fixedSudoku, blocks);
            int iterations = countNonZeroCells(fixedSudoku);
            
            // Try multiple times to find a solution
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                int score = calculateErrors(board);
                double currentSigma = sigma;
                
                while (score > 0 && currentSigma > 0.01) {  // Add minimum temperature threshold
                    for (int i = 0; i < iterations; i++) {
                        var result = chooseNewState(board, fixedSudoku, blocks, currentSigma);
                        if (result.sudoku != board) {  // If a new state was accepted
                            copyBoard(result.sudoku, board);
                            score += result.costDifference;
                        }
                        if (score == 0) return true;
                    }
                    currentSigma *= COOLING_RATE;
                }
                
                if (score == 0) return true;
                
                // If not solved, reset and try again
                if (attempt < MAX_ATTEMPTS - 1) {
                    fillBoardInPlace(board, blocks);
                }
            }
            
            return false;  // Could not find solution
        } catch (Exception e) {
            return false;  // Return false if any error occurs
        }
    }

    private static void copyBoard(int[][] source, int[][] destination) {
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, SIZE);
        }
    }

    private static void fillBoardInPlace(int[][] board, List<List<int[]>> blocks) {
        Random rand = new Random();
        
        for (List<int[]> block : blocks) {
            Set<Integer> used = new HashSet<>();
            // First collect fixed numbers in this block
            for (int[] cell : block) {
                int value = board[cell[0]][cell[1]];
                if (value != 0) {
                    used.add(value);
                }
            }
            // Then fill empty cells
            for (int[] cell : block) {
                if (board[cell[0]][cell[1]] == 0) {
                    int num;
                    do {
                        num = rand.nextInt(9) + 1;
                    } while (used.contains(num));
                    board[cell[0]][cell[1]] = num;
                    used.add(num);
                }
            }
        }
    }

    private static int[][] markFixedCells(int[][] board) {
        int[][] fixed = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != 0) fixed[i][j] = 1;
            }
        }
        return fixed;
    }

    private static List<List<int[]>> create3x3Blocks() {
        List<List<int[]>> blocks = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            List<int[]> block = new ArrayList<>();
            for (int r = (i / 3) * 3; r < (i / 3) * 3 + 3; r++) {
                for (int c = (i % 3) * 3; c < (i % 3) * 3 + 3; c++) {
                    block.add(new int[]{r, c});
                }
            }
            blocks.add(block);
        }
        return blocks;
    }

    private static int countNonZeroCells(int[][] board) {
        return (int) Arrays.stream(board)
                .flatMapToInt(Arrays::stream)
                .filter(cell -> cell != 0)
                .count();
    }

    private static int calculateErrors(int[][] board) {
        int errors = 0;
        for (int i = 0; i < SIZE; i++) {
            errors += calculateErrorsInRowColumn(i, board);
        }
        return errors;
    }

    private static int calculateErrorsInRowColumn(int index, int[][] board) {
        int rowErrors = SIZE - (int) Arrays.stream(board[index]).distinct().count();
        int colErrors = SIZE - (int) IntStream.range(0, SIZE).map(i -> board[i][index]).distinct().count();
        return rowErrors + colErrors;
    }

    private static double calculateInitialSigma(int[][] board, int[][] fixedSudoku, List<List<int[]>> blocks) {
        List<Integer> differences = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            var proposal = proposeNewState(board, fixedSudoku, blocks);
            differences.add(calculateErrors(proposal.sudoku));
        }
        
        double mean = differences.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = differences.stream()
                .mapToDouble(diff -> Math.pow(diff - mean, 2))
                .average()
                .orElse(0.0);
        
        return Math.sqrt(variance);
    }

    private static Proposal chooseNewState(int[][] board, int[][] fixedSudoku, List<List<int[]>> blocks, double sigma) {
        var proposal = proposeNewState(board, fixedSudoku, blocks);
        int[][] newSudoku = proposal.sudoku;
        int costDifference = calculateErrors(newSudoku) - calculateErrors(board);

        if (costDifference < 0 || Math.exp(-costDifference / sigma) > Math.random()) {
            return new Proposal(newSudoku, costDifference);
        }
        return new Proposal(board, 0);
    }

    private static Proposal proposeNewState(int[][] board, int[][] fixedSudoku, List<List<int[]>> blocks) {
        Random rand = new Random();
        List<int[]> block = blocks.get(rand.nextInt(blocks.size()));
        int[][] newSudoku = Arrays.stream(board).map(int[]::clone).toArray(int[][]::new);

        int[] first = block.get(rand.nextInt(block.size()));
        int[] second = block.get(rand.nextInt(block.size()));

        while (fixedSudoku[first[0]][first[1]] == 1 || fixedSudoku[second[0]][second[1]] == 1) {
            first = block.get(rand.nextInt(block.size()));
            second = block.get(rand.nextInt(block.size()));
        }

        int temp = newSudoku[first[0]][first[1]];
        newSudoku[first[0]][first[1]] = newSudoku[second[0]][second[1]];
        newSudoku[second[0]][second[1]] = temp;

        return new Proposal(newSudoku, 0);
    }

    private static class Proposal {
        int[][] sudoku;
        int costDifference;

        Proposal(int[][] sudoku, int costDifference) {
            this.sudoku = sudoku;
            this.costDifference = costDifference;
        }
    }
}