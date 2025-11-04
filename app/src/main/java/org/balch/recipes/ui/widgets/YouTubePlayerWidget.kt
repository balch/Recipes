package org.balch.recipes.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun rememberYouTubePlayer(
    context: Context = LocalContext.current,
    showControls: Boolean = true,
    allowFullScreen: Boolean = true,
    autoPlay: Boolean = true,
    showAnnotations: Boolean = false,
    key: Any? = null
): YouTubePlayerState {

    return remember(
        context,
        showControls,
        allowFullScreen,
        autoPlay,
        showAnnotations,
        key
    ) {
        YouTubePlayerState(
            context = context,
            showControls = showControls,
            allowFullScreen = allowFullScreen,
            autoPlay = autoPlay,
            showAnnotations = showAnnotations,
        )
    }
}


enum class PlayerStatus {
    UNKNOWN, IDLE, LOADING, LOADED, ERROR
}


class YouTubePlayerState(
    private val context: Context,
    private val showControls: Boolean = true,
    private val allowFullScreen: Boolean = true,
    private val autoPlay: Boolean = true,
    private val showAnnotations: Boolean = false,
) {

    private val _status = mutableStateOf(PlayerStatus.UNKNOWN)

    val status: State<PlayerStatus> = _status

    private val listener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            this@YouTubePlayerState.youTubePlayer = youTubePlayer
            _status.value = PlayerStatus.IDLE
        }

        override fun onStateChange(
            youTubePlayer: YouTubePlayer,
            state: PlayerConstants.PlayerState
        ) {
            when (state) {
                PlayerConstants.PlayerState.UNKNOWN -> _status.value = PlayerStatus.UNKNOWN
                PlayerConstants.PlayerState.UNSTARTED -> _status.value = PlayerStatus.IDLE
                else -> _status.value = PlayerStatus.LOADED
            }
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
            _status.value = PlayerStatus.ERROR
        }

    }
    private var youTubePlayer: YouTubePlayer? = null
    internal val playerView =
        YouTubePlayerView(context).apply {
            enableAutomaticInitialization = false
            val options = IFramePlayerOptions.Builder(context)
                .controls(1.takeIf { showControls } ?: 0)
                .fullscreen(1.takeIf { allowFullScreen } ?: 0)
                .autoplay(1.takeIf { autoPlay } ?: 0)
                .ivLoadPolicy(1.takeIf { showAnnotations } ?: 3)
                .build()

            initialize(listener, options)
        }

    fun loadVideo(videoUrl: String, startSeconds: Float = 0f) {
        _status.value = PlayerStatus.LOADING
        youTubePlayer?.loadVideo(extractYouTubeId(videoUrl), startSeconds)
    }

    fun cueVideo(videoUrl: String, startSeconds: Float = 0f) {
        _status.value = PlayerStatus.LOADING
        youTubePlayer?.cueVideo(extractYouTubeId(videoUrl), startSeconds)
    }

    fun clear() {
        _status.value = PlayerStatus.IDLE
    }

    fun play() {
        youTubePlayer?.play()
    }

    fun release() {
        playerView.release()
        youTubePlayer = null
    }
}

@Composable
fun YouTubePlayerWidget(
    state: YouTubePlayerState,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { state.playerView }
    )
}


private val YOUTUBE_VIDEO_ID_REGEX = "(?<=youtu.be/|watch\\?v=|/videos/|embed/|shorts/)[^#&?\\s]*".toRegex()

/**
 * Extracts the YouTube video ID from a given YouTube video URL.
 *
 * @param url The full URL of the YouTube video.
 * @return The extracted video ID, or null if the URL is not a valid YouTube video URL.
 */
private fun extractYouTubeId(url: String): String =
    YOUTUBE_VIDEO_ID_REGEX.find(url)
        ?.value
        ?.takeIf { it.length == 11 }
    ?: ""

