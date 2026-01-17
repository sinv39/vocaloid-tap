package com.lianyinchengge.controller;

import com.lianyinchengge.entity.Song;
import com.lianyinchengge.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private SongService songService;

    /**
     * 根据旋律查询歌曲
     * GET /api/game/search?melody=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchByMelody(@RequestParam String melody) {
        Optional<Song> songOpt = songService.searchByMelody(melody);
        
        if (songOpt.isPresent()) {
            Song song = songOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("found", true);
            response.put("title", song.getTitle());
            // 将音频数据转换为 base64
            String audioBase64 = java.util.Base64.getEncoder().encodeToString(song.getAudioData());
            response.put("audioData", "data:audio/mpeg;base64," + audioBase64);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("found", false);
            return ResponseEntity.ok(response);
        }
    }
}
