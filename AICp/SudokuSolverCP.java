package AICp;
import java.awt.Point;
import java.util.*;

public class SudokuSolverCP {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private static Set<Integer>[][] domains;
    private static int[][] grid;
    private static int steps = 0;

    public static boolean solveSudokuConstraint(int[][] inputGrid) {
        grid = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            grid[i] = inputGrid[i].clone();
        }
        steps = 0;
        initializeDomains();
        printState("Initial State");
        boolean solved = ac3() && backtrack();
        
        // Copy solution back to input grid if solved
        if (solved) {
            for (int i = 0; i < SIZE; i++) {
                System.arraycopy(grid[i], 0, inputGrid[i], 0, SIZE);
            }
        }
        
        return solved;
    }

    private static void printState(String message) {
        System.out.println("\n" + message + " (Step " + steps++ + ")");
        System.out.println("Current Grid:");
        for (int i = 0; i < SIZE; i++) {
            if (i % 3 == 0 && i != 0) {
                System.out.println("- - - - - - - - - - - -");
            }
            for (int j = 0; j < SIZE; j++) {
                if (j % 3 == 0 && j != 0) {
                    System.out.print("| ");
                }
                System.out.print(grid[i][j] == 0 ? ". " : grid[i][j] + " ");
            }
            System.out.println();
        }
        
        System.out.println("\nDomain sizes:");
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 0) {
                    System.out.printf("(%d,%d):%d ", i, j, domains[i][j].size());
                }
            }
        }
        System.out.println("\n");
    }

    private static void initializeDomains() {
        domains = new HashSet[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                domains[row][col] = new HashSet<>();
                if (grid[row][col] == 0) {
                    for (int num = 1; num <= SIZE; num++) {
                        if (isValidInitial(num, row, col)) {
                            domains[row][col].add(num);
                        }
                    }
                } else {
                    domains[row][col].add(grid[row][col]);
                }
            }
        }
    }

    private static boolean isValidInitial(int num, int row, int col) {
        for (int i = 0; i < SIZE; i++) {
            if (grid[row][i] == num || grid[i][col] == num) return false;
        }
        
        int startRow = row - row % SUBGRID_SIZE;
        int startCol = col - col % SUBGRID_SIZE;
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                if (grid[i + startRow][j + startCol] == num) return false;
            }
        }
        return true;
    }

    private static boolean ac3() {
        Queue<Arc> queue = new LinkedList<>();
        Set<String> processed = new HashSet<>();  // To avoid processing same arcs repeatedly
        
        // Initialize queue with all arcs
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == 0) {
                    for (Point neighbor : getNeighbors(row, col)) {
                        Arc arc = new Arc(new Point(row, col), neighbor);
                        String arcKey = row + "," + col + "-" + neighbor.x + "," + neighbor.y;
                        if (!processed.contains(arcKey)) {
                            queue.add(arc);
                            processed.add(arcKey);
                        }
                    }
                }
            }
        }

        while (!queue.isEmpty()) {
            Arc arc = queue.poll();
            if (revise(arc)) {
                Point source = arc.source;
                if (domains[source.x][source.y].isEmpty()) {
                    System.out.println("AC-3: Domain empty at (" + source.x + "," + source.y + ")");
                    return false;
                }
                if (domains[source.x][source.y].size() == 1) {
                    printState("Value deduced during AC-3 at (" + source.x + "," + source.y + ")");
                }
                for (Point neighbor : getNeighbors(source.x, source.y)) {
                    if (!neighbor.equals(arc.target)) {
                        String arcKey = neighbor.x + "," + neighbor.y + "-" + source.x + "," + source.y;
                        if (!processed.contains(arcKey)) {
                            queue.add(new Arc(neighbor, source));
                            processed.add(arcKey);
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean revise(Arc arc) {
        boolean revised = false;
        Point source = arc.source;
        Point target = arc.target;
        
        if (grid[target.x][target.y] != 0) {
            int targetValue = grid[target.x][target.y];
            if (domains[source.x][source.y].contains(targetValue)) {
                domains[source.x][source.y].remove(targetValue);
                revised = true;
            }
            return revised;
        }
        
        Set<Integer> toRemove = new HashSet<>();
        for (int x : domains[source.x][source.y]) {
            boolean hasValidValue = false;
            for (int y : domains[target.x][target.y]) {
                if (isConsistent(x, y, source, target)) {
                    hasValidValue = true;
                    break;
                }
            }
            if (!hasValidValue) {
                toRemove.add(x);
                revised = true;
            }
        }
        
        domains[source.x][source.y].removeAll(toRemove);
        return revised;
    }

    private static boolean backtrack() {
        Point emptyCell = findMRV();
        if (emptyCell == null) {
            printState("Solution Found!");
            return true;
        }

        int row = emptyCell.x;
        int col = emptyCell.y;
        List<Integer> orderedValues = new ArrayList<>(domains[row][col]);
        Collections.sort(orderedValues); // You could implement LCV heuristic here

        for (int num : orderedValues) {
            if (isValid(num, row, col)) {
                grid[row][col] = num;
                Set<Integer> oldDomain = new HashSet<>(domains[row][col]);
                domains[row][col] = new HashSet<>(Collections.singleton(num));
                
                Map<Point, Set<Integer>> savedDomains = saveDomainsState();
                printState("Trying " + num + " at (" + row + "," + col + ")");
                
                if (forwardCheck(row, col) && backtrack()) {
                    return true;
                }
                
                printState("Backtracking from " + num + " at (" + row + "," + col + ")");
                grid[row][col] = 0;
                restoreDomainsState(savedDomains);
                domains[row][col] = oldDomain;
            }
        }
        return false;
    }

    private static Point findMRV() {
        Point bestCell = null;
        int minDomainSize = Integer.MAX_VALUE;
        
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == 0) {
                    int domainSize = domains[row][col].size();
                    if (domainSize < minDomainSize) {
                        minDomainSize = domainSize;
                        bestCell = new Point(row, col);
                    }
                }
            }
        }
        return bestCell;
    }

    private static Map<Point, Set<Integer>> saveDomainsState() {
        Map<Point, Set<Integer>> state = new HashMap<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 0) {
                    state.put(new Point(i, j), new HashSet<>(domains[i][j]));
                }
            }
        }
        return state;
    }

    private static void restoreDomainsState(Map<Point, Set<Integer>> state) {
        for (Map.Entry<Point, Set<Integer>> entry : state.entrySet()) {
            Point p = entry.getKey();
            domains[p.x][p.y] = new HashSet<>(entry.getValue());
        }
    }

    // Rest of the helper methods remain the same
    private static boolean isConsistent(int value1, int value2, Point pos1, Point pos2) {
        if (pos1.x == pos2.x || pos1.y == pos2.y) {
            return value1 != value2;
        }
        
        int subgridRow1 = pos1.x / SUBGRID_SIZE;
        int subgridCol1 = pos1.y / SUBGRID_SIZE;
        int subgridRow2 = pos2.x / SUBGRID_SIZE;
        int subgridCol2 = pos2.y / SUBGRID_SIZE;
        
        if (subgridRow1 == subgridRow2 && subgridCol1 == subgridCol2) {
            return value1 != value2;
        }
        
        return true;
    }

    private static boolean isValid(int num, int row, int col) {
        for (int i = 0; i < SIZE; i++) {
            if ((i != col && grid[row][i] == num) || 
                (i != row && grid[i][col] == num)) {
                return false;
            }
        }
        
        int startRow = row - row % SUBGRID_SIZE;
        int startCol = col - col % SUBGRID_SIZE;
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                if ((startRow + i != row || startCol + j != col) && 
                    grid[i + startRow][j + startCol] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean forwardCheck(int row, int col) {
        for (Point neighbor : getNeighbors(row, col)) {
            if (grid[neighbor.x][neighbor.y] == 0) {
                domains[neighbor.x][neighbor.y].remove(grid[row][col]);
                if (domains[neighbor.x][neighbor.y].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class Arc {
        Point source;
        Point target;

        Arc(Point source, Point target) {
            this.source = source;
            this.target = target;
        }
    }

    private static Set<Point> getNeighbors(int row, int col) {
        Set<Point> neighbors = new HashSet<>();
        
        for (int i = 0; i < SIZE; i++) {
            if (i != col) neighbors.add(new Point(row, i));
            if (i != row) neighbors.add(new Point(i, col));
        }
        
        int startRow = row - row % SUBGRID_SIZE;
        int startCol = col - col % SUBGRID_SIZE;
        for (int r = 0; r < SUBGRID_SIZE; r++) {
            for (int c = 0; c < SUBGRID_SIZE; c++) {
                if (startRow + r != row || startCol + c != col) {
                    neighbors.add(new Point(startRow + r, startCol + c));
                }
            }
        }
        return neighbors;
    }
}