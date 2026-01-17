package com.lianyinchengge.controller;

import com.lianyinchengge.entity.Song;
import com.lianyinchengge.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private SongService songService;

    /**
     * 获取所有歌曲列表（不包含音频数据）
     * GET /api/admin/songs
     */
    @GetMapping("/songs")
    public ResponseEntity<List<SongService.SongDTO>> getAllSongs() {
        List<SongService.SongDTO> songs = songService.getAllSongs();
        return ResponseEntity.ok(songs);
    }

    /**
     * 新增歌曲
     * POST /api/admin/songs
     */
    @PostMapping("/songs")
    public ResponseEntity<Map<String, Object>> saveSong(
            @RequestParam String title,
            @RequestParam String melody,
            @RequestParam("audioFile") MultipartFile audioFile) {
        
        try {
            if (audioFile.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "音频文件不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            byte[] audioData = audioFile.getBytes();
            Song song = songService.saveSong(title, melody, audioData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "歌曲添加成功");
            response.put("id", song.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "添加歌曲失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 删除歌曲
     * DELETE /api/admin/songs/{id}
     */
    @DeleteMapping("/songs/{id}")
    public ResponseEntity<Map<String, Object>> deleteSong(@PathVariable Long id) {
        try {
            songService.deleteSong(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "歌曲删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "删除歌曲失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
