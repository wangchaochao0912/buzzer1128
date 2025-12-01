package com.example.buzzer.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class GomokuService {
    private static final int BOARD_SIZE = 15;
    private int[][] board1;
    private int[][] board2;
    private int currentPlayer;
    private boolean game1Active;
    private boolean game2Active;
    private int player1MoveCount;
    private int player2MoveCount;
    private Random random;

    public GomokuService() {
        initGame();
        random = new Random();
    }

    public void initGame() {
        board1 = new int[BOARD_SIZE][BOARD_SIZE];
        board2 = new int[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = 1;
        game1Active = true;
        game2Active = true;
        player1MoveCount = 0;
        player2MoveCount = 0;
    }

    public boolean makeMove(int boardIndex, int row, int col) {
        int[][] board = boardIndex == 1 ? board1 : board2;
        boolean gameActive = boardIndex == 1 ? game1Active : game2Active;

        if (!gameActive || row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || board[row][col] != 0) {
            return false;
        }

        board[row][col] = currentPlayer;

        // 更新玩家步数
        if (currentPlayer == 1) {
            player1MoveCount++;
            // 每走3步，随机将一个黑子变成白子
            if (player1MoveCount % 3 == 0) {
                convertRandomPiece(1, 2);
            }
        } else {
            player2MoveCount++;
            // 每走3步，随机将一个白子变成黑子
            if (player2MoveCount % 3 == 0) {
                convertRandomPiece(2, 1);
            }
        }

        if (checkWin(board, row, col)) {
            if (boardIndex == 1) {
                game1Active = false;
            } else {
                game2Active = false;
            }
        }

        // 只有当两个游戏都结束时，才切换玩家
        if (game1Active || game2Active) {
            currentPlayer = currentPlayer == 1 ? 2 : 1;
        }

        return true;
    }

    /**
     * 随机将一个玩家的棋子变成对方的颜色
     * @param fromPlayer 原玩家
     * @param toPlayer 目标玩家
     */
    private void convertRandomPiece(int fromPlayer, int toPlayer) {
        List<int[]> pieces = new ArrayList<>();
        // 收集所有fromPlayer的棋子
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board1[i][j] == fromPlayer) {
                    pieces.add(new int[]{1, i, j});
                }
                if (board2[i][j] == fromPlayer) {
                    pieces.add(new int[]{2, i, j});
                }
            }
        }
        // 如果有棋子，随机选择一个进行转换
        if (!pieces.isEmpty()) {
            int[] piece = pieces.get(random.nextInt(pieces.size()));
            int boardIndex = piece[0];
            int row = piece[1];
            int col = piece[2];
            if (boardIndex == 1) {
                board1[row][col] = toPlayer;
            } else {
                board2[row][col] = toPlayer;
            }
        }
    }

    private boolean checkWin(int[][] board, int row, int col) {
        int player = board[row][col];

        // 检查横向
        int count = 1;
        for (int c = col - 1; c >= 0 && board[row][c] == player; c--) count++;
        for (int c = col + 1; c < BOARD_SIZE && board[row][c] == player; c++) count++;
        if (count >= 5) return true;

        // 检查纵向
        count = 1;
        for (int r = row - 1; r >= 0 && board[r][col] == player; r--) count++;
        for (int r = row + 1; r < BOARD_SIZE && board[r][col] == player; r++) count++;
        if (count >= 5) return true;

        // 检查对角线（左上到右下）
        count = 1;
        for (int r = row - 1, c = col - 1; r >= 0 && c >= 0 && board[r][c] == player; r--, c--) count++;
        for (int r = row + 1, c = col + 1; r < BOARD_SIZE && c < BOARD_SIZE && board[r][c] == player; r++, c++) count++;
        if (count >= 5) return true;

        // 检查对角线（右上到左下）
        count = 1;
        for (int r = row - 1, c = col + 1; r >= 0 && c < BOARD_SIZE && board[r][c] == player; r--, c++) count++;
        for (int r = row + 1, c = col - 1; r < BOARD_SIZE && c >= 0 && board[r][c] == player; r++, c--) count++;
        if (count >= 5) return true;

        return false;
    }

    public int[][] getBoard1() {
        return board1;
    }

    public int[][] getBoard2() {
        return board2;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGame1Active() {
        return game1Active;
    }

    public boolean isGame2Active() {
        return game2Active;
    }

    public boolean isBothGamesWon() {
        return !game1Active && !game2Active;
    }
}
