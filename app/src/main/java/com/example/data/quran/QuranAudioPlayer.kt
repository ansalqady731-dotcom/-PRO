package com.example.data.quran

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.model.Reciter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

object QuranAudioPlayer {

    val reciters = listOf(
        Reciter("alafasy", "الشيخ مشاري راشد العفاسي", "https://everyayah.com/data/Alafasy_128kbps/"),
        Reciter("husary", "الشيخ محمود خليل الحصري", "https://everyayah.com/data/Husary_128kbps/"),
        Reciter("abdulsamad", "الشيخ عبد الباسط عبد الصمد", "https://everyayah.com/data/AbdulSamad_64kbps_QuranExplorer.Com/"),
        Reciter("ghamadi", "الشيخ سعد الغامدي", "https://everyayah.com/data/Ghamadi_40kbps/")
    )

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPlayingSurah = MutableStateFlow<Int?>(null)
    val currentPlayingSurah: StateFlow<Int?> = _currentPlayingSurah

    private val _currentPlayingAyah = MutableStateFlow<Int?>(null)
    val currentPlayingAyah: StateFlow<Int?> = _currentPlayingAyah

    private val _selectedReciter = MutableStateFlow(reciters[0])
    val selectedReciter: StateFlow<Reciter> = _selectedReciter

    private var maxAyahsInCurrentSurah = 1
    private var onAyahCompletedCallback: (() -> Unit)? = null

    fun setReciter(reciter: Reciter) {
        _selectedReciter.value = reciter
        val s = _currentPlayingSurah.value
        val a = _currentPlayingAyah.value
        if (_isPlaying.value && s != null && a != null) {
            playAyah(s, a, maxAyahsInCurrentSurah, onAyahCompletedCallback)
        }
    }

    fun playAyah(
        surahNumber: Int,
        ayahNumber: Int,
        totalAyahs: Int = 1,
        onComplete: (() -> Unit)? = null
    ) {
        stop()
        maxAyahsInCurrentSurah = totalAyahs
        onAyahCompletedCallback = onComplete

        val fileName = String.format(Locale.US, "%03d%03d.mp3", surahNumber, ayahNumber)
        val url = "${_selectedReciter.value.baseUrl}$fileName"

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _currentPlayingSurah.value = surahNumber
                    _currentPlayingAyah.value = ayahNumber
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    onAyahCompletedCallback?.invoke()

                    // Auto-advance to next ayah if available
                    if (ayahNumber < maxAyahsInCurrentSurah) {
                        playAyah(surahNumber, ayahNumber + 1, maxAyahsInCurrentSurah, onAyahCompletedCallback)
                    }
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            _isPlaying.value = false
        }
    }

    fun playNextAyah() {
        val s = _currentPlayingSurah.value ?: return
        val a = _currentPlayingAyah.value ?: return
        if (a < maxAyahsInCurrentSurah) {
            playAyah(s, a + 1, maxAyahsInCurrentSurah, onAyahCompletedCallback)
        }
    }

    fun playPreviousAyah() {
        val s = _currentPlayingSurah.value ?: return
        val a = _currentPlayingAyah.value ?: return
        if (a > 1) {
            playAyah(s, a - 1, maxAyahsInCurrentSurah, onAyahCompletedCallback)
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
            }
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
        }
    }
}
