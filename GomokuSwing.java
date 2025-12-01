import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GomokuSwing extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 30;
    private static final int WIN_COUNT = 5;
    
    private String[][] board;
    private String currentPlayer;
    private boolean gameStarted;
    private boolean gameOver;
    private String winner;
    private int blackMoveCount;
    private int whiteMoveCount;
    
    private JPanel boardPanel;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton exitButton;
    
    public GomokuSwing() {
        board = new String[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = "black";
        gameStarted = false;
        gameOver = false;
        winner = null;
        blackMoveCount = 0;
        whiteMoveCount = 0;
        
        initUI();
        initBoard();
    }
    
    private void initUI() {
        setTitle("五子棋游戏");
        setSize(BOARD_SIZE * CELL_SIZE + 100, BOARD_SIZE * CELL_SIZE + 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // 棋盘面板
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
            }
        };
        boardPanel.setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameStarted && !gameOver) {
                    int x = e.getX();
                    int y = e.getY();
                    
                    int col = x / CELL_SIZE;
                    int row = y / CELL_SIZE;
                    
                    if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                        makeMove(row, col);
                    }
                }
            }
        });
        
        // 控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        
        startButton = new JButton("开始游戏");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
        
        exitButton = new JButton("退出游戏");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitGame();
            }
        });
        
        statusLabel = new JLabel("当前玩家：黑棋");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        controlPanel.add(startButton);
        controlPanel.add(exitButton);
        controlPanel.add(statusLabel);
        
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void initBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = "";
            }
        }
        boardPanel.repaint();
    }
    
    private void drawBoard(Graphics g) {
        g.setColor(new Color(222, 184, 135)); // 棋盘颜色
        g.fillRect(0, 0, BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
        
        g.setColor(Color.BLACK);
        for (int i = 0; i <= BOARD_SIZE; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
            g.drawLine(0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE, i * CELL_SIZE);
        }
    }
    
    private void drawPieces(Graphics g) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (!board[i][j].isEmpty()) {
                    int x = j * CELL_SIZE + CELL_SIZE / 2;
                    int y = i * CELL_SIZE + CELL_SIZE / 2;
                    int radius = CELL_SIZE / 2 - 2;
                    
                    if (board[i][j].equals("black")) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    
                    g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                    g.setColor(Color.BLACK);
                    g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
                }
            }
        }
    }
    
    private void startGame() {
        gameStarted = true;
        gameOver = false;
        winner = null;
        currentPlayer = "black";
        blackMoveCount = 0;
        whiteMoveCount = 0;
        initBoard();
        statusLabel.setText("当前玩家：黑棋");
        JOptionPane.showMessageDialog(this, "游戏开始！黑棋先行。");
    }
    
    private void exitGame() {
        int choice = JOptionPane.showConfirmDialog(this, "确定要退出游戏吗？", "退出游戏", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            gameStarted = false;
            gameOver = false;
            winner = null;
            currentPlayer = "black";
            blackMoveCount = 0;
            whiteMoveCount = 0;
            initBoard();
            statusLabel.setText("当前玩家：黑棋");
            JOptionPane.showMessageDialog(this, "游戏已退出。");
        }
    }
    
    private void makeMove(int row, int col) {
        if (!board[row][col].isEmpty()) {
            JOptionPane.showMessageDialog(this, "此位置已有棋子，请选择其他位置！");
            return;
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
        
        boardPanel.repaint();
        
        if (checkWin(row, col)) {
            gameOver = true;
            winner = currentPlayer;
            String winnerText = winner.equals("black") ? "黑棋" : "白棋";
            JOptionPane.showMessageDialog(this, winnerText + "获胜！");
            statusLabel.setText(winnerText + "获胜！");
        } else if (checkDraw()) {
            gameOver = true;
            JOptionPane.showMessageDialog(this, "平局！");
            statusLabel.setText("平局！");
        } else {
            switchPlayer();
            statusLabel.setText("当前玩家：" + (currentPlayer.equals("black") ? "黑棋" : "白棋"));
        }
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
            
            // 显示转换提示
            String fromText = fromColor.equals("black") ? "黑棋" : "白棋";
            String toText = toColor.equals("black") ? "黑棋" : "白棋";
            JOptionPane.showMessageDialog(this, "由于" + fromText + "已走3步，随机将一个" + fromText + "变成了" + toText + "！");
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GomokuSwing game = new GomokuSwing();
                game.setVisible(true);
            }
        });
    }
}
