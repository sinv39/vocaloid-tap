package com.lianyinchengge.service;

import com.lianyinchengge.entity.Song;
import com.lianyinchengge.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SongService {

    @Autowired
    private SongRepository songRepository;

    /**
     * 根据旋律查询歌曲（包含音频数据）
     */
    public Optional<Song> searchByMelody(String melody) {
        return songRepository.findByMelody(melody);
    }

    /**
     * 获取所有歌曲列表（不包含音频数据）
     */
    public List<SongDTO> getAllSongs() {
        return songRepository.findAll().stream()
                .map(song -> {
                    SongDTO dto = new SongDTO();
                    dto.setId(song.getId());
                    dto.setTitle(song.getTitle());
                    dto.setMelody(song.getMelody());
                    dto.setCreatedAt(song.getCreatedAt());
                    dto.setUpdatedAt(song.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 保存歌曲
     */
    public Song saveSong(String title, String melody, byte[] audioData) {
        Song song = new Song();
        song.setTitle(title);
        song.setMelody(melody);
        song.setAudioData(audioData);
        return songRepository.save(song);
    }

    /**
     * 删除歌曲
     */
    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }

    /**
     * DTO 类，用于返回不包含音频数据的歌曲信息
     */
    public static class SongDTO {
        private Long id;
        private String title;
        private String melody;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMelody() {
            return melody;
        }

        public void setMelody(String melody) {
            this.melody = melody;
        }

        public java.time.LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public java.time.LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
