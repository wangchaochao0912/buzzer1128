package com.example.buzzer.controller;

import com.example.buzzer.service.GomokuGame;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GomokuGameController {
    
    private GomokuGame game = new GomokuGame();
    
    @PostMapping("/start")
    public Map<String, Object> startGame() {
        game.startGame();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "游戏开始！黑棋先行。");
        response.put("currentPlayer", game.getCurrentPlayer());
        response.put("boardSize", game.getBoardSize());
        return response;
    }
    
    @PostMapping("/exit")
    public Map<String, Object> exitGame() {
        game.exitGame();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "游戏已退出。");
        return response;
    }
    
    @PostMapping("/move")
    public Map<String, Object> makeMove(@RequestBody Map<String, Integer> request) {
        int row = request.get("row");
        int col = request.get("col");
        
        boolean success = game.makeMove(row, col);
        Map<String, Object> response = new HashMap<>();
        
        if (success) {
            response.put("success", true);
            response.put("row", row);
            response.put("col", col);
            response.put("player", game.getCurrentPlayer().equals("black") ? "white" : "black"); // 返回刚落子的玩家
            
            if (game.isGameOver()) {
                if (game.getWinner() != null) {
                    response.put("gameOver", true);
                    response.put("winner", game.getWinner());
                    response.put("message", game.getWinner().equals("black") ? "黑棋获胜！" : "白棋获胜！");
                } else {
                    response.put("gameOver", true);
                    response.put("winner", null);
                    response.put("message", "平局！");
                }
            } else {
                response.put("gameOver", false);
                response.put("currentPlayer", game.getCurrentPlayer());
            }
        } else {
            response.put("success", false);
            if (!game.isGameStarted() || game.isGameOver()) {
                response.put("message", "游戏未开始或已结束，请先点击\"开始游戏\"按钮！");
            } else {
                response.put("message", "此位置已有棋子或位置无效，请选择其他位置！");
            }
        }
        
        return response;
    }
    
    @GetMapping("/status")
    public Map<String, Object> getGameStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("gameStarted", game.isGameStarted());
        response.put("gameOver", game.isGameOver());
        response.put("currentPlayer", game.getCurrentPlayer());
        response.put("winner", game.getWinner());
        response.put("boardSize", game.getBoardSize());
        return response;
    }
    
    @GetMapping("/board")
    public Map<String, Object> getBoard() {
        Map<String, Object> response = new HashMap<>();
        response.put("board", game.getBoard());
        return response;
    }
}
