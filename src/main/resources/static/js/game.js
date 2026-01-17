let currentInput = '';
let timeoutId = null;
const TIMEOUT_DURATION = 5000; // 5秒超时

// 音频上下文（用于播放音符）
let audioContext = null;

// 音符频率映射（C4 到 B4，简谱 1-7）
const NOTE_FREQUENCIES = {
    '1': 261.63,  // C4 (Do)
    '2': 293.66,  // D4 (Re)
    '3': 329.63,  // E4 (Mi)
    '4': 349.23,  // F4 (Fa)
    '5': 392.00,  // G4 (Sol)
    '6': 440.00,  // A4 (La)
    '7': 493.88   // B4 (Si)
};

/**
 * 初始化音频上下文
 */
function initAudioContext() {
    if (!audioContext) {
        audioContext = new (window.AudioContext || window.webkitAudioContext)();
    }
    return audioContext;
}

/**
 * 播放音符（钢琴音色）
 */
function playNote(note) {
    const frequency = NOTE_FREQUENCIES[note];
    if (!frequency) return;
    
    const ctx = initAudioContext();
    const oscillator = ctx.createOscillator();
    const gainNode = ctx.createGain();
    
    // 使用正弦波，音色更纯净
    oscillator.type = 'sine';
    oscillator.frequency.setValueAtTime(frequency, ctx.currentTime);
    
    // 创建 ADSR 包络（Attack-Decay-Sustain-Release）让声音更像钢琴
    const now = ctx.currentTime;
    const attackTime = 0.01;  // 起音时间
    const decayTime = 0.1;    // 衰减时间
    const sustainLevel = 0.3; // 持续音量
    const releaseTime = 0.2;  // 释放时间
    const duration = 0.3;     // 总时长
    
    gainNode.gain.setValueAtTime(0, now);
    gainNode.gain.linearRampToValueAtTime(0.3, now + attackTime);  // Attack
    gainNode.gain.linearRampToValueAtTime(sustainLevel, now + attackTime + decayTime);  // Decay
    gainNode.gain.setValueAtTime(sustainLevel, now + duration - releaseTime);  // Sustain
    gainNode.gain.linearRampToValueAtTime(0, now + duration);  // Release
    
    // 连接节点
    oscillator.connect(gainNode);
    gainNode.connect(ctx.destination);
    
    // 播放并停止
    oscillator.start(now);
    oscillator.stop(now + duration);
}

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    const noteButtons = document.querySelectorAll('.note-btn');
    
    noteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const note = this.getAttribute('data-note');
            handleNoteClick(note);
        });
    });
    
    updateInputDisplay();
});

/**
 * 处理音符点击
 */
function handleNoteClick(note) {
    // 播放音符声音
    playNote(note);
    
    // 添加音符到当前输入
    currentInput += note;
    updateInputDisplay();
    
    // 重置超时定时器
    clearTimeout(timeoutId);
    
    // 设置新的超时定时器
    timeoutId = setTimeout(() => {
        searchSong(currentInput);
    }, TIMEOUT_DURATION);
}

/**
 * 更新输入显示
 */
function updateInputDisplay() {
    const inputElement = document.getElementById('current-input');
    inputElement.textContent = currentInput || '(空)';
}

/**
 * 搜索歌曲
 */
function searchSong(melody) {
    if (!melody || melody.length === 0) {
        clearInput();
        return;
    }
    
    // 显示加载状态
    const resultContainer = document.getElementById('result-container');
    resultContainer.style.display = 'block';
    document.getElementById('song-title').textContent = '搜索中...';
    
    // 发送请求
    fetch(`/api/game/search?melody=${encodeURIComponent(melody)}`)
        .then(response => response.json())
        .then(data => {
            if (data.found) {
                // 找到歌曲，显示标题并播放音频
                document.getElementById('song-title').textContent = data.title;
                const audioPlayer = document.getElementById('audio-player');
                audioPlayer.src = data.audioData;
                audioPlayer.load();
                
                // 播放音频
                audioPlayer.play().catch(err => {
                    console.error('播放音频失败:', err);
                });
                
                // 清空输入，准备下一轮
                setTimeout(() => {
                    clearInput();
                }, 1000);
            } else {
                // 未找到歌曲
                document.getElementById('song-title').textContent = '未找到匹配的歌曲';
                document.getElementById('audio-player').src = '';
                
                // 清空输入
                setTimeout(() => {
                    clearInput();
                    resultContainer.style.display = 'none';
                }, 2000);
            }
        })
        .catch(error => {
            console.error('搜索失败:', error);
            document.getElementById('song-title').textContent = '搜索失败，请重试';
            setTimeout(() => {
                clearInput();
                resultContainer.style.display = 'none';
            }, 2000);
        });
}

/**
 * 清空输入
 */
function clearInput() {
    currentInput = '';
    updateInputDisplay();
    clearTimeout(timeoutId);
    timeoutId = null;
}
