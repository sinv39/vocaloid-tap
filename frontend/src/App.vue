<template>
  <div class="container">
    <h1>连音成歌</h1>
    <GameBoard />
    <div class="links">
      <a href="/admin.html">管理页面</a>
    </div>
  </div>
</template>

<script>
import GameBoard from './components/GameBoard.vue';

export default {
  name: 'App',
  components: {
    GameBoard
  }
};
</script>

