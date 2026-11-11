package org.rural.learn
import android.os.Bundle
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import org.rural.learn.feature.GardenViewModel
import org.rural.learn.feature.design.*
import org.rural.learn.feature.learning.*
import org.rural.learn.feature.settings.*
import org.rural.learn.feature.audio.LessonAudio
import org.rural.learn.feature.research.*
class MainActivity: AppCompatActivity() {
 private fun event(type: String) { val c=(application as GardenApp).container; c.scope.launch { c.learning.log(c.settings.flow.first().activeLearnerId,type) } }
 override fun onStart() { super.onStart(); if(!changing) event("session_start"); changing=false }
 private var changing=false
 override fun onStop() { changing=isChangingConfigurations; if(!changing) event("session_end"); super.onStop() }
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); if(savedInstanceState==null) event("app_open"); setContent {
  val vm: GardenViewModel=viewModel(factory=object: ViewModelProvider.Factory { override fun <T: ViewModel> create(modelClass: Class<T>): T { @Suppress("UNCHECKED_CAST") return GardenViewModel((application as GardenApp).container) as T } })
  GardenRoot(vm)
 } }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable fun GardenRoot(vm: GardenViewModel) {
 val settings by vm.settings.collectAsState(); val ready by vm.ready.collectAsState(); val error by vm.error.collectAsState()
 LaunchedEffect(settings.language) { if(settings.language.isNotEmpty() && AppCompatDelegate.getApplicationLocales().toLanguageTags()!=settings.language) AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(settings.language)) }
 val context=LocalContext.current
 val audio=remember { LessonAudio(context) { vm.log("audio_played") } }
 DisposableEffect(audio) { onDispose { audio.close() } }
 val density=LocalDensity.current
 CompositionLocalProvider(LocalDensity provides Density(density.density,maxOf(density.fontScale,settings.textScale))) { GardenTheme { Surface(Modifier.fillMaxSize()) {
  val nav=rememberNavController(); val entry by nav.currentBackStackEntryAsState(); val route=entry?.destination?.route ?: "home"
  var pending by remember { mutableStateOf<org.rural.learn.domain.content.Lesson?>(null) }
  val currentRun by vm.run.collectAsState()
  LaunchedEffect(route) { audio.stop() }
  BackHandler { vm.log("back_press",mapOf("screen" to route)); if(route!="home") nav.popBackStack() else (context as? android.app.Activity)?.finish() }
  fun home() { nav.navigate("home") { popUpTo("home") { inclusive=true }; launchSingleTop=true } }
  Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
   Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),horizontalArrangement=Arrangement.SpaceBetween) {
    Text(stringResource(R.string.app_name),style=MaterialTheme.typography.titleLarge,modifier=Modifier.weight(1f).heightIn(min=56.dp).combinedClickable(onClick={home()},onLongClick={nav.navigate("research")}))
    TextButton(onClick={home()},modifier=Modifier.heightIn(min=56.dp)) { Text(stringResource(R.string.home)) }
   }
   Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
    if(!ready) Text(stringResource(R.string.loading))
    else if(settings.language.isEmpty()) LanguageScreen({vm.language(it)},{text,lang->audio.play(text,lang,null,settings.audioOn)})
    else if(settings.activeLearnerId.isEmpty()) Profiles(vm)
    else {

     NavHost(navController=nav,startDestination="home",enterTransition={androidx.compose.animation.EnterTransition.None},exitTransition={androidx.compose.animation.ExitTransition.None},popEnterTransition={androidx.compose.animation.EnterTransition.None},popExitTransition={androidx.compose.animation.ExitTransition.None}) {
      composable("home") { Column(verticalArrangement=Arrangement.spacedBy(16.dp)) { Home(vm) { lesson -> if(currentRun!=null && currentRun?.complete==false && currentRun?.lessonId!=lesson.id) pending=lesson else { vm.start(lesson); nav.navigate("lesson") } }; Action(stringResource(R.string.progress)) { nav.navigate("progress") }; Action(stringResource(R.string.settings)) { nav.navigate("settings") }; Action(stringResource(R.string.downloads)) { nav.navigate("downloads") }; Action(stringResource(R.string.switch_profile)) { vm.select("") } } }
      composable("lesson") { Column(verticalArrangement=Arrangement.spacedBy(16.dp)) { LessonScreen(vm,{text,asset->audio.play(text,settings.language,asset,settings.audioOn)}) { Survey(vm) { text->audio.play(text,settings.language,null,settings.audioOn) } } } }
      composable("progress") { ProgressScreen(vm) }
      composable("settings") { SettingsScreen(vm) { text,lang->audio.play(text,lang,null,settings.audioOn) } }
      composable("downloads") { DownloadsScreen(vm) }
      composable("research") { ResearchScreen(vm) }
     }
    }
    pending?.let { lesson -> AlertDialog(onDismissRequest={pending=null},text={Text(stringResource(R.string.replace))},confirmButton={TextButton(onClick={pending=null;vm.start(lesson);nav.navigate("lesson")},modifier=Modifier.heightIn(min=56.dp)) { Text(stringResource(R.string.start)) }},dismissButton={TextButton(onClick={pending=null},modifier=Modifier.heightIn(min=56.dp)) { Text(stringResource(R.string.cancel)) }}) }
    if(error.isNotEmpty()) { Text(stringResource(R.string.error)); Action(stringResource(R.string.retry)) { vm.reload() }; Action(stringResource(R.string.dismiss)) { vm.error.value="" } }
   }
  }
 } } }
}
