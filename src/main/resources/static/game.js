// 猜单词游戏核心逻辑
class WordGuessGame {
    constructor() {
        // 游戏状态
        this.gameState = {
            currentWord: '',
            guessedLetters: [],
            maxAttempts: 6,
            remainingAttempts: 6,
            score: 0,
            isGameOver: false
        };
        
        // 单词库
        this.words = [
            'apple', 'banana', 'cherry', 'date', 'elderberry',
            'fig', 'grape', 'honeydew', 'kiwi', 'lemon',
            'mango', 'nectarine', 'orange', 'peach', 'pear',
            'quince', 'raspberry', 'strawberry', 'tangerine',
            'watermelon', 'avocado', 'blueberry', 'coconut',
            'dragonfruit', 'grapefruit', 'lime', 'papaya',
            'pineapple', 'pomegranate', 'blackberry', 'boysenberry',
            'cranberry', 'gooseberry', 'mulberry', 'passionfruit'
        ];
        
        // DOM元素
        this.elements = {
            // 设置界面
            setupScreen: document.getElementById('setupScreen'),
            maxAttemptsInput: document.getElementById('maxAttempts'),
            attemptsError: document.getElementById('attemptsError'),
            startGameBtn: document.getElementById('startGameBtn'),
            
            // 游戏界面
            gameScreen: document.getElementById('gameScreen'),
            remainingAttempts: document.getElementById('remainingAttempts'),
            score: document.getElementById('score'),
            wordLetters: document.getElementById('wordLetters'),
            keyboardLetters: document.getElementById('keyboardLetters'),
            newGameBtn: document.getElementById('newGameBtn'),
            quitGameBtn: document.getElementById('quitGameBtn'),
            
            // 结果界面
            resultScreen: document.getElementById('resultScreen'),
            resultTitle: document.getElementById('resultTitle'),
            resultMessage: document.getElementById('resultMessage'),
            correctWord: document.getElementById('correctWord'),
            playAgainBtn: document.getElementById('playAgainBtn'),
            backToSetupBtn: document.getElementById('backToSetupBtn')
        };
        
        // 初始化游戏
        this.init();
    }
    
    // 初始化游戏
    init() {
        this.bindEvents();
        this.generateKeyboard();
    }
    
    // 绑定事件
    bindEvents() {
        // 设置界面事件
        this.elements.startGameBtn.addEventListener('click', () => this.startGame());
        
        // 游戏界面事件
        this.elements.newGameBtn.addEventListener('click', () => this.newGame());
        this.elements.quitGameBtn.addEventListener('click', () => this.quitGame());
        
        // 结果界面事件
        this.elements.playAgainBtn.addEventListener('click', () => this.newGame());
        this.elements.backToSetupBtn.addEventListener('click', () => this.showSetupScreen());
        
        // 键盘事件
        document.addEventListener('keydown', (e) => this.handleKeyPress(e));
    }
    
    // 生成键盘
    generateKeyboard() {
        const letters = 'abcdefghijklmnopqrstuvwxyz';
        this.elements.keyboardLetters.innerHTML = '';
        
        for (let letter of letters) {
            const key = document.createElement('div');
            key.className = 'key';
            key.textContent = letter;
            key.dataset.letter = letter;
            key.addEventListener('click', () => this.guessLetter(letter));
            this.elements.keyboardLetters.appendChild(key);
        }
    }
    
    // 开始游戏
    startGame() {
        // 获取用户输入的猜测次数
        const maxAttempts = parseInt(this.elements.maxAttemptsInput.value);
        
        // 随机生成一个单词
        const randomIndex = Math.floor(Math.random() * this.words.length);
        const word = this.words[randomIndex];
        
        // 检查猜测次数是否小于单词长度
        if (maxAttempts < word.length) {
            this.elements.attemptsError.textContent = `猜测次数不能小于单词长度(${word.length}个字母)`;
            this.elements.attemptsError.classList.add('show');
            return;
        }
        
        // 隐藏错误提示
        this.elements.attemptsError.classList.remove('show');
        
        // 初始化游戏状态
        this.gameState = {
            currentWord: word,
            guessedLetters: [],
            maxAttempts: maxAttempts,
            remainingAttempts: maxAttempts,
            score: this.gameState.score,
            isGameOver: false
        };
        
        // 更新UI
        this.updateUI();
        
        // 显示游戏界面
        this.showGameScreen();
    }
    
    // 新游戏
    newGame() {
        // 随机生成一个新单词
        const randomIndex = Math.floor(Math.random() * this.words.length);
        const word = this.words[randomIndex];
        
        // 检查猜测次数是否小于单词长度
        if (this.gameState.maxAttempts < word.length) {
            // 增加猜测次数以满足单词长度要求
            const newMaxAttempts = Math.max(this.gameState.maxAttempts, word.length);
            this.gameState.maxAttempts = newMaxAttempts;
            this.gameState.remainingAttempts = newMaxAttempts;
            this.elements.maxAttemptsInput.value = newMaxAttempts;
        }
        
        // 重置游戏状态
        this.gameState = {
            currentWord: word,
            guessedLetters: [],
            maxAttempts: this.gameState.maxAttempts,
            remainingAttempts: this.gameState.maxAttempts,
            score: this.gameState.score,
            isGameOver: false
        };
        
        // 重置键盘
        this.resetKeyboard();
        
        // 更新UI
        this.updateUI();
        
        // 显示游戏界面
        this.showGameScreen();
    }
    
    // 退出游戏
    quitGame() {
        if (confirm('确定要退出游戏吗？')) {
            this.showSetupScreen();
        }
    }
    
    // 猜测字母
    guessLetter(letter) {
        // 如果游戏已经结束，返回
        if (this.gameState.isGameOver) {
            return;
        }
        
        // 如果字母已经被猜测过，返回
        if (this.gameState.guessedLetters.includes(letter)) {
            return;
        }
        
        // 添加到已猜测字母列表
        this.gameState.guessedLetters.push(letter);
        
        // 更新键盘状态
        this.updateKeyboard(letter);
        
        // 检查字母是否在单词中
        if (!this.gameState.currentWord.includes(letter)) {
            // 减少剩余次数
            this.gameState.remainingAttempts--;
            
            // 检查是否游戏结束（失败）
            if (this.gameState.remainingAttempts === 0) {
                this.endGame(false);
            }
        } else {
            // 检查是否游戏结束（胜利）
            if (this.checkWin()) {
                this.endGame(true);
            }
        }
        
        // 更新UI
        this.updateUI();
    }
    
    // 处理键盘按键
    handleKeyPress(e) {
        // 如果游戏已经结束，返回
        if (this.gameState.isGameOver) {
            return;
        }
        
        // 获取按下的字母
        const letter = e.key.toLowerCase();
        
        // 检查是否是字母
        if (/^[a-z]$/.test(letter)) {
            this.guessLetter(letter);
        }
    }
    
    // 检查是否胜利
    checkWin() {
        for (let letter of this.gameState.currentWord) {
            if (!this.gameState.guessedLetters.includes(letter)) {
                return false;
            }
        }
        return true;
    }
    
    // 结束游戏
    endGame(isWin) {
        this.gameState.isGameOver = true;
        
        // 更新分数
        if (isWin) {
            const bonus = this.gameState.remainingAttempts * 10;
            this.gameState.score += 100 + bonus;
            this.showResultScreen(true, `恭喜你猜对了！获得 ${100 + bonus} 分。`);
        } else {
            this.showResultScreen(false, '很遗憾，你没有猜对。');
        }
    }
    
    // 更新UI
    updateUI() {
        // 更新剩余次数
        this.elements.remainingAttempts.textContent = this.gameState.remainingAttempts;
        
        // 更新分数
        this.elements.score.textContent = this.gameState.score;
        
        // 更新单词显示
        this.updateWordDisplay();
    }
    
    // 更新单词显示
    updateWordDisplay() {
        this.elements.wordLetters.innerHTML = '';
        
        for (let letter of this.gameState.currentWord) {
            const letterBox = document.createElement('div');
            letterBox.className = 'letter-box';
            
            if (this.gameState.guessedLetters.includes(letter)) {
                letterBox.textContent = letter;
            }
            
            this.elements.wordLetters.appendChild(letterBox);
        }
    }
    
    // 更新键盘状态
    updateKeyboard(letter) {
        const key = this.elements.keyboardLetters.querySelector(`[data-letter="${letter}"]`);
        
        if (key) {
            key.classList.add('used');
            
            if (this.gameState.currentWord.includes(letter)) {
                key.classList.add('correct');
            } else {
                key.classList.add('wrong');
            }
        }
    }
    
    // 重置键盘
    resetKeyboard() {
        const keys = this.elements.keyboardLetters.querySelectorAll('.key');
        
        for (let key of keys) {
            key.classList.remove('used', 'correct', 'wrong');
        }
    }
    
    // 显示设置界面
    showSetupScreen() {
        // 重置游戏状态
        this.gameState = {
            currentWord: '',
            guessedLetters: [],
            maxAttempts: 6,
            remainingAttempts: 6,
            score: this.gameState.score,
            isGameOver: false
        };
        
        // 重置键盘
        this.resetKeyboard();
        
        // 重置UI显示
        this.elements.remainingAttempts.textContent = '6';
        this.elements.score.textContent = this.gameState.score;
        this.elements.wordLetters.innerHTML = '';
        this.elements.maxAttemptsInput.value = '6';
        this.elements.attemptsError.classList.remove('show');
        
        // 显示设置界面
        this.elements.setupScreen.classList.add('active');
        this.elements.gameScreen.classList.remove('active');
        this.elements.resultScreen.classList.remove('active');
    }
    
    // 显示游戏界面
    showGameScreen() {
        this.elements.setupScreen.classList.remove('active');
        this.elements.gameScreen.classList.add('active');
        this.elements.resultScreen.classList.remove('active');
    }
    
    // 显示结果界面
    showResultScreen(isWin, message) {
        this.elements.setupScreen.classList.remove('active');
        this.elements.gameScreen.classList.remove('active');
        this.elements.resultScreen.classList.add('active');
        
        // 更新结果界面内容
        this.elements.resultTitle.textContent = isWin ? '🎉 胜利！' : '😢 失败！';
        this.elements.resultTitle.className = `result-title ${isWin ? 'win' : 'lose'}`;
        this.elements.resultMessage.textContent = message;
        this.elements.correctWord.textContent = `正确答案：${this.gameState.currentWord}`;
    }
}

// 页面加载完成后初始化游戏
document.addEventListener('DOMContentLoaded', () => {
    new WordGuessGame();
});