package org.rural.learn.feature.settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.rural.learn.R
import org.rural.learn.feature.GardenViewModel
import org.rural.learn.feature.design.*
import org.rural.learn.feature.learning.LanguageScreen
import org.rural.learn.domain.content.Pack
import org.rural.learn.domain.gamification.LearningRules
@Composable fun ProgressScreen(vm: GardenViewModel) {
 val settings by vm.settings.collectAsState(); val progress by vm.progress.collectAsState(); val xp by vm.xp.collectAsState(); val badges by vm.badges.collectAsState(); val subjects by vm.subjects.collectAsState()
 val id=settings.activeLearnerId; val level=LearningRules.level(xp.filter { it.learnerId==id }.sumOf { it.amount })
 Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
  Text(stringResource(R.string.progress),style=MaterialTheme.typography.headlineMedium); Plant(level.stage); Text("${stringResource(R.string.level)} ${level.number}")
  Text(stringResource(R.string.mastery),style=MaterialTheme.typography.titleLarge)
  subjects.forEach { subject -> val lessons=subject.units.flatMap { it.lessons }; val done=progress.count { it.learnerId==id && it.lessonId in lessons.map { it.id } }; Text("${subject.title}: $done / ${lessons.size}"); LinearProgressIndicator(progress={done.toFloat()/lessons.size},modifier=Modifier.fillMaxWidth()) }
  Text(stringResource(R.string.badges),style=MaterialTheme.typography.titleLarge)
  val awards=badges.filter { it.learnerId==id }; if(awards.isEmpty()) Text(stringResource(R.string.empty)); awards.forEach { Text("★ ${stringResource(badgeLabel(it.badgeId))}") }
 }
}
@Composable fun SettingsScreen(vm: GardenViewModel,onAudio: (String,String)->Unit) {
 val s by vm.settings.collectAsState(); val bytes by vm.storage.collectAsState()
 Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
  Text(stringResource(R.string.settings),style=MaterialTheme.typography.headlineMedium)
  LanguageScreen({vm.language(it)},onAudio)
  Toggle(stringResource(R.string.audio_setting),s.audioOn) { vm.boolean("audioOn",it) }
  Toggle(stringResource(R.string.text_size),s.textScale>1f) { vm.scale(if(it) 1.5f else 1f) }
  Toggle(stringResource(R.string.reduced_motion),s.reducedMotion) { vm.boolean("reducedMotion",it) }
  Toggle(stringResource(R.string.data_saver),s.dataSaver) { vm.boolean("dataSaver",it) }
  Text("${stringResource(R.string.storage)}: ${bytes/1024} KB")
  Action(stringResource(R.string.sync),s.syncStatus!="running") { vm.sync() }
  Text(stringResource(when(s.syncStatus) { "running"->R.string.sync_running; "success"->R.string.sync_success; "failed"->R.string.sync_failed; else->R.string.sync_idle }))
 }
}
@Composable private fun Toggle(label: String,checked: Boolean,onChange: (Boolean)->Unit) { Row(Modifier.fillMaxWidth().heightIn(min=56.dp),horizontalArrangement=Arrangement.SpaceBetween) { Text(label,Modifier.weight(1f)); Switch(checked,onChange,modifier=Modifier.sizeIn(minWidth=56.dp,minHeight=56.dp)) } }
@Composable fun DownloadsScreen(vm: GardenViewModel) {
 val subjects by vm.subjects.collectAsState(); val packs by vm.packs.collectAsState(); val installed by vm.installed.collectAsState(); val busy by vm.busy.collectAsState(); var consent by remember { mutableStateOf<Pack?>(null) }
 Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
  Text(stringResource(R.string.downloads),style=MaterialTheme.typography.headlineMedium)
  packs.forEach { p -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("${p.lang.uppercase()} • ${subjects.find { it.subjectId==p.subject }?.title ?: p.subject} • v${p.version}"); Text("${p.sizeBytes/1024} KB"); if(p.bundled || installed.any { it.packId==p.id }) Text("✓ ${stringResource(R.string.installed)}") else Action(stringResource(R.string.install),!busy) { consent=p } } } }
 }
 consent?.let { p -> AlertDialog(onDismissRequest={consent=null},title={Text(stringResource(R.string.install))},text={Text(stringResource(R.string.consent))},confirmButton={TextButton(onClick={consent=null;vm.install(p)},modifier=Modifier.heightIn(min=56.dp)) { Text(stringResource(R.string.confirm)) }},dismissButton={TextButton(onClick={consent=null},modifier=Modifier.heightIn(min=56.dp)) { Text(stringResource(R.string.cancel)) }}) }
}
