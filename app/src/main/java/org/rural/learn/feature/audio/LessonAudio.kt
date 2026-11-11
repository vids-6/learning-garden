package org.rural.learn.feature.audio
import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale
/** Lazy platform audio: only local TTS voices, no download intents or network voices. */
class LessonAudio(private val context: Context,private val onPlayed: ()->Unit) {
 private var tts: TextToSpeech?=null; private var ready=false; private var pending: (() -> Unit)?=null; private var player: MediaPlayer?=null
 fun play(text: String,lang: String,asset: String?,enabled: Boolean) {
  if(!enabled) return
  stop()
  if(asset!=null) try {
   context.assets.openFd(asset).use { fd ->
    val media=MediaPlayer(); player=media
    media.setDataSource(fd.fileDescriptor,fd.startOffset,fd.length)
    media.setOnPreparedListener { it.start(); onPlayed() }
    media.setOnCompletionListener { it.release(); if(player===it) player=null }
    media.setOnErrorListener { p,_,_ -> p.release(); if(player===p) player=null; speak(text,lang); true }
    media.prepareAsync(); return
   }
  } catch(_: Exception) { player?.release(); player=null }
  speak(text,lang)
 }
 private fun speak(text: String,lang: String) {
  val action: () -> Unit = {
   val engine=tts
   if(engine!=null) {
    val voice=engine.voices?.filter { !it.isNetworkConnectionRequired && it.locale.language==Locale.forLanguageTag(lang).language }?.sortedBy { it.name }?.firstOrNull()
    if(voice!=null) { engine.voice=voice; if(engine.speak(text,TextToSpeech.QUEUE_FLUSH,null,"lesson")==TextToSpeech.SUCCESS) onPlayed() }
   }
  }
  if(ready) action() else { pending=action; if(tts==null) tts=TextToSpeech(context) { status -> ready=status==TextToSpeech.SUCCESS; if(ready) pending?.invoke(); pending=null } }
 }
 fun stop() { pending=null; player?.release();player=null;tts?.stop() }
 fun close() { stop();tts?.shutdown();tts=null;ready=false }
}
