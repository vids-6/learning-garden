package org.rural.learn.feature.research
import android.content.Intent
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.rural.learn.R
import org.rural.learn.feature.GardenViewModel
import org.rural.learn.feature.design.*
import org.rural.learn.domain.analytics.Research
@Composable fun Survey(vm: GardenViewModel,onAudio: (String)->Unit) {
 val run by vm.run.collectAsState(); val busy by vm.busy.collectAsState(); val state=run ?: return
 if(state.surveyDone) return
 var answers by rememberSaveable(state.runId) { mutableStateOf(listOf(-1,-1,-1)) }; var clarity by rememberSaveable(state.runId) { mutableIntStateOf(-1) }
 val spokenQuestions=listOf(stringResource(R.string.survey_q1),stringResource(R.string.survey_q2),stringResource(R.string.survey_q3)).joinToString(". ")
 LaunchedEffect(state.runId) { onAudio(spokenQuestions) }
 Text(stringResource(R.string.survey),style=MaterialTheme.typography.titleLarge)
 listOf(R.string.survey_q1,R.string.survey_q2,R.string.survey_q3).forEachIndexed { index,res ->
  val question=stringResource(res); Text(question); AudioButton { onAudio(question) }
  listOf(2 to R.string.happy,1 to R.string.neutral,0 to R.string.sad).forEach { (v,label) ->
   val emoji=when(v) { 2->"☺"; 1->"😐"; else->"☹" }
   Action("${if(answers[index]==v) "✓ " else ""}$emoji ${stringResource(label)}",!busy) { answers=answers.mapIndexed { i,old -> if(i==index) v else old } }
 }
 }
 Text(stringResource(R.string.clarity))
 Action("${if(clarity==1) "✓ " else ""}${stringResource(R.string.clear)}",!busy) { clarity=1 }
 Action("${if(clarity==0) "✓ " else ""}${stringResource(R.string.unclear)}",!busy) { clarity=0 }
 Action(stringResource(R.string.survey_done),!busy && answers.all { it>=0 } && clarity>=0) { vm.feedback(answers,clarity==1) }
}
@Composable fun ResearchScreen(vm: GardenViewModel) {
 // Gate is deliberately not saveable; leaving this destination locks it again.
 var unlocked by remember { mutableStateOf(false) }; var pin by remember { mutableStateOf("") }; var invalid by remember { mutableStateOf(false) }
 val events by vm.events.collectAsState(); val context=LocalContext.current; val scope=rememberCoroutineScope(); var saved by remember { mutableStateOf(false) }; var failed by remember { mutableStateOf(false) }
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
  if(result.resultCode==Activity.RESULT_OK) result.data?.data?.let { uri -> scope.launch { runCatching { withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(Research.csv(events)) } ?: error("No output") } }.onSuccess { saved=true }.onFailure { failed=true } } }
 }
 Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
  Text(stringResource(R.string.research),style=MaterialTheme.typography.headlineMedium)
  if(!unlocked) {
   OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(4)},label={Text(stringResource(R.string.pin))},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),singleLine=true)
   if(invalid) Text(stringResource(R.string.wrong_pin))
   Action(stringResource(R.string.unlock),pin.length==4) { unlocked=pin=="2468";invalid=!unlocked;pin="" }
  } else {
   Text(stringResource(R.string.anonymous))
   val m=remember(events) { Research.metrics(events) }
   listOf(R.string.completion_metric to "${m.completionPercent}%",R.string.time_metric to "${m.averageSeconds}",R.string.errors_metric to "${m.navigationErrors}",R.string.first_metric to "${m.firstScore}%",R.string.repeat_metric to "${m.repeatScore}%",R.string.survey_metric to "${m.surveyAverage}").forEach { (label,value) -> Text("${stringResource(label)}: $value") }
   Action(stringResource(R.string.export)) { launcher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE);type="text/csv";putExtra(Intent.EXTRA_TITLE,"learning-garden-events.csv") }) }
   if(saved) Text(stringResource(R.string.exported));if(failed) Text(stringResource(R.string.error))
  }
 }
}
