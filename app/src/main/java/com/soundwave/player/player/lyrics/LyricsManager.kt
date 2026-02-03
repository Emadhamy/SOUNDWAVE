package com.soundwave.player.player.lyrics

import android.content.Context
import com.soundwave.player.data.local.database.dao.LyricsDao
import com.soundwave.player.data.local.database.entities.LyricsEntity
import com.soundwave.player.domain.model.LyricLine
import com.soundwave.player.domain.model.Lyrics
import com.soundwave.player.domain.model.LyricsSource
import com.soundwave.player.domain.model.LyricsState
import com.soundwave.player.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lyricsDao: LyricsDao
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _state = MutableStateFlow(LyricsState())
    val state: StateFlow<LyricsState> = _state.asStateFlow()
    
    private var currentSongId: Long = 0
    
    suspend fun loadLyrics(song: Song) {
        if (song.id == currentSongId && _state.value.lyrics != null) return
        
        currentSongId = song.id
        _state.update { it.copy(isLoading = true, lyrics = null, error = null) }
        
        try {
            // 1. البحث في قاعدة البيانات
            val cached = lyricsDao.getLyricsBySongIdSync(song.id)
            if (cached != null) {
                val lyrics = cached.toLyrics()
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        lyrics = lyrics,
                        source = LyricsSource.LOCAL
                    )
                }
                return
            }
            
            // 2. البحث عن ملف .lrc
            val lrcFile = findLrcFile(song.path)
            if (lrcFile != null) {
                val lyrics = parseLrcFile(lrcFile, song)
                if (lyrics != null) {
                    saveLyrics(lyrics)
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            lyrics = lyrics,
                            source = LyricsSource.LOCAL
                        )
                    }
                    return
                }
            }
            
            // 3. البحث في metadata الملف
            val embeddedLyrics = extractEmbeddedLyrics(song)
            if (embeddedLyrics != null) {
                saveLyrics(embeddedLyrics)
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        lyrics = embeddedLyrics,
                        source = LyricsSource.EMBEDDED
                    )
                }
                return
            }
            
            // لم يتم العثور على كلمات
            _state.update { 
                it.copy(
                    isLoading = false, 
                    lyrics = null,
                    error = "لم يتم العثور على كلمات"
                )
            }
            
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    isLoading = false, 
                    error = e.message
                )
            }
        }
    }
    
    fun updateCurrentLine(position: Long) {
        val lyrics = _state.value.lyrics ?: return
        if (!lyrics.isSynced) return
        
        val currentIndex = lyrics.lines.indexOfLast { it.startTime <= position }
        if (currentIndex != _state.value.currentLineIndex && currentIndex >= 0) {
            _state.update { it.copy(currentLineIndex = currentIndex) }
        }
    }
    
    suspend fun saveLyrics(lyrics: Lyrics) {
        withContext(Dispatchers.IO) {
            val entity = LyricsEntity(
                songId = lyrics.songId,
                title = lyrics.title,
                artist = lyrics.artist,
                lyricsJson = json.encodeToString(lyrics.lines),
                isSynced = lyrics.isSynced,
                language = lyrics.language,
                source = lyrics.source.name
            )
            lyricsDao.insertLyrics(entity)
        }
    }
    
    suspend fun deleteLyrics(songId: Long) {
        withContext(Dispatchers.IO) {
            lyricsDao.deleteLyrics(songId)
        }
        if (currentSongId == songId) {
            _state.update { LyricsState() }
        }
    }
    
    fun clearLyrics() {
        currentSongId = 0
        _state.update { LyricsState() }
    }
    
    private suspend fun findLrcFile(songPath: String): File? = withContext(Dispatchers.IO) {
        val songFile = File(songPath)
        val baseName = songFile.nameWithoutExtension
        val parentDir = songFile.parentFile ?: return@withContext null
        
        // البحث عن ملف .lrc بنفس الاسم
        val lrcFile = File(parentDir, "$baseName.lrc")
        if (lrcFile.exists()) return@withContext lrcFile
        
        // البحث في مجلد lyrics فرعي
        val lyricsDir = File(parentDir, "lyrics")
        if (lyricsDir.exists()) {
            val lrcInSubdir = File(lyricsDir, "$baseName.lrc")
            if (lrcInSubdir.exists()) return@withContext lrcInSubdir
        }
        
        null
    }
    
    private suspend fun parseLrcFile(file: File, song: Song): Lyrics? = withContext(Dispatchers.IO) {
        try {
            val content = file.readText()
            val lines = mutableListOf<LyricLine>()
            var isSynced = false
            
            val timePattern = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
            
            content.lines().forEach { line ->
                val match = timePattern.find(line.trim())
                if (match != null) {
                    isSynced = true
                    val (minutes, seconds, millis, text) = match.destructured
                    val time = minutes.toLong() * 60 * 1000 +
                               seconds.toLong() * 1000 +
                               millis.padEnd(3, '0').take(3).toLong()
                    
                    if (text.isNotBlank()) {
                        lines.add(LyricLine(startTime = time, text = text.trim()))
                    }
                } else if (line.isNotBlank() && !line.startsWith("[")) {
                    // سطر بدون وقت
                    lines.add(LyricLine(startTime = 0, text = line.trim()))
                }
            }
            
            // حساب endTime لكل سطر
            val linesWithEnd = lines.mapIndexed { index, lyricLine ->
                val endTime = lines.getOrNull(index + 1)?.startTime ?: (lyricLine.startTime + 5000)
                lyricLine.copy(endTime = endTime)
            }
            
            Lyrics(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                lines = linesWithEnd,
                isSynced = isSynced,
                source = LyricsSource.LOCAL
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun extractEmbeddedLyrics(song: Song): Lyrics? = withContext(Dispatchers.IO) {
        // استخراج الكلمات من metadata الملف
        // يحتاج مكتبة إضافية مثل JAudioTagger
        null
    }
    
    private fun LyricsEntity.toLyrics(): Lyrics {
        val lines: List<LyricLine> = try {
            json.decodeFromString(lyricsJson)
        } catch (e: Exception) {
            emptyList()
        }
        
        return Lyrics(
            songId = songId,
            title = title,
            artist = artist,
            lines = lines,
            isSynced = isSynced,
            language = language,
            source = LyricsSource.valueOf(source)
        )
    }
}