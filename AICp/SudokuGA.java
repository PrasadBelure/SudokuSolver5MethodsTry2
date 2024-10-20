package AICp;
import java.util.*;

public class SudokuGA {
    private static final int SIZE = 9;
    private static final int SUBGRID = 3;
    private static final int POPULATION_SIZE = 1000;
    private static final int MAX_GENERATIONS = 1000;
    private static final double MUTATION_RATE = 0.1;
    private static final double CROSSOVER_RATE = 0.95;
    private static final int STAGNATION_LIMIT = 50;
    private static final Random rand = new Random();

    public static boolean solve(int[][] board) {
        try {
            // Create a copy of the initial board
            int[][] initialBoard = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++) {
                System.arraycopy(board[i], 0, initialBoard[i], 0, SIZE);
            }

            // Pre-process to find fixed cells and available numbers for each cell
            boolean[][][] fixedCells = new boolean[SIZE][SIZE][SIZE + 1];
            List<Integer>[][] availableNumbers = preprocessBoard(initialBoard);

            // If any cell has no available numbers, the puzzle is unsolvable
            if (!isValidInitialBoard(availableNumbers)) {
                System.out.println("No valid solutions possible with current constraints");
                return false;
            }

            List<int[][]> population = initializePopulation(initialBoard, availableNumbers);
            int bestFitness = Integer.MIN_VALUE;
            int stagnationCounter = 0;
            int[][] lastBestSolution = null;
            
            // Track best solutions for diversity
            Set<String> seenSolutions = new HashSet<>();
            List<int[][]> elitePool = new ArrayList<>();

            for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
                // Adaptive mutation rate based on population diversity
                double currentDiversity = calculateDiversity(population);
                double adaptiveMutationRate = MUTATION_RATE * (1 + (1 - currentDiversity));

                // Get mating pool using improved selection
                List<int[][]> matingPool = getMatingPool(population, generation);
                
                // Create new population through improved crossover and mutation
                population = evolvePopulation(matingPool, initialBoard, availableNumbers, adaptiveMutationRate);
                
                // Find best solution
                int[][] bestSolution = getBestSolution(population);
                int currentFitness = calculateFitness(bestSolution);
                
                // Store unique elite solutions
                String solutionHash = boardToString(bestSolution);
                if (!seenSolutions.contains(solutionHash) && currentFitness > -10) {
                    seenSolutions.add(solutionHash);
                    elitePool.add(cloneBoard(bestSolution));
                    if (elitePool.size() > 50) { // Keep top 50 unique solutions
                        elitePool.sort((a, b) -> calculateFitness(b) - calculateFitness(a));
                        elitePool = elitePool.subList(0, 50);
                    }
                }

                // Print progress every 10 generations
                if (generation % 10 == 0) {
                    System.out.println("Generation " + generation + 
                                     ", Best Fitness: " + currentFitness + 
                                     ", Diversity: " + String.format("%.2f", currentDiversity) +
                                     ", Mutation Rate: " + String.format("%.2f", adaptiveMutationRate) +
                                     ", Elite Pool: " + elitePool.size());
                }

                // Check for improvement
                if (currentFitness > bestFitness) {
                    bestFitness = currentFitness;
                    lastBestSolution = bestSolution;
                    stagnationCounter = 0;
                } else {
                    stagnationCounter++;
                }

                // Solution found
                if (currentFitness == 0) {
                    System.out.println("Solution found at generation " + generation);
                    for (int i = 0; i < SIZE; i++) {
                        System.arraycopy(bestSolution[i], 0, board[i], 0, SIZE);
                    }
                    return true;
                }

                // If stuck, try to escape local optima
                if (stagnationCounter >= STAGNATION_LIMIT) {
                    System.out.println("Attempting to escape local optimum...");
                    
                    // Mix elite solutions with new random solutions
                    population = new ArrayList<>();
                    population.addAll(elitePool);
                    
                    // Add some completely new solutions
                    while (population.size() < POPULATION_SIZE) {
                        population.add(generateCandidate(initialBoard, availableNumbers));
                    }
                    
                    // Clear seen solutions but keep elite pool
                    seenSolutions.clear();
                    stagnationCounter = 0;
                }
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static List<Integer>[][] preprocessBoard(int[][] board) {
        @SuppressWarnings("unchecked")
        List<Integer>[][] availableNumbers = new List[SIZE][SIZE];
        
        // Initialize available numbers for each cell
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                availableNumbers[row][col] = new ArrayList<>();
                if (board[row][col] == 0) {
                    boolean[] used = new boolean[SIZE + 1];
                    
                    // Check row
                    for (int i = 0; i < SIZE; i++) {
                        used[board[row][i]] = true;
                    }
                    
                    // Check column
                    for (int i = 0; i < SIZE; i++) {
                        used[board[i][col]] = true;
                    }
                    
                    // Check block
                    int blockRow = (row / SUBGRID) * SUBGRID;
                    int blockCol = (col / SUBGRID) * SUBGRID;
                    for (int i = blockRow; i < blockRow + SUBGRID; i++) {
                        for (int j = blockCol; j < blockCol + SUBGRID; j++) {
                            used[board[i][j]] = true;
                        }
                    }
                    
                    // Add available numbers
                    for (int num = 1; num <= SIZE; num++) {
                        if (!used[num]) {
                            availableNumbers[row][col].add(num);
                        }
                    }
                } else {
                    availableNumbers[row][col].add(board[row][col]);
                }
            }
        }
        
        return availableNumbers;
    }

    private static boolean isValidInitialBoard(List<Integer>[][] availableNumbers) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (availableNumbers[row][col].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double calculateDiversity(List<int[][]> population) {
        Set<String> uniqueBoards = new HashSet<>();
        for (int[][] board : population) {
            uniqueBoards.add(boardToString(board));
        }
        return (double) uniqueBoards.size() / population.size();
    }

    private static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell);
            }
        }
        return sb.toString();
    }

    private static int[][] generateCandidate(int[][] initialBoard, List<Integer>[][] availableNumbers) {
        int[][] candidate = new int[SIZE][SIZE];
        
        // Copy fixed numbers
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (initialBoard[row][col] != 0) {
                    candidate[row][col] = initialBoard[row][col];
                }
            }
        }
        
        // Fill empty cells using available numbers
        for (int row = 0; row < SIZE; row++) {
            List<Integer> remainingNumbers = new ArrayList<>();
            for (int i = 1; i <= SIZE; i++) {
                remainingNumbers.add(i);
            }
            
            // Remove fixed numbers from remaining
            for (int col = 0; col < SIZE; col++) {
                if (candidate[row][col] != 0) {
                    remainingNumbers.remove(Integer.valueOf(candidate[row][col]));
                }
            }
            
            // Fill empty cells
            Collections.shuffle(remainingNumbers);
            int remainingIndex = 0;
            for (int col = 0; col < SIZE; col++) {
                if (candidate[row][col] == 0) {
                    // Prefer numbers from available list if possible
                    List<Integer> available = availableNumbers[row][col];
                    if (!available.isEmpty() && rand.nextDouble() < 0.8) {
                        candidate[row][col] = available.get(rand.nextInt(available.size()));
                        remainingNumbers.remove(Integer.valueOf(candidate[row][col]));
                    } else {
                        candidate[row][col] = remainingNumbers.get(remainingIndex++);
                    }
                }
            }
        }
        
        return candidate;
    }

    private static List<int[][]> initializePopulation(int[][] initialBoard, List<Integer>[][] availableNumbers) {
        List<int[][]> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateCandidate(initialBoard, availableNumbers));
        }
        return population;
    }
    
    private static List<int[][]> getMatingPool(List<int[][]> population, int generation) {
        List<int[][]> matingPool = new ArrayList<>();
        
        // Sort population by fitness
        population.sort((a, b) -> calculateFitness(b) - calculateFitness(a));
        
        // Adaptive tournament size based on generation
        int tournamentSize = 5 + (generation / 200); // Increases tournament pressure over time
        tournamentSize = Math.min(tournamentSize, 10); // Cap at 10
        
        // Tournament selection
        while (matingPool.size() < population.size()) {
            int[][] selected = tournamentSelect(population, tournamentSize);
            matingPool.add(cloneBoard(selected));
        }
        
        return matingPool;
    }

    private static List<int[][]> evolvePopulation(List<int[][]> matingPool, int[][] initialBoard, 
                                            List<Integer>[][] availableNumbers, double mutationRate) {
    List<int[][]> newPopulation = new ArrayList<>();
    
    // Keep best solutions (elitism)
    int eliteSize = POPULATION_SIZE / 20; // Keep top 5%
    for (int i = 0; i < eliteSize; i++) {
        newPopulation.add(cloneBoard(matingPool.get(i)));
    }
    
    // Create rest of new population through crossover and mutation
    while (newPopulation.size() < POPULATION_SIZE) {
        int[][] parent1 = tournamentSelect(matingPool, 3);
        int[][] parent2 = tournamentSelect(matingPool, 3);
        
        if (rand.nextDouble() < CROSSOVER_RATE) {
            int[][][] children = crossover(parent1, parent2);
            mutate(children[0], initialBoard, mutationRate, availableNumbers);
            mutate(children[1], initialBoard, mutationRate, availableNumbers);
            newPopulation.add(children[0]);
            if (newPopulation.size() < POPULATION_SIZE) {
                newPopulation.add(children[1]);
            }
        } else {
            newPopulation.add(cloneBoard(parent1));
            if (newPopulation.size() < POPULATION_SIZE) {
                newPopulation.add(cloneBoard(parent2));
            }
        }
    }
    
    return newPopulation;
}

private static int[][] mutate(int[][] candidate, int[][] initialBoard, 
                            double mutationRate, List<Integer>[][] availableNumbers) {
    for (int row = 0; row < SIZE; row++) {
        if (rand.nextDouble() < mutationRate) {
            // Only mutate rows that don't contain fixed numbers from initial board
            boolean hasFixed = false;
            for (int col = 0; col < SIZE; col++) {
                if (initialBoard[row][col] != 0) {
                    hasFixed = true;
                    break;
                }
            }
            
            if (!hasFixed) {
                if (rand.nextDouble() < 0.5) {
                    // Swap two random positions in the row
                    int pos1 = rand.nextInt(SIZE);
                    int pos2 = rand.nextInt(SIZE);
                    int temp = candidate[row][pos1];
                    candidate[row][pos1] = candidate[row][pos2];
                    candidate[row][pos2] = temp;
                } else {
                    // Try to replace a number with an available one
                    int col = rand.nextInt(SIZE);
                    if (!availableNumbers[row][col].isEmpty()) {
                        candidate[row][col] = availableNumbers[row][col]
                            .get(rand.nextInt(availableNumbers[row][col].size()));
                    }
                }
            }
        }
    }
    return candidate;
}


    private static void printBoard(int[][] board) {
        System.out.println("Current best board:");
        for (int i = 0; i < SIZE; i++) {
            if (i % 3 == 0 && i != 0) {
                System.out.println("-".repeat(21));
            }
            for (int j = 0; j < SIZE; j++) {
                if (j % 3 == 0 && j != 0) {
                    System.out.print("| ");
                }
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private static List<int[][]> initializePopulation(int[][] initialBoard) {
        List<int[][]> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateCandidate(initialBoard));
        }
        return population;
    }

    private static int[][] generateCandidate(int[][] initialBoard) {
        int[][] candidate = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            List<Integer> available = new ArrayList<>();
            for (int i = 1; i <= SIZE; i++) {
                available.add(i);
            }
            Collections.shuffle(available);
            
            // Copy fixed numbers from initial board
            for (int col = 0; col < SIZE; col++) {
                if (initialBoard[row][col] != 0) {
                    candidate[row][col] = initialBoard[row][col];
                    available.remove(Integer.valueOf(initialBoard[row][col]));
                }
            }
            
            // Fill remaining cells
            int availableIndex = 0;
            for (int col = 0; col < SIZE; col++) {
                if (candidate[row][col] == 0) {
                    candidate[row][col] = available.get(availableIndex++);
                }
            }
        }
        return candidate;
    }

    private static List<int[][]> getMatingPool(List<int[][]> population) {
        List<int[][]> matingPool = new ArrayList<>();
        
        // Sort population by fitness
        population.sort((a, b) -> calculateFitness(b) - calculateFitness(a));
        
        // Tournament selection
        while (matingPool.size() < population.size()) {
            int[][] selected = tournamentSelect(population, 5);
            matingPool.add(cloneBoard(selected));
        }
        
        return matingPool;
    }

    private static int[][] tournamentSelect(List<int[][]> population, int tournamentSize) {
        List<int[][]> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(population.get(rand.nextInt(population.size())));
        }
        return Collections.max(tournament, Comparator.comparingInt(SudokuGA::calculateFitness));
    }

    private static List<int[][]> evolvePopulation(List<int[][]> matingPool, int[][] initialBoard) {
        List<int[][]> newPopulation = new ArrayList<>();
        
        // Keep best solution (elitism)
        newPopulation.add(cloneBoard(getBestSolution(matingPool)));
        
        for (int i = 1; i < POPULATION_SIZE; i += 2) {
            int[][] parent1 = matingPool.get(i - 1);
            int[][] parent2 = matingPool.get(i);
            
            if (rand.nextDouble() < CROSSOVER_RATE) {
                int[][][] children = crossover(parent1, parent2);
                mutate(children[0], initialBoard);
                mutate(children[1], initialBoard);
                newPopulation.add(children[0]);
                if (newPopulation.size() < POPULATION_SIZE) {
                    newPopulation.add(children[1]);
                }
            } else {
                newPopulation.add(cloneBoard(parent1));
                if (newPopulation.size() < POPULATION_SIZE) {
                    newPopulation.add(cloneBoard(parent2));
                }
            }
        }
        
        return newPopulation;
    }

    private static int[][] getBestSolution(List<int[][]> population) {
        return Collections.max(population, Comparator.comparingInt(SudokuGA::calculateFitness));
    }

    private static int[][] cloneBoard(int[][] board) {
        int[][] clone = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(board[i], 0, clone[i], 0, SIZE);
        }
        return clone;
    }

    private static int[][] mutate(int[][] candidate, int[][] initialBoard) {
        for (int row = 0; row < SIZE; row++) {
            if (rand.nextDouble() < MUTATION_RATE) {
                // Only mutate rows that don't contain fixed numbers from initial board
                boolean hasFixed = false;
                for (int col = 0; col < SIZE; col++) {
                    if (initialBoard[row][col] != 0) {
                        hasFixed = true;
                        break;
                    }
                }
                if (!hasFixed) {
                    // Swap two random positions in the row
                    int pos1 = rand.nextInt(SIZE);
                    int pos2 = rand.nextInt(SIZE);
                    int temp = candidate[row][pos1];
                    candidate[row][pos1] = candidate[row][pos2];
                    candidate[row][pos2] = temp;
                }
            }
        }
        return candidate;
    }

    private static int[][][] crossover(int[][] parent1, int[][] parent2) {
        int[][] child1 = new int[SIZE][SIZE];
        int[][] child2 = new int[SIZE][SIZE];
        
        for (int row = 0; row < SIZE; row++) {
            if (rand.nextBoolean()) {
                System.arraycopy(parent1[row], 0, child1[row], 0, SIZE);
                System.arraycopy(parent2[row], 0, child2[row], 0, SIZE);
            } else {
                System.arraycopy(parent2[row], 0, child1[row], 0, SIZE);
                System.arraycopy(parent1[row], 0, child2[row], 0, SIZE);
            }
        }
        
        return new int[][][] { child1, child2 };
    }

    private static int calculateFitness(int[][] candidate) {
        int conflicts = 0;
        
        // Check rows (not needed as we maintain row validity in generation)
        for (int row = 0; row < SIZE; row++) {
            conflicts += countConflicts(getRow(candidate, row));
        }
        
        // Check columns
        for (int col = 0; col < SIZE; col++) {
            conflicts += countConflicts(getColumn(candidate, col));
        }
        
        // Check 3x3 subgrids
        for (int blockRow = 0; blockRow < SIZE; blockRow += SUBGRID) {
            for (int blockCol = 0; blockCol < SIZE; blockCol += SUBGRID) {
                conflicts += countConflicts(getBlock(candidate, blockRow, blockCol));
            }
        }
        
        return -conflicts; // Return negative conflicts as fitness (0 is perfect)
    }

    private static int countConflicts(List<Integer> numbers) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int num : numbers) {
            counts.merge(num, 1, Integer::sum);
        }
        
        int conflicts = 0;
        for (int count : counts.values()) {
            if (count > 1) {
                conflicts += (count - 1);
            }
        }
        return conflicts;
    }

    private static List<Integer> getRow(int[][] board, int row) {
        List<Integer> numbers = new ArrayList<>();
        for (int col = 0; col < SIZE; col++) {
            numbers.add(board[row][col]);
        }
        return numbers;
    }

    private static List<Integer> getColumn(int[][] board, int col) {
        List<Integer> numbers = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            numbers.add(board[row][col]);
        }
        return numbers;
    }

    private static List<Integer> getBlock(int[][] board, int blockRow, int blockCol) {
        List<Integer> numbers = new ArrayList<>();
        for (int row = blockRow; row < blockRow + SUBGRID; row++) {
            for (int col = blockCol; col < blockCol + SUBGRID; col++) {
                numbers.add(board[row][col]);
            }
        }
        return numbers;
    }
}