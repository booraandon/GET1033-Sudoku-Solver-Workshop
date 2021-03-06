import java.util.Stack;

public class GameGen{

    public static final int GAMESIZE = 9;

    private Cell[][] board;

    public GameGen(){
        board = new Cell[GAMESIZE][GAMESIZE];
        initializeCells();
    }

    public void generate(){
        updateAllDomains();
        board = backtrackingSearch();
    }

    private void initializeCells() {
        for(int r = 0; r < GAMESIZE; r++) {
            for(int c = 0; c < GAMESIZE; c++) {
                board[r][c] = new Cell();
            }
        }
    }

    private Cell[][] backtrackingSearch() {
		/*-- Initialize a stack to store the game instance history --*/
        Stack<GameInstance> history = new Stack();
		/*-- Initialize counters for the current row and column --*/
        int curRow = 0;
        int curColumn = 0;
		
		/*-- Save the initial state --*/
        Cell cell = board[curRow][curColumn].getDefensiveCopy();
        history.push(new GameInstance(curRow, curColumn, cell, board));
		
		/*-- Loop until done --*/
        while(!history.peek().isBoardFilled()) {
			/*-- Get the current state and the cell to fill--*/
            GameInstance curGI = history.peek().getDefensiveCopy();
            Cell cellToTry = curGI.board[curRow][curColumn];
			
            if(cellToTry.isInitialized) {
                /*-- Move to next cell --*/
                curRow++;
                if (curRow >= GAMESIZE) {
                    curRow = 0;
                    curColumn++;
                    if (curColumn >= GAMESIZE) {
                        break;
                    }
                }
            }
            else if(cellToTry.domain.size() == 0) {
                /*-- Backtrack --*/
                GameInstance valueToRemoveGI = history.pop();
                GameInstance domainToReduceGI = history.pop();
                domainToReduceGI
				.board[valueToRemoveGI.row][valueToRemoveGI.column]
				.domain.remove(new Integer(valueToRemoveGI.cell.value));
                history.push(domainToReduceGI);
                curRow = valueToRemoveGI.row;
                curColumn = valueToRemoveGI.column;
            }
            else {
				/*-- Fill a cell --*/
                cellToTry.tryFill();
                maintainArcConsistency(curRow, curColumn, cellToTry, curGI);
                GameInstance newGI = new GameInstance(curRow, curColumn, cellToTry, curGI.board);
                history.push(newGI);

                /*-- Move to next cell --*/
                curRow++;
                if (curRow >= GAMESIZE) {
                    curRow = 0;
                    curColumn++;
                    if (curColumn >= GAMESIZE) {
                        break;
                    }
                }
            }
        }
		/*-- Return latest board --*/
        return history.peek().board;
    }

    private void maintainArcConsistency(int row, int column, Cell cell, GameInstance GI) {
        // Maintain row
        for(int c = 0; c < GAMESIZE; c++){
            GI.board[row][c].domain.remove(new Integer(cell.value));
        }
        // Maintain column
        for(int r = 0; r < GAMESIZE; r++) {
            GI.board[r][column].domain.remove(new Integer(cell.value));
        }
        // Maintain square
        int rSquare = row / 3 * 3;
        int cSquare = column / 3 * 3;
        for(int r = 0; r < (int)Math.sqrt(GAMESIZE); r++) {
            for(int c = 0; c < (int)Math.sqrt(GAMESIZE); c++) {
                GI.board[rSquare + r][cSquare + c].domain.remove(new Integer(cell.value));
            }
        }
    }

    private void maintainArcConsistency(int row, int column, Cell cell) {
        // Maintain row
        for(int c = 0; c < GAMESIZE; c++){
            board[row][c].domain.remove(new Integer(cell.value));
        }
        // Maintain column
        for(int r = 0; r < GAMESIZE; r++) {
            board[r][column].domain.remove(new Integer(cell.value));
        }
        // Maintain square
        int rSquare = row / 3 * 3;
        int cSquare = column / 3 * 3;
        for(int r = 0; r < (int)Math.sqrt(GAMESIZE); r++) {
            for(int c = 0; c < (int)Math.sqrt(GAMESIZE); c++) {
                board[rSquare + r][cSquare + c].domain.remove(new Integer(cell.value));
            }
        }
    }

    public void display(){
        for(int r = 0; r < GAMESIZE; r++){
            for(int c = 0; c < GAMESIZE; c++){
                System.out.print(board[r][c].value + " ");
                if((c + 1) % (int)Math.sqrt(GAMESIZE) == 0 && (c + 1) != GAMESIZE) {
                  System.out.print(" | ");
                }
            }
            System.out.println();
            if((r + 1) % (int)Math.sqrt(GAMESIZE) == 0 && (r + 1) != GAMESIZE) {
              System.out.println("------------------------");
            }
        }
    }

    public void fillCellsWithInput(char[][] inputs) {
        for(int r = 0; r < GAMESIZE; r++) {
            for(int c = 0; c < GAMESIZE; c++) {
              char input = inputs[r][c];
              if(Character.isDigit(input)) {
                board[r][c].fill(Character.getNumericValue(input));
              }
            }
        }
    }

    public void updateAllDomains() {
        for(int r = 0; r < GAMESIZE; r++){
            for(int c = 0; c < GAMESIZE; c++){
                if(board[r][c].isInitialized) {
                  maintainArcConsistency(r, c, board[r][c]);
                }
            }
        }
    }
}
