import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GomokuDoubleBoard extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 25;
    private static final int WIN_COUNT = 5;
    
    // 第一个棋盘的状态
    private String[][] board1;
    private String currentPlayer1;
    private boolean gameStarted1;
    private boolean gameOver1;
    private String winner1;
    private int blackMoveCount1;
    private int whiteMoveCount1;
    
    // 第二个棋盘的状态
    private String[][] board2;
    private String currentPlayer2;
    private boolean gameStarted2;
    private boolean gameOver2;
    private String winner2;
    private int blackMoveCount2;
    private int whiteMoveCount2;
    
    // UI组件
    private JPanel boardPanel1;
    private JPanel boardPanel2;
    private JLabel statusLabel1;
    private JLabel statusLabel2;
    private JLabel overallStatusLabel;
    private JButton startButton;
    private JButton exitButton;
    
    public GomokuDoubleBoard() {
        // 初始化第一个棋盘
        board1 = new String[BOARD_SIZE][BOARD_SIZE];
        currentPlayer1 = "black";
        gameStarted1 = false;
        gameOver1 = false;
        winner1 = null;
        blackMoveCount1 = 0;
        whiteMoveCount1 = 0;
        
        // 初始化第二个棋盘
        board2 = new String[BOARD_SIZE][BOARD_SIZE];
        currentPlayer2 = "black";
        gameStarted2 = false;
        gameOver2 = false;
        winner2 = null;
        blackMoveCount2 = 0;
        whiteMoveCount2 = 0;
        
        initUI();
        initBoards();
    }
    
    private void initUI() {
        setTitle("双棋盘五子棋游戏");
        setSize(BOARD_SIZE * CELL_SIZE * 2 + 200, BOARD_SIZE * CELL_SIZE + 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // 棋盘容器面板
        JPanel boardsContainer = new JPanel();
        boardsContainer.setLayout(new GridLayout(1, 2, 20, 0));
        
        // 第一个棋盘面板
        JPanel board1Container = new JPanel();
        board1Container.setLayout(new BorderLayout());
        boardPanel1 = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g, board1);
            }
        };
        boardPanel1.setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
        boardPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameStarted1 && !gameOver1) {
                    int x = e.getX();
                    int y = e.getY();
                    
                    int col = x / CELL_SIZE;
                    int row = y / CELL_SIZE;
                    
                    if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                        makeMove(row, col, 1);
                    }
                }
            }
        });
        
        statusLabel1 = new JLabel("棋盘1：当前玩家 - 黑棋");
        statusLabel1.setFont(new Font("Arial", Font.PLAIN, 14));
        
        board1Container.add(new JLabel("棋盘 1"), BorderLayout.NORTH);
        board1Container.add(boardPanel1, BorderLayout.CENTER);
        board1Container.add(statusLabel1, BorderLayout.SOUTH);
        
        // 第二个棋盘面板
        JPanel board2Container = new JPanel();
        board2Container.setLayout(new BorderLayout());
        boardPanel2 = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g, board2);
            }
        };
        boardPanel2.setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
        boardPanel2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameStarted2 && !gameOver2) {
                    int x = e.getX();
                    int y = e.getY();
                    
                    int col = x / CELL_SIZE;
                    int row = y / CELL_SIZE;
                    
                    if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                        makeMove(row, col, 2);
                    }
                }
            }
        });
        
        statusLabel2 = new JLabel("棋盘2：当前玩家 - 黑棋");
        statusLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
        
        board2Container.add(new JLabel("棋盘 2"), BorderLayout.NORTH);
        board2Container.add(boardPanel2, BorderLayout.CENTER);
        board2Container.add(statusLabel2, BorderLayout.SOUTH);
        
        boardsContainer.add(board1Container);
        boardsContainer.add(board2Container);
        
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
        
        overallStatusLabel = new JLabel("整体状态：等待开始游戏");
        overallStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        overallStatusLabel.setForeground(Color.BLUE);
        
        controlPanel.add(startButton);
        controlPanel.add(exitButton);
        controlPanel.add(overallStatusLabel);
        
        mainPanel.add(boardsContainer, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void initBoards() {
        // 初始化第一个棋盘
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board1[i][j] = "";
            }
        }
        
        // 初始化第二个棋盘
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board2[i][j] = "";
            }
        }
        
        boardPanel1.repaint();
        boardPanel2.repaint();
    }
    
    private void drawBoard(Graphics g, String[][] board) {
        g.setColor(new Color(222, 184, 135)); // 棋盘颜色
        g.fillRect(0, 0, BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
        
        g.setColor(Color.BLACK);
        for (int i = 0; i <= BOARD_SIZE; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
            g.drawLine(0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE, i * CELL_SIZE);
        }
        
        // 绘制棋子
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
        // 重置第一个棋盘
        gameStarted1 = true;
        gameOver1 = false;
        winner1 = null;
        currentPlayer1 = "black";
        blackMoveCount1 = 0;
        whiteMoveCount1 = 0;
        
        // 重置第二个棋盘
        gameStarted2 = true;
        gameOver2 = false;
        winner2 = null;
        currentPlayer2 = "black";
        blackMoveCount2 = 0;
        whiteMoveCount2 = 0;
        
        initBoards();
        
        // 更新状态标签
        statusLabel1.setText("棋盘1：当前玩家 - 黑棋");
        statusLabel2.setText("棋盘2：当前玩家 - 黑棋");
        overallStatusLabel.setText("整体状态：游戏进行中");
        
        JOptionPane.showMessageDialog(this, "双棋盘游戏开始！两个棋盘都需要获胜才能赢得整体胜利。");
    }
    
    private void exitGame() {
        int choice = JOptionPane.showConfirmDialog(this, "确定要退出游戏吗？", "退出游戏", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            // 重置两个棋盘
            gameStarted1 = false;
            gameOver1 = false;
            winner1 = null;
            currentPlayer1 = "black";
            blackMoveCount1 = 0;
            whiteMoveCount1 = 0;
            
            gameStarted2 = false;
            gameOver2 = false;
            winner2 = null;
            currentPlayer2 = "black";
            blackMoveCount2 = 0;
            whiteMoveCount2 = 0;
            
            initBoards();
            
            // 更新状态标签
            statusLabel1.setText("棋盘1：当前玩家 - 黑棋");
            statusLabel2.setText("棋盘2：当前玩家 - 黑棋");
            overallStatusLabel.setText("整体状态：等待开始游戏");
            
            JOptionPane.showMessageDialog(this, "游戏已退出。");
        }
    }
    
    private void makeMove(int row, int col, int boardNum) {
        if (boardNum == 1) {
            // 第一个棋盘落子
            if (!board1[row][col].isEmpty()) {
                JOptionPane.showMessageDialog(this, "棋盘1的此位置已有棋子，请选择其他位置！");
                return;
            }
            
            board1[row][col] = currentPlayer1;
            
            // 增加当前玩家的步数
            if (currentPlayer1.equals("black")) {
                blackMoveCount1++;
                // 每3步，随机将一个黑棋变成白棋
                if (blackMoveCount1 % 3 == 0) {
                    convertRandomPiece("black", "white", board1, 1);
                }
            } else {
                whiteMoveCount1++;
                // 每3步，随机将一个白棋变成黑棋
                if (whiteMoveCount1 % 3 == 0) {
                    convertRandomPiece("white", "black", board1, 1);
                }
            }
            
            boardPanel1.repaint();
            
            if (checkWin(row, col, board1, currentPlayer1)) {
                gameOver1 = true;
                winner1 = currentPlayer1;
                String winnerText = winner1.equals("black") ? "黑棋" : "白棋";
                JOptionPane.showMessageDialog(this, "棋盘1：" + winnerText + "获胜！");
                statusLabel1.setText("棋盘1：" + winnerText + "获胜！");
                
                // 检查是否两个棋盘都获胜
                checkOverallWin();
            } else if (checkDraw(board1)) {
                gameOver1 = true;
                JOptionPane.showMessageDialog(this, "棋盘1：平局！");
                statusLabel1.setText("棋盘1：平局！");
            } else {
                switchPlayer(1);
                statusLabel1.setText("棋盘1：当前玩家 - " + (currentPlayer1.equals("black") ? "黑棋" : "白棋"));
            }
        } else if (boardNum == 2) {
            // 第二个棋盘落子
            if (!board2[row][col].isEmpty()) {
                JOptionPane.showMessageDialog(this, "棋盘2的此位置已有棋子，请选择其他位置！");
                return;
            }
            
            board2[row][col] = currentPlayer2;
            
            // 增加当前玩家的步数
            if (currentPlayer2.equals("black")) {
                blackMoveCount2++;
                // 每3步，随机将一个黑棋变成白棋
                if (blackMoveCount2 % 3 == 0) {
                    convertRandomPiece("black", "white", board2, 2);
                }
            } else {
                whiteMoveCount2++;
                // 每3步，随机将一个白棋变成黑棋
                if (whiteMoveCount2 % 3 == 0) {
                    convertRandomPiece("white", "black", board2, 2);
                }
            }
            
            boardPanel2.repaint();
            
            if (checkWin(row, col, board2, currentPlayer2)) {
                gameOver2 = true;
                winner2 = currentPlayer2;
                String winnerText = winner2.equals("black") ? "黑棋" : "白棋";
                JOptionPane.showMessageDialog(this, "棋盘2：" + winnerText + "获胜！");
                statusLabel2.setText("棋盘2：" + winnerText + "获胜！");
                
                // 检查是否两个棋盘都获胜
                checkOverallWin();
            } else if (checkDraw(board2)) {
                gameOver2 = true;
                JOptionPane.showMessageDialog(this, "棋盘2：平局！");
                statusLabel2.setText("棋盘2：平局！");
            } else {
                switchPlayer(2);
                statusLabel2.setText("棋盘2：当前玩家 - " + (currentPlayer2.equals("black") ? "黑棋" : "白棋"));
            }
        }
    }
    
    private void switchPlayer(int boardNum) {
        if (boardNum == 1) {
            currentPlayer1 = currentPlayer1.equals("black") ? "white" : "black";
        } else if (boardNum == 2) {
            currentPlayer2 = currentPlayer2.equals("black") ? "white" : "black";
        }
    }
    
    private boolean checkWin(int row, int col, String[][] board, String player) {
        // 检查横向
        int count = 1;
        for (int j = col + 1; j < BOARD_SIZE && board[row][j].equals(player); j++) {
            count++;
        }
        for (int j = col - 1; j >= 0 && board[row][j].equals(player); j--) {
            count++;
        }
        if (count >= WIN_COUNT) {
            return true;
        }
        
        // 检查纵向
        count = 1;
        for (int i = row + 1; i < BOARD_SIZE && board[i][col].equals(player); i++) {
            count++;
        }
        for (int i = row - 1; i >= 0 && board[i][col].equals(player); i--) {
            count++;
        }
        if (count >= WIN_COUNT) {
            return true;
        }
        
        // 检查主对角线 (左上到右下)
        count = 1;
        for (int i = row + 1, j = col + 1; i < BOARD_SIZE && j < BOARD_SIZE && board[i][j].equals(player); i++, j++) {
            count++;
        }
        for (int i = row - 1, j = col - 1; i >= 0 && j >= 0 && board[i][j].equals(player); i--, j--) {
            count++;
        }
        if (count >= WIN_COUNT) {
            return true;
        }
        
        // 检查副对角线 (右上到左下)
        count = 1;
        for (int i = row + 1, j = col - 1; i < BOARD_SIZE && j >= 0 && board[i][j].equals(player); i++, j--) {
            count++;
        }
        for (int i = row - 1, j = col + 1; i >= 0 && j < BOARD_SIZE && board[i][j].equals(player); i--, j++) {
            count++;
        }
        if (count >= WIN_COUNT) {
            return true;
        }
        
        return false;
    }
    
    private boolean checkDraw(String[][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void convertRandomPiece(String fromColor, String toColor, String[][] board, int boardNum) {
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
            JOptionPane.showMessageDialog(this, "棋盘" + boardNum + "：由于" + fromText + "已走3步，随机将一个" + fromText + "变成了" + toText + "！");
            
            // 重绘棋盘
            if (boardNum == 1) {
                boardPanel1.repaint();
            } else if (boardNum == 2) {
                boardPanel2.repaint();
            }
        }
    }
    
    private void checkOverallWin() {
        if (gameOver1 && gameOver2 && winner1 != null && winner2 != null) {
            // 两个棋盘都获胜
            String overallWinner = "";
            if (winner1.equals(winner2)) {
                overallWinner = winner1.equals("black") ? "黑棋" : "白棋";
            } else {
                // 如果两个棋盘获胜者不同，视为平局
                JOptionPane.showMessageDialog(this, "两个棋盘获胜者不同，整体游戏平局！");
                overallStatusLabel.setText("整体状态：平局！");
                return;
            }
            
            JOptionPane.showMessageDialog(this, "恭喜！" + overallWinner + "在两个棋盘都获胜了，整体游戏胜利！");
            overallStatusLabel.setText("整体状态：" + overallWinner + "获胜！");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GomokuDoubleBoard game = new GomokuDoubleBoard();
                game.setVisible(true);
            }
        });
    }
}