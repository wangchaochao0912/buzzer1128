package com.example.buzzer;

import java.util.Scanner;

public class GomokuGame {
    private static final int BOARD_SIZE = 15;
    private String[][] board;
    private boolean gameStarted;
    private String currentPlayer;
    private boolean gameOver;
    private Scanner scanner;
    private int blackMoves;
    private int whiteMoves;
    private java.util.Random random;

    public GomokuGame() {
        board = new String[BOARD_SIZE][BOARD_SIZE];
        gameStarted = false;
        currentPlayer = "黑";
        gameOver = false;
        scanner = new Scanner(System.in);
        blackMoves = 0;
        whiteMoves = 0;
        random = new java.util.Random();
    }

    public void start() {
        System.out.println("=== 五子棋游戏 ===");
        
        while (true) {
            showMenu();
            int choice = getMenuChoice();
            
            switch (choice) {
                case 1:
                    startNewGame();
                    break;
                case 2:
                    if (gameStarted && !gameOver) {
                        playGame();
                    } else {
                        System.out.println("请先开始游戏！");
                    }
                    break;
                case 3:
                    exitGame();
                    return;
                default:
                    System.out.println("无效的选择，请重新输入。");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n===== 菜单 =====");
        System.out.println("1. 开始游戏");
        System.out.println("2. 继续游戏");
        System.out.println("3. 退出游戏");
        System.out.print("请选择: ");
    }

    private int getMenuChoice() {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // 清除输入缓冲区
            return -1;
        }
    }

    private void startNewGame() {
        // 初始化棋盘
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = "-";
            }
        }
        gameStarted = true;
        currentPlayer = "黑";
        gameOver = false;
        blackMoves = 0;
        whiteMoves = 0;
        System.out.println("游戏开始！黑方先手。");
        System.out.println("特殊规则：每方每走3步，随机将对方的一个棋子变为自己的颜色！");
        displayBoard();
        playGame();
    }

    private void playGame() {
        while (gameStarted && !gameOver) {
            System.out.println("\n当前玩家: " + currentPlayer);
            System.out.print("请输入落子位置 (行 列): ");
            
            try {
                int row = scanner.nextInt() - 1; // 转换为0-based索引
                int col = scanner.nextInt() - 1;
                
                if (isValidMove(row, col)) {
                    board[row][col] = currentPlayer;
                    displayBoard();
                    
                    // 更新步数计数
                    if (currentPlayer.equals("黑")) {
                        blackMoves++;
                        // 检查是否需要变色
                        if (blackMoves % 3 == 0) {
                            convertRandomOpponentPiece("白", "黑");
                        }
                    } else {
                        whiteMoves++;
                        // 检查是否需要变色
                        if (whiteMoves % 3 == 0) {
                            convertRandomOpponentPiece("黑", "白");
                        }
                    }
                    
                    if (checkWin(row, col)) {
                        gameOver = true;
                        System.out.println("恭喜！" + currentPlayer + "方获胜！");
                    } else if (isBoardFull()) {
                        gameOver = true;
                        System.out.println("棋盘已满，游戏结束！");
                    } else {
                        // 切换玩家
                        currentPlayer = currentPlayer.equals("黑") ? "白" : "黑";
                    }
                } else {
                    System.out.println("无效的位置，请重新输入。");
                }
            } catch (Exception e) {
                System.out.println("输入格式错误，请输入两个数字。");
                scanner.nextLine(); // 清除输入缓冲区
            }
        }
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col].equals("-");
    }

    private void displayBoard() {
        System.out.println("  1 2 3 4 5 6 7 8 9 0 1 2 3 4 5");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print((i + 1) + (i + 1 < 10 ? " " : ""));
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    private boolean checkWin(int row, int col) {
        String player = board[row][col];
        
        // 检查水平方向
        int count = 1;
        for (int c = col - 1; c >= 0 && board[row][c].equals(player); c--) {
            count++;
        }
        for (int c = col + 1; c < BOARD_SIZE && board[row][c].equals(player); c++) {
            count++;
        }
        if (count >= 5) return true;

        // 检查垂直方向
        count = 1;
        for (int r = row - 1; r >= 0 && board[r][col].equals(player); r--) {
            count++;
        }
        for (int r = row + 1; r < BOARD_SIZE && board[r][col].equals(player); r++) {
            count++;
        }
        if (count >= 5) return true;

        // 检查对角线方向（左上到右下）
        count = 1;
        for (int r = row - 1, c = col - 1; r >= 0 && c >= 0 && board[r][c].equals(player); r--, c--) {
            count++;
        }
        for (int r = row + 1, c = col + 1; r < BOARD_SIZE && c < BOARD_SIZE && board[r][c].equals(player); r++, c++) {
            count++;
        }
        if (count >= 5) return true;

        // 检查对角线方向（右上到左下）
        count = 1;
        for (int r = row - 1, c = col + 1; r >= 0 && c < BOARD_SIZE && board[r][c].equals(player); r--, c++) {
            count++;
        }
        for (int r = row + 1, c = col - 1; r < BOARD_SIZE && c >= 0 && board[r][c].equals(player); r++, c--) {
            count++;
        }
        if (count >= 5) return true;

        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].equals("-")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void convertRandomOpponentPiece(String opponentColor, String myColor) {
        // 收集对手的所有棋子位置
        java.util.List<int[]> opponentPieces = new java.util.ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].equals(opponentColor)) {
                    opponentPieces.add(new int[]{i, j});
                }
            }
        }
        
        // 如果有对手棋子，随机选择一个并改变颜色
        if (!opponentPieces.isEmpty()) {
            int[] selectedPiece = opponentPieces.get(random.nextInt(opponentPieces.size()));
            int row = selectedPiece[0];
            int col = selectedPiece[1];
            board[row][col] = myColor;
            System.out.println("特殊规则触发：随机将位置 (" + (row + 1) + ", " + (col + 1) + ") 的" + opponentColor + "子变为" + myColor + "子！");
            displayBoard();
        }
    }
    
    private void exitGame() {
        System.out.println("感谢游玩，再见！");
        scanner.close();
    }

    public static void main(String[] args) {
        GomokuGame game = new GomokuGame();
        game.start();
    }
}