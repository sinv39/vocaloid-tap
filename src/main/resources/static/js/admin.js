// 页面加载时获取歌曲列表
document.addEventListener('DOMContentLoaded', function() {
    loadSongs();
    
    // 绑定表单提交事件
    document.getElementById('add-song-form').addEventListener('submit', function(e) {
        e.preventDefault();
        addSong();
    });
});

/**
 * 加载歌曲列表
 */
function loadSongs() {
    const loading = document.getElementById('loading');
    const table = document.getElementById('songs-table');
    
    loading.style.display = 'block';
    table.style.display = 'none';
    
    fetch('/api/admin/songs')
        .then(response => response.json())
        .then(data => {
            loading.style.display = 'none';
            displaySongs(data);
            table.style.display = 'table';
        })
        .catch(error => {
            console.error('加载歌曲列表失败:', error);
            loading.textContent = '加载失败，请刷新重试';
        });
}

/**
 * 显示歌曲列表
 */
function displaySongs(songs) {
    const tbody = document.getElementById('songs-tbody');
    tbody.innerHTML = '';
    
    if (songs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">暂无歌曲</td></tr>';
        return;
    }
    
    songs.forEach(song => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${song.id}</td>
            <td>${escapeHtml(song.title)}</td>
            <td>${escapeHtml(song.melody)}</td>
            <td>${formatDateTime(song.createdAt)}</td>
            <td>
                <button class="btn btn-danger" onclick="deleteSong(${song.id})">删除</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

/**
 * 添加歌曲
 */
function addSong() {
    const form = document.getElementById('add-song-form');
    const formData = new FormData(form);
    const messageDiv = document.getElementById('form-message');
    
    // 清除之前的消息
    messageDiv.className = '';
    messageDiv.style.display = 'none';
    
    fetch('/api/admin/songs', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                messageDiv.className = 'success';
                messageDiv.textContent = data.message;
                messageDiv.style.display = 'block';
                
                // 重置表单
                form.reset();
                
                // 重新加载歌曲列表
                loadSongs();
            } else {
                messageDiv.className = 'error';
                messageDiv.textContent = data.message || '添加失败';
                messageDiv.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('添加歌曲失败:', error);
            messageDiv.className = 'error';
            messageDiv.textContent = '添加失败: ' + error.message;
            messageDiv.style.display = 'block';
        });
}

/**
 * 删除歌曲
 */
function deleteSong(id) {
    if (!confirm('确定要删除这首歌曲吗？')) {
        return;
    }
    
    fetch(`/api/admin/songs/${id}`, {
        method: 'DELETE'
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message);
                loadSongs();
            } else {
                alert('删除失败: ' + (data.message || '未知错误'));
            }
        })
        .catch(error => {
            console.error('删除歌曲失败:', error);
            alert('删除失败: ' + error.message);
        });
}

/**
 * 转义 HTML
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '';
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-CN');
}
