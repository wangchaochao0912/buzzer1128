// 单词库 - 包含不同长度的常用英文单词
const wordLibrary = [
    'apple', 'banana', 'cherry', 'date', 'elderberry',
    'fig', 'grape', 'honeydew', 'kiwi', 'lemon',
    'mango', 'nectarine', 'orange', 'peach', 'pear',
    'pineapple', 'quince', 'raspberry', 'strawberry', 'tangerine',
    'computer', 'keyboard', 'monitor', 'mouse', 'printer',
    'javascript', 'python', 'java', 'html', 'css',
    'water', 'fire', 'earth', 'air', 'wind',
    'sun', 'moon', 'star', 'cloud', 'rain',
    'book', 'pen', 'paper', 'pencil', 'notebook',
    'car', 'bike', 'train', 'plane', 'ship'
];

// 游戏状态
let gameState = {
    targetWord: '',
    remainingGuesses: 0,
    maxGuesses: 0,
    guessedLetters: [],
    correctLetters: [],
    isGameOver: false
};

// DOM 元素
const setupScreen = document.getElementById('setup-screen');
const gameScreen = document.getElementById('game-screen');
const resultScreen = document.getElementById('result-screen');
const guessCountInput = document.getElementById('guess-count');
const startGameBtn = document.getElementById('start-game');
const setupError = document.getElementById('setup-error');
const remainingCount = document.getElementById('remaining-count');
const guessedList = document.getElementById('guessed-list');
const wordDisplay = document.getElementById('word-display');
const letterInput = document.getElementById('letter-input');
const submitGuessBtn = document.getElementById('submit-guess');
const gameMessage = document.getElementById('game-message');
const restartGameBtn = document.getElementById('restart-game');
const resultTitle = document.getElementById('result-title');
const resultDetail = document.getElementById('result-detail');
const playAgainBtn = document.getElementById('play-again');

// 初始化事件监听
function initEventListeners() {
    startGameBtn.addEventListener('click', startGame);
    submitGuessBtn.addEventListener('click', submitGuess);
    letterInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            submitGuess();
        }
    });
    restartGameBtn.addEventListener('click', resetGame);
    playAgainBtn.addEventListener('click', resetGame);
}

// 开始游戏
function startGame() {
    const guessCount = parseInt(guessCountInput.value);
    
    // 验证输入
    if (isNaN(guessCount) || guessCount < 1) {
        setupError.textContent = '请输入有效的猜测次数（至少1次）';
        return;
    }
    
    // 随机选择单词
    const randomWord = wordLibrary[Math.floor(Math.random() * wordLibrary.length)];
    
    // 检查猜测次数是否小于单词长度
    if (guessCount < randomWord.length) {
        setupError.textContent = `猜测次数不能小于单词长度（该单词有${randomWord.length}个字母）`;
        return;
    }
    
    // 初始化游戏状态
    gameState = {
        targetWord: randomWord,
        remainingGuesses: guessCount,
        maxGuesses: guessCount,
        guessedLetters: [],
        correctLetters: [],
        isGameOver: false
    };
    
    // 切换到游戏界面
    setupScreen.classList.remove('active');
    gameScreen.classList.add('active');
    
    // 更新界面显示
    updateGameDisplay();
    clearMessage();
    letterInput.focus();
}

// 更新游戏界面显示
function updateGameDisplay() {
    // 更新剩余猜测次数
    remainingCount.textContent = gameState.remainingGuesses;
    
    // 更新已猜字母
    guessedList.innerHTML = gameState.guessedLetters
        .map(letter => `<span>${letter}</span>`)
        .join('');
    
    // 更新单词显示
    wordDisplay.innerHTML = '';
    for (const letter of gameState.targetWord) {
        const letterBox = document.createElement('div');
        letterBox.className = 'letter-box';
        letterBox.textContent = gameState.correctLetters.includes(letter) ? letter : '';
        wordDisplay.appendChild(letterBox);
    }
}

// 提交猜测
function submitGuess() {
    if (gameState.isGameOver) return;
    
    const input = letterInput.value.trim().toLowerCase();
    
    // 验证输入
    if (!input || input.length !== 1 || !/^[a-z]$/.test(input)) {
        showMessage('请输入有效的单个字母', 'error');
        letterInput.value = '';
        letterInput.focus();
        return;
    }
    
    const letter = input;
    
    // 检查是否已经猜过该字母
    if (gameState.guessedLetters.includes(letter)) {
        showMessage(`你已经猜过字母 "${letter}" 了`, 'error');
        letterInput.value = '';
        letterInput.focus();
        return;
    }
    
    // 添加到已猜字母列表
    gameState.guessedLetters.push(letter);
    
    // 检查是否猜对
    if (gameState.targetWord.includes(letter)) {
        // 猜对了
        gameState.correctLetters.push(letter);
        showMessage(`恭喜你，猜对了！字母 "${letter}" 在单词中`, 'success');
        
        // 检查是否已经猜完所有字母
        const allLettersGuessed = [...new Set(gameState.targetWord)].every(letter => 
            gameState.correctLetters.includes(letter)
        );
        
        if (allLettersGuessed) {
            endGame(true);
            return;
        }
    } else {
        // 猜错了
        gameState.remainingGuesses--;
        showMessage(`很遗憾，猜错了。字母 "${letter}" 不在单词中`, 'error');
        
        // 检查是否用完了猜测次数
        if (gameState.remainingGuesses <= 0) {
            endGame(false);
            return;
        }
    }
    
    // 更新界面
    updateGameDisplay();
    letterInput.value = '';
    letterInput.focus();
}

// 结束游戏
function endGame(isWin) {
    gameState.isGameOver = true;
    
    // 切换到结果界面
    gameScreen.classList.remove('active');
    resultScreen.classList.add('active');
    
    if (isWin) {
        resultTitle.textContent = '恭喜你，游戏胜利！';
        resultTitle.className = 'win';
        resultDetail.textContent = `你成功猜出了单词 "${gameState.targetWord.toUpperCase()}"，总共使用了 ${gameState.maxGuesses - gameState.remainingGuesses} 次猜测机会。`;
    } else {
        resultTitle.textContent = '很遗憾，游戏失败！';
        resultTitle.className = 'lose';
        resultDetail.textContent = `很遗憾，你用完了所有猜测次数。正确单词是 "${gameState.targetWord.toUpperCase()}"。`;
    }
}

// 显示消息
function showMessage(message, type) {
    gameMessage.textContent = message;
    gameMessage.className = `game-message ${type}`;
    
    // 3秒后自动清除消息
    setTimeout(() => {
        clearMessage();
    }, 3000);
}

// 清除消息
function clearMessage() {
    gameMessage.textContent = '';
    gameMessage.className = 'game-message';
}

// 重置游戏
function resetGame() {
    // 清空输入框和错误提示
    guessCountInput.value = '';
    setupError.textContent = '';
    letterInput.value = '';
    
    // 切换到初始界面
    gameScreen.classList.remove('active');
    resultScreen.classList.remove('active');
    setupScreen.classList.add('active');
    
    guessCountInput.focus();
}

// 页面加载完成后初始化
window.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    guessCountInput.focus();
});
