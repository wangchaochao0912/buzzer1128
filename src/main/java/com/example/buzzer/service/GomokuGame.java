package com.example.buzzer.service;

public class GomokuGame {
    private static final int BOARD_SIZE = 15;
    private static final int WIN_COUNT = 5;
    
    private String[][] board;
    private String currentPlayer;
    private boolean gameStarted;
    private boolean gameOver;
    private String winner;
    private int blackMoveCount;
    private int whiteMoveCount;
    
    public GomokuGame() {
        board = new String[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = "black";
        gameStarted = false;
        gameOver = false;
        winner = null;
        blackMoveCount = 0;
        whiteMoveCount = 0;
        initBoard();
    }
    
    private void initBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = "";
            }
        }
    }
    
    public void startGame() {
        gameStarted = true;
        gameOver = false;
        winner = null;
        currentPlayer = "black";
        blackMoveCount = 0;
        whiteMoveCount = 0;
        initBoard();
    }
    
    public void exitGame() {
        gameStarted = false;
        gameOver = false;
        winner = null;
        currentPlayer = "black";
        blackMoveCount = 0;
        whiteMoveCount = 0;
        initBoard();
    }
    
    public boolean makeMove(int row, int col) {
        if (!gameStarted || gameOver) {
            return false;
        }
        
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false;
        }
        
        if (!board[row][col].isEmpty()) {
            return false;
        }
        
        board[row][col] = currentPlayer;
        
        // 增加当前玩家的步数
        if (currentPlayer.equals("black")) {
            blackMoveCount++;
            // 每3步，随机将一个黑棋变成白棋
            if (blackMoveCount % 3 == 0) {
                convertRandomPiece("black", "white");
            }
        } else {
            whiteMoveCount++;
            // 每3步，随机将一个白棋变成黑棋
            if (whiteMoveCount % 3 == 0) {
                convertRandomPiece("white", "black");
            }
        }
        
        if (checkWin(row, col)) {
            gameOver = true;
            winner = currentPlayer;
        } else if (checkDraw()) {
            gameOver = true;
            winner = null;
        } else {
            switchPlayer();
        }
        
        return true;
    }
    
    /**
     * 随机将一个指定颜色的棋子变成另一种颜色
     * @param fromColor 原颜色
     * @param toColor 目标颜色
     */
    private void convertRandomPiece(String fromColor, String toColor) {
        // 收集所有指定颜色的棋子位置
        java.util.List<int[]> pieces = new java.util.ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].equals(fromColor)) {
                    pieces.add(new int[]{i, j});
                }
            }
        }
        
        // 如果有至少一个棋子，随机选择一个并转换颜色
        if (!pieces.isEmpty()) {
            java.util.Random random = new java.util.Random();
            int[] selectedPiece = pieces.get(random.nextInt(pieces.size()));
            board[selectedPiece[0]][selectedPiece[1]] = toColor;
        }
    }
    
    private void switchPlayer() {
        currentPlayer = currentPlayer.equals("black") ? "white" : "black";
    }
    
    private boolean checkWin(int row, int col) {
        // 检查横向
        int count = 1;
        for (int j = col + 1; j < BOARD_SIZE && board[row][j].equals(currentPlayer); j++) {
            count++;
        }
        for (int j = col - 1; j >= 0 && board[row][j].equals(currentPlayer); j--) {
            count++;
        }
        if (count >= WIN_COUNT) {
            return true;
        }
        
        // 检查纵向
        count = 1;
        for (int i = row + 1; i < BOARD_SIZE && board[i][col].equals(currentPlayer); i++) {
            count++;
        }
        for (int i = row - 1; i >= 0 && board[i][col].equals(currentPlayer); i--) {
            count++;
        }
        if (count >= WIN_COUNT) {
            return true;
        }
        
        // 检查主对角线 (左上到右下)
        count = 1;
        for (int i = row + 1, j = col + 1; i < BOARD_SIZE && j < BOARD_SIZE && board[i][j].equals(currentPlayer); i++, j++) {
            count++;
        }
        for (int i = row - 1, j = col - 1; i >= 0 && j >= 0 && board[i][j].equals(currentPlayer); i--, j--) {
            count++;
        }
        if (count >= WIN_COUNT) {
            return true;
        }
        
        // 检查副对角线 (右上到左下)
        count = 1;
        for (int i = row + 1, j = col - 1; i < BOARD_SIZE && j >= 0 && board[i][j].equals(currentPlayer); i++, j--) {
            count++;
        }
        for (int i = row - 1, j = col + 1; i >= 0 && j < BOARD_SIZE && board[i][j].equals(currentPlayer); i--, j++) {
            count++;
        }
        if (count >= WIN_COUNT) {
            return true;
        }
        
        return false;
    }
    
    private boolean checkDraw() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public String[][] getBoard() {
        return board;
    }
    
    public String getCurrentPlayer() {
        return currentPlayer;
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public int getBoardSize() {
        return BOARD_SIZE;
    }
}
