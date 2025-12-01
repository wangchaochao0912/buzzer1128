package com.example.buzzer.controller;

import com.example.buzzer.service.GomokuService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
public class GomokuGameController {
    private final GomokuService gomokuService;

    public GomokuGameController(GomokuService gomokuService) {
        this.gomokuService = gomokuService;
    }

    @PostMapping("/start")
    public GameResponse startGame() {
        gomokuService.initGame();
        return new GameResponse(true, gomokuService.getBoard1(), gomokuService.getBoard2(), gomokuService.getCurrentPlayer(), gomokuService.isGame1Active(), gomokuService.isGame2Active(), false);
    }

    @PostMapping("/move")
    public GameResponse makeMove(@RequestBody MoveRequest moveRequest) {
        boolean validMove = gomokuService.makeMove(moveRequest.getBoardIndex(), moveRequest.getRow(), moveRequest.getCol());
        if (!validMove) {
            return new GameResponse(false, gomokuService.getBoard1(), gomokuService.getBoard2(), gomokuService.getCurrentPlayer(), gomokuService.isGame1Active(), gomokuService.isGame2Active(), false);
        }
        boolean bothGamesWon = gomokuService.isBothGamesWon();
        return new GameResponse(true, gomokuService.getBoard1(), gomokuService.getBoard2(), gomokuService.getCurrentPlayer(), gomokuService.isGame1Active(), gomokuService.isGame2Active(), bothGamesWon);
    }

    @PostMapping("/exit")
    public void exitGame() {
        // 这里可以添加退出游戏的逻辑
    }

    @PostMapping("/status")
    public GameResponse getGameStatus() {
        return new GameResponse(true, gomokuService.getBoard1(), gomokuService.getBoard2(), gomokuService.getCurrentPlayer(), gomokuService.isGame1Active(), gomokuService.isGame2Active(), false);
    }

    // DTO classes
    public static class MoveRequest {
        private int boardIndex;
        private int row;
        private int col;

        // Getters and setters
        public int getBoardIndex() { return boardIndex; }
        public void setBoardIndex(int boardIndex) { this.boardIndex = boardIndex; }
        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }
        public int getCol() { return col; }
        public void setCol(int col) { this.col = col; }
    }

    public static class GameResponse {
        private boolean success;
        private int[][] board1;
        private int[][] board2;
        private int currentPlayer;
        private boolean game1Active;
        private boolean game2Active;
        private boolean bothGamesWon;

        public GameResponse(boolean success, int[][] board1, int[][] board2, int currentPlayer, boolean game1Active, boolean game2Active, boolean bothGamesWon) {
            this.success = success;
            this.board1 = board1;
            this.board2 = board2;
            this.currentPlayer = currentPlayer;
            this.game1Active = game1Active;
            this.game2Active = game2Active;
            this.bothGamesWon = bothGamesWon;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public int[][] getBoard1() { return board1; }
        public int[][] getBoard2() { return board2; }
        public int getCurrentPlayer() { return currentPlayer; }
        public boolean isGame1Active() { return game1Active; }
        public boolean isGame2Active() { return game2Active; }
        public boolean isBothGamesWon() { return bothGamesWon; }
    }
}
