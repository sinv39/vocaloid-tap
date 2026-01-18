<template>
  <div>
    <div class="notes-container">
      <button
        v-for="note in notes"
        :key="note"
        class="note-btn"
        :data-note="note"
        @click="handleNoteClick(note)"
      >
        {{ note }}
      </button>
    </div>

    <div class="input-display">
      <p>当前输入: <span>{{ currentInput || '(空)' }}</span></p>
    </div>

    <div v-if="showResult" class="result-container">
      <h2>找到的歌曲:</h2>
      <p class="song-title">{{ songTitle }}</p>
      <audio
        v-if="audioSrc"
        ref="audioPlayer"
        class="audio-player"
        :src="audioSrc"
        controls
        @ended="onAudioEnded"
      ></audio>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GameBoard',
  data() {
    return {
      notes: ['1', '2', '3', '4', '5', '6', '7'],
      currentInput: '',
      timeoutId: null,
      websocket: null,
      audioContext: null,
      showResult: false,
      songTitle: '',
      audioSrc: '',
      CLEAR_TIMEOUT: 2000 // 2秒无操作自动清除
    };
  },
  mounted() {
    this.initWebSocket();
    this.initAudioContext();
  },
  beforeUnmount() {
    this.clearTimeout();
    if (this.websocket) {
      this.websocket.close();
    }
  },
  methods: {
    initWebSocket() {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = `${protocol}//${window.location.host}/ws/game`;
      
      this.websocket = new WebSocket(wsUrl);
      
      this.websocket.onopen = () => {
        console.log('WebSocket连接已建立');
      };
      
      this.websocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this.handleWebSocketMessage(data);
        } catch (error) {
          console.error('解析WebSocket消息失败:', error);
        }
      };
      
      this.websocket.onerror = (error) => {
        console.error('WebSocket错误:', error);
      };
      
      this.websocket.onclose = () => {
        console.log('WebSocket连接已关闭');
        // 可以在这里实现重连逻辑
      };
    },
    
    initAudioContext() {
      // 初始化Web Audio API用于播放音符
      if (!this.audioContext) {
        this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
      }
    },
    
    playNote(note) {
      const NOTE_FREQUENCIES = {
        '1': 523.25,  // C5 (Do) - 提高一个八度
        '2': 587.33,  // D5 (Re)
        '3': 659.25,  // E5 (Mi)
        '4': 698.46,  // F5 (Fa)
        '5': 783.99,  // G5 (Sol)
        '6': 880.00,  // A5 (La)
        '7': 987.77   // B5 (Si)
      };
      
      const frequency = NOTE_FREQUENCIES[note];
      if (!frequency || !this.audioContext) return;
      
      const ctx = this.audioContext;
      const now = ctx.currentTime;
      const duration = 0.6; // 古典钢琴声音更长
      
      // 创建主增益节点
      const masterGain = ctx.createGain();
      
      // 古典钢琴音色：使用多个正弦波叠加，模拟真实的钢琴谐波
      // 真实钢琴的谐波强度比例（基于实际钢琴频谱分析）
      const harmonics = [
        { freq: 1, gain: 1.0 },    // 基频 - 最强
        { freq: 2, gain: 0.5 },    // 2倍频
        { freq: 3, gain: 0.3 },    // 3倍频
        { freq: 4, gain: 0.15 },   // 4倍频
        { freq: 5, gain: 0.08 },   // 5倍频
        { freq: 6, gain: 0.04 }    // 6倍频
      ];
      
      const oscillators = [];
      const gains = [];
      
      // 创建所有谐波振荡器
      harmonics.forEach((harmonic, index) => {
        const osc = ctx.createOscillator();
        osc.type = 'sine'; // 使用正弦波，更纯净自然
        osc.frequency.setValueAtTime(frequency * harmonic.freq, now);
        
        const gain = ctx.createGain();
        gain.gain.setValueAtTime(harmonic.gain * 0.35, now); // 增加音量（从0.15提高到0.35）
        
        osc.connect(gain);
        gain.connect(masterGain);
        
        oscillators.push(osc);
        gains.push(gain);
      });
      
      // 古典钢琴的包络特性：快速起音，长衰减，无持续音
      const attackTime = 0.01;    // 快速起音
      const decayTime = 0.4;     // 较长的衰减（古典钢琴声音衰减较慢）
      const releaseTime = 0.19;   // 释放时间
      
      // 使用指数衰减，更接近真实钢琴的衰减曲线
      masterGain.gain.setValueAtTime(0, now);
      masterGain.gain.linearRampToValueAtTime(0.6, now + attackTime); // 增加主音量（从0.35提高到0.6）
      masterGain.gain.exponentialRampToValueAtTime(0.01, now + attackTime + decayTime);
      masterGain.gain.exponentialRampToValueAtTime(0.001, now + duration);
      
      masterGain.connect(ctx.destination);
      
      // 启动所有振荡器
      oscillators.forEach(osc => {
        osc.start(now);
        osc.stop(now + duration);
      });
    },
    
    handleNoteClick(note) {
      // 如果正在播放歌曲，停止播放并清空歌曲数据
      if (this.showResult && this.$refs.audioPlayer) {
        this.stopAudio();
        this.clearSongData();
      }
      
      // 播放音符声音
      this.playNote(note);
      
      // 添加音符到当前输入
      this.currentInput += note;
      
      // 清除旧的定时器
      this.clearTimeout();
      
      // 立即发送完整字符串到后端
      this.sendMelody(this.currentInput);
      
      // 设置新的2秒清除定时器
      this.timeoutId = setTimeout(() => {
        this.clearInput();
      }, this.CLEAR_TIMEOUT);
    },
    
    sendMelody(melody) {
      if (!this.websocket || this.websocket.readyState !== WebSocket.OPEN) {
        console.warn('WebSocket未连接');
        return;
      }
      
      const message = {
        melody: melody
      };
      
      this.websocket.send(JSON.stringify(message));
    },
    
    handleWebSocketMessage(data) {
      if (data.found) {
        // 匹配成功，立即播放并清空
        this.songTitle = data.title;
        this.audioSrc = data.audioData;
        this.showResult = true;
        
        // 立即清空输入
        this.clearInput();
        
        // 播放音频
        this.$nextTick(() => {
          if (this.$refs.audioPlayer) {
            this.$refs.audioPlayer.play().catch(err => {
              console.error('播放音频失败:', err);
            });
          }
        });
      } else {
        // 未匹配，保留输入继续等待
        // 不执行任何操作，让用户继续点击
      }
    },
    
    clearInput() {
      this.currentInput = '';
      this.clearTimeout();
    },
    
    clearTimeout() {
      if (this.timeoutId) {
        clearTimeout(this.timeoutId);
        this.timeoutId = null;
      }
    },
    
    stopAudio() {
      if (this.$refs.audioPlayer) {
        this.$refs.audioPlayer.pause();
        this.$refs.audioPlayer.currentTime = 0;
      }
    },
    
    clearSongData() {
      this.showResult = false;
      this.songTitle = '';
      this.audioSrc = '';
      this.currentInput = '';
      this.clearTimeout();
    },
    
    onAudioEnded() {
      // 音频播放结束后，清空字符串和歌曲数据
      this.clearSongData();
    }
  }
};
</script>

