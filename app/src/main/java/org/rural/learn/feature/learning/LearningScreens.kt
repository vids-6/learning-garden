package org.rural.learn.feature.learning
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import org.rural.learn.R
import org.rural.learn.feature.GardenViewModel
import org.rural.learn.feature.design.*
import org.rural.learn.domain.content.*
import org.rural.learn.domain.gamification.*
@Composable fun LanguageScreen(onSelect: (String)->Unit, onAudio: (String,String)->Unit) {
 Text(stringResource(R.string.choose_language),style=MaterialTheme.typography.headlineMedium)
 listOf("en" to "English","hi" to "हिन्दी","mr" to "मराठी").forEach { (code,name) -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) { Button(onClick={onSelect(code)},modifier=Modifier.weight(1f).heightIn(min=56.dp)) { Text(name) }; AudioButton { onAudio(name,code) } } }
}
@Composable fun Profiles(vm: GardenViewModel) {
 val learners by vm.learners.collectAsState(); val busy by vm.busy.collectAsState()
 Text(stringResource(R.string.profiles),style=MaterialTheme.typography.headlineMedium)
 learners.forEach { Action("${listOf("☀","❀","★","☘")[it.avatarId.coerceIn(0,3)]}  ${it.name}",!busy) { vm.select(it.id) } }
 var name by rememberSaveable { mutableStateOf("") }; var avatar by rememberSaveable { mutableIntStateOf(0) }
 OutlinedTextField(name,{name=it.take(24)},label={Text(stringResource(R.string.name))},singleLine=true,modifier=Modifier.fillMaxWidth())
 Text(stringResource(R.string.choose_avatar))
 Row { listOf("☀","❀","★","☘").forEachIndexed { index,s -> OutlinedButton(onClick={avatar=index},modifier=Modifier.weight(1f).heightIn(min=56.dp).semantics { selected=avatar==index }) { Text(if(avatar==index) "✓ $s" else s) } } }
 Action(stringResource(R.string.create),name.isNotBlank() && !busy) { vm.create(name,avatar) }
}
@Composable fun Home(vm: GardenViewModel,onLesson: (Lesson)->Unit) {
 val settings by vm.settings.collectAsState(); val learners by vm.learners.collectAsState(); val xp by vm.xp.collectAsState(); val days by vm.days.collectAsState(); val subjects by vm.subjects.collectAsState(); val progress by vm.progress.collectAsState(); val run by vm.run.collectAsState()
 val id=settings.activeLearnerId; val level=LearningRules.level(xp.filter { it.learnerId==id }.sumOf { it.amount })
 Text(learners.find { it.id==id }?.name ?: "",style=MaterialTheme.typography.headlineMedium)
 Text(stringResource(R.string.offline)); Plant(level.stage)
 Text("${stringResource(R.string.level)} ${level.number} • ${level.within}/${level.threshold} ${stringResource(R.string.xp)}")
 LinearProgressIndicator(progress={level.within.toFloat()/level.threshold},modifier=Modifier.fillMaxWidth().height(8.dp))
 val now=System.currentTimeMillis(); val today=Math.floorDiv(now+java.util.TimeZone.getDefault().getOffset(now),86400000L); val c=java.util.Calendar.getInstance(); val start=today-((c.get(java.util.Calendar.DAY_OF_WEEK)+5)%7)
 Text("${stringResource(R.string.weekly)}: ${days.count { it.learnerId==id && it.epochDay in start..today }} / 7")
 if(subjects.isEmpty()) Text(stringResource(R.string.empty))
 subjects.forEach { subject -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
  Text(subject.title,style=MaterialTheme.typography.headlineSmall)
  subject.units.flatMap { it.lessons }.forEachIndexed { index,l -> val lessons=subject.units.flatMap { it.lessons }; val p=progress.find { it.learnerId==id && it.lessonId==l.id }; val prev=if(index==0) 1 else progress.find { it.learnerId==id && it.lessonId==lessons[index-1].id }?.stars ?: 0
   val unlocked=LearningRules.unlocked(index,prev)
   Text(l.objective,style=MaterialTheme.typography.titleMedium)
   if(p!=null) Text("${stringResource(R.string.stars)}: ${p.stars} / 3")
   if(!unlocked) Text(stringResource(R.string.locked))
   Action(stringResource(if(run?.lessonId==l.id && run?.complete==false) R.string.continue_lesson else R.string.start),unlocked) { onLesson(l) }
  }
 } } }
}
@Composable fun LessonScreen(vm: GardenViewModel,onAudio: (String,String?)->Unit,onSurvey: @Composable ()->Unit = {}) {
 val state by vm.run.collectAsState(); val busy by vm.busy.collectAsState(); val s=state; val lesson=vm.lesson
 if(s==null || lesson==null) { Text(stringResource(R.string.loading)); return }
 LaunchedEffect(s.index,s.complete) { if(s.index==lesson.activities.size && !s.complete) vm.restoreCompletion() }
 Text(lesson.objective,style=MaterialTheme.typography.headlineSmall)
 when {
  s.complete -> { Text(stringResource(R.string.result),style=MaterialTheme.typography.headlineMedium); val score=LearningRules.percent(s.correctCount,lesson.activities.size); Text("${stringResource(R.string.stars)}: ${LearningRules.stars(score)} / 3"); Text("${stringResource(R.string.score)}: $score% • +${s.resultXp} ${stringResource(R.string.xp)}"); s.resultBadges.forEach { Text("★ ${stringResource(badgeLabel(it))}") }; onSurvey(); Action(stringResource(R.string.practice_again),!busy) { vm.replay() } }
  !s.cardsDone -> { lesson.cards.forEach { c -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(when(c.image) { "ic_math"->R.drawable.ic_math; "ic_vocab"->R.drawable.ic_vocab; else->R.drawable.ic_lifeskills }),contentDescription=c.text,modifier=Modifier.size(96.dp)); Text(c.text,style=MaterialTheme.typography.titleLarge); AudioButton { onAudio(c.text,c.audio) } } } }; Action(stringResource(R.string.begin_quiz),!busy) { vm.cardsDone() } }
  s.index<lesson.activities.size -> { val a=lesson.activities[s.index]; Text("${s.index+1} / ${lesson.activities.size}"); key(s.runId,s.index,s.records.size) { ActivityView(a,s,busy,{vm.answer(it)},onAudio) }; if(s.feedback) { val record=s.records.last(); Text(stringResource(if(record.correct) R.string.correct else if(record.resolved) R.string.answer_reveal else R.string.incorrect_retry),modifier=Modifier.semantics { liveRegion=LiveRegionMode.Polite },style=MaterialTheme.typography.titleLarge)
   if(record.resolved) { val answer=when(a.type) { "NUMBER_ENTRY"->a.answer.toString(); "MATCH_PAIRS"->a.pairs.joinToString("; ") { "${it.left} → ${it.right}" }; else->a.options.getOrNull(a.correctIndex ?: -1)?.text ?: "" }; Text(answer); Text(a.explanation); AudioButton { onAudio(a.explanation,null) } }
   Action(stringResource(if(record.resolved) R.string.next else R.string.retry),!busy) { vm.next() }
  } }
 }
}
@Composable private fun ActivityView(a: Activity,s: RunState,busy: Boolean,onAnswer: (String)->Unit,onAudio: (String,String?)->Unit) {
 val enabled=!busy && !s.feedback
 Text(a.prompt,style=MaterialTheme.typography.titleLarge); AudioButton { onAudio(a.prompt,a.promptAudio) }
 when(a.type) {
  "MCQ","LISTEN_PICK" -> a.options.forEachIndexed { index,o -> Action(o.text,enabled) { onAnswer(index.toString()) } }
  "NUMBER_ENTRY" -> { var digits by rememberSaveable { mutableStateOf("") }; Text(digits.ifEmpty { "—" },style=MaterialTheme.typography.headlineLarge); listOf(listOf("1","2","3"),listOf("4","5","6"),listOf("7","8","9"),listOf("0")).forEach { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) { row.forEach { digit -> Button(onClick={if(digits.length<6) digits+=digit},enabled=enabled,modifier=Modifier.weight(1f).heightIn(min=56.dp)) { Text(digit) } } } }; Action(stringResource(R.string.clear_number),enabled) { digits="" }; Action(stringResource(R.string.check),enabled && digits.isNotEmpty()) { onAnswer(digits) } }
  "MATCH_PAIRS" -> { Text(stringResource(R.string.match_help)); var left by remember { mutableStateOf<String?>(null) }; var matched by remember { mutableStateOf(setOf<String>()) }; var wrong by remember { mutableStateOf(false) }
   a.pairs.forEachIndexed { index,p -> val right=a.pairs.reversed()[index]; Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
    OutlinedButton(onClick={left=p.id;wrong=false},enabled=enabled && p.id !in matched,modifier=Modifier.weight(1f).heightIn(min=56.dp).semantics { selected=left==p.id }) { Text(if(p.id in matched) "✓ ${p.left}" else if(left==p.id) "→ ${p.left}" else p.left) }
    OutlinedButton(onClick={if(left==right.id) { matched=matched+right.id;left=null;wrong=false; if(matched.size==a.pairs.size) onAnswer(matched.sorted().joinToString(",")) } else { wrong=true; onAnswer("wrong") }},enabled=enabled && right.id !in matched && left!=null,modifier=Modifier.weight(1f).heightIn(min=56.dp)) { Text(if(right.id in matched) "✓ ${right.right}" else right.right) }
   } }; if(wrong) Text(stringResource(R.string.incorrect_retry))
  }
 }
}
