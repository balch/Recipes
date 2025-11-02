package org.balch.recipes.ui.widgets

import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


@Composable
fun YouTubePlayerWidget(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var playerView by remember { mutableStateOf<YouTubePlayerView?>(null) }
    var fullScreenView by remember { mutableStateOf<View?>(null) }

    var youTubePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                enableAutomaticInitialization = false

                lifecycleOwner.lifecycle.addObserver(this)

                val options = IFramePlayerOptions.Builder(ctx)
                    .controls(1)
                    .fullscreen(1)
                    .autoplay(0)
                    .ivLoadPolicy(3)
                    .build()

                val listener = object : AbstractYouTubePlayerListener() {
                    override fun onReady(player: YouTubePlayer) {
                        youTubePlayer = player
                    }
                }

                initialize(listener, options)

                addFullscreenListener(object : FullscreenListener {
                    override fun onEnterFullscreen(
                        fullscreenView: View,
                        exitFullscreen: () -> Unit
                    ) {
                        val decor = activity?.window?.decorView as? ViewGroup ?: return
                        decor.addView(
                            fullscreenView,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                        fullScreenView = fullscreenView
                    }

                    override fun onExitFullscreen() {
                        val decor = activity?.window?.decorView as? ViewGroup ?: return
                        fullScreenView?.let { decor.removeView(it) }
                        fullScreenView = null
                    }
                })

                playerView = this
            }
        }
    )

    // Use LaunchedEffect to react to changes in videoId or the player instance
    LaunchedEffect(youTubePlayer, videoUrl) {
        youTubePlayer?.loadOrCueVideo(lifecycleOwner.lifecycle,
            extractYouTubeId(videoUrl),
            0f
        )
    }

    DisposableEffect(lifecycleOwner, activity) {
        onDispose {
            val decor = activity?.window?.decorView as? ViewGroup
            fullScreenView?.let { decor?.removeView(it) }
            fullScreenView = null
            playerView?.release()
            playerView = null
        }
    }
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

