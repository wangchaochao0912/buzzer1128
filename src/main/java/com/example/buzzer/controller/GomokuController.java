package com.example.buzzer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GomokuController {

    // 棋盘1状态
    private String[][] board1 = new String[15][15];
    private boolean gameStarted1 = false;
    private String currentPlayer1 = "black";
    private boolean gameOver1 = false;
    private int blackMoves1 = 0;
    private int whiteMoves1 = 0;
    private String lastSpecialMoveMessage1 = "";
    
    // 棋盘2状态
    private String[][] board2 = new String[15][15];
    private boolean gameStarted2 = false;
    private String currentPlayer2 = "black";
    private boolean gameOver2 = false;
    private int blackMoves2 = 0;
    private int whiteMoves2 = 0;
    private String lastSpecialMoveMessage2 = "";
    
    private java.util.Random random = new java.util.Random();
    private boolean totalVictory = false; // 总胜利标志

    @GetMapping("/")
    public String index(Model model) {
        // 添加两个棋盘的状态到模型
        model.addAttribute("board1", board1);
        model.addAttribute("gameStarted1", gameStarted1);
        model.addAttribute("currentPlayer1", currentPlayer1);
        model.addAttribute("gameOver1", gameOver1);
        model.addAttribute("blackMoves1", blackMoves1);
        model.addAttribute("whiteMoves1", whiteMoves1);
        model.addAttribute("lastSpecialMoveMessage1", lastSpecialMoveMessage1);
        
        model.addAttribute("board2", board2);
        model.addAttribute("gameStarted2", gameStarted2);
        model.addAttribute("currentPlayer2", currentPlayer2);
        model.addAttribute("gameOver2", gameOver2);
        model.addAttribute("blackMoves2", blackMoves2);
        model.addAttribute("whiteMoves2", whiteMoves2);
        model.addAttribute("lastSpecialMoveMessage2", lastSpecialMoveMessage2);
        
        model.addAttribute("totalVictory", totalVictory);
        
        return "gomoku";
    }
    
    @GetMapping("/gomoku")
    public String gomoku(Model model) {
        return index(model);
    }

    @PostMapping("/start-game")
    public String startGame() {
        // 初始化两个棋盘
        board1 = new String[15][15];
        gameStarted1 = true;
        currentPlayer1 = "black";
        gameOver1 = false;
        blackMoves1 = 0;
        whiteMoves1 = 0;
        lastSpecialMoveMessage1 = "";
        
        board2 = new String[15][15];
        gameStarted2 = true;
        currentPlayer2 = "black";
        gameOver2 = false;
        blackMoves2 = 0;
        whiteMoves2 = 0;
        lastSpecialMoveMessage2 = "";
        
        totalVictory = false;
        
        return "redirect:/";
    }

    @PostMapping("/place-stone")
    public String placeStone(@RequestParam int row, @RequestParam int col, @RequestParam int boardNum) {
        // 根据棋盘编号选择对应的棋盘状态
        if (boardNum == 1) {
            if (!gameStarted1 || gameOver1 || board1[row][col] != null || totalVictory) {
                return "redirect:/";
            }

            // 放置棋子
            board1[row][col] = currentPlayer1;

            // 更新步数计数
            if (currentPlayer1.equals("black")) {
                blackMoves1++;
                // 检查是否需要变色
                if (blackMoves1 % 3 == 0) {
                    lastSpecialMoveMessage1 = convertRandomOpponentPiece(board1, "white", "black");
                } else {
                    lastSpecialMoveMessage1 = "";
                }
            } else {
                whiteMoves1++;
                // 检查是否需要变色
                if (whiteMoves1 % 3 == 0) {
                    lastSpecialMoveMessage1 = convertRandomOpponentPiece(board1, "black", "white");
                } else {
                    lastSpecialMoveMessage1 = "";
                }
            }

            // 检查胜负
            if (checkWin(board1, row, col)) {
                gameOver1 = true;
                // 检查是否双盘都胜利
                checkTotalVictory();
            } else {
                // 切换玩家
                currentPlayer1 = currentPlayer1.equals("black") ? "white" : "black";
            }
        } else if (boardNum == 2) {
            if (!gameStarted2 || gameOver2 || board2[row][col] != null || totalVictory) {
                return "redirect:/";
            }

            // 放置棋子
            board2[row][col] = currentPlayer2;

            // 更新步数计数
            if (currentPlayer2.equals("black")) {
                blackMoves2++;
                // 检查是否需要变色
                if (blackMoves2 % 3 == 0) {
                    lastSpecialMoveMessage2 = convertRandomOpponentPiece(board2, "white", "black");
                } else {
                    lastSpecialMoveMessage2 = "";
                }
            } else {
                whiteMoves2++;
                // 检查是否需要变色
                if (whiteMoves2 % 3 == 0) {
                    lastSpecialMoveMessage2 = convertRandomOpponentPiece(board2, "black", "white");
                } else {
                    lastSpecialMoveMessage2 = "";
                }
            }

            // 检查胜负
            if (checkWin(board2, row, col)) {
                gameOver2 = true;
                // 检查是否双盘都胜利
                checkTotalVictory();
            } else {
                // 切换玩家
                currentPlayer2 = currentPlayer2.equals("black") ? "white" : "black";
            }
        }

        return "redirect:/";
    }

    @PostMapping("/exit-game")
    public String exitGame() {
        // 重置两个棋盘
        gameStarted1 = false;
        gameOver1 = false;
        board1 = new String[15][15];
        blackMoves1 = 0;
        whiteMoves1 = 0;
        lastSpecialMoveMessage1 = "";
        
        gameStarted2 = false;
        gameOver2 = false;
        board2 = new String[15][15];
        blackMoves2 = 0;
        whiteMoves2 = 0;
        lastSpecialMoveMessage2 = "";
        
        totalVictory = false;
        
        return "redirect:/";
    }

    private boolean checkWin(String[][] board, int row, int col) {
        String player = board[row][col];
        
        // 检查水平方向
        int count = 1;
        for (int c = col - 1; c >= 0 && board[row][c] != null && board[row][c].equals(player); c--) {
            count++;
        }
        for (int c = col + 1; c < 15 && board[row][c] != null && board[row][c].equals(player); c++) {
            count++;
        }
        if (count >= 5) return true;

        // 检查垂直方向
        count = 1;
        for (int r = row - 1; r >= 0 && board[r][col] != null && board[r][col].equals(player); r--) {
            count++;
        }
        for (int r = row + 1; r < 15 && board[r][col] != null && board[r][col].equals(player); r++) {
            count++;
        }
        if (count >= 5) return true;

        // 检查对角线方向（左上到右下）
        count = 1;
        for (int r = row - 1, c = col - 1; r >= 0 && c >= 0 && board[r][c] != null && board[r][c].equals(player); r--, c--) {
            count++;
        }
        for (int r = row + 1, c = col + 1; r < 15 && c < 15 && board[r][c] != null && board[r][c].equals(player); r++, c++) {
            count++;
        }
        if (count >= 5) return true;

        // 检查对角线方向（右上到左下）
        count = 1;
        for (int r = row - 1, c = col + 1; r >= 0 && c < 15 && board[r][c] != null && board[r][c].equals(player); r--, c++) {
            count++;
        }
        for (int r = row + 1, c = col - 1; r < 15 && c >= 0 && board[r][c] != null && board[r][c].equals(player); r++, c--) {
            count++;
        }
        if (count >= 5) return true;

        return false;
    }
    
    // 检查是否双盘都胜利
    private void checkTotalVictory() {
        if (gameOver1 && gameOver2) {
            totalVictory = true;
        }
    }
    
    private String convertRandomOpponentPiece(String[][] board, String opponentColor, String myColor) {
        // 收集对手的所有棋子位置
        java.util.List<int[]> opponentPieces = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j] != null && board[i][j].equals(opponentColor)) {
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
            return "特殊规则触发：位置 (" + (row + 1) + ", " + (col + 1) + ") 的" + 
                   (opponentColor.equals("black") ? "黑" : "白") + "子变为" + 
                   (myColor.equals("black") ? "黑" : "白") + "子！";
        }
        return "";
    }
}