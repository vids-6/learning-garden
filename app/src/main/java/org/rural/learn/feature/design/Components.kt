package org.rural.learn.feature.design
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import org.rural.learn.R
@Composable fun Action(text: String, enabled: Boolean=true, onClick: () -> Unit) { Button(onClick=onClick,enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)) { Text(text) } }
@Composable fun AudioButton(onClick: () -> Unit) { OutlinedButton(onClick=onClick,modifier=Modifier.heightIn(min=56.dp)) { Icon(painterResource(R.drawable.ic_audio),contentDescription=stringResource(R.string.audio)); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.audio)) } }
@Composable fun Plant(stage: Int) {
 val label=stringResource(listOf(R.string.seed,R.string.sprout,R.string.seedling,R.string.plant,R.string.flowering,R.string.fruit_tree)[stage.coerceIn(0,5)])
 Canvas(Modifier.fillMaxWidth().height(130.dp).semantics { contentDescription=label }) {
  val x=size.width/2; val ground=size.height*.88f; val green=Color(0xFF245D42)
  drawLine(Color(0xFF75501A),Offset(x-55,ground),Offset(x+55,ground),8f)
  if(stage==0) drawOval(Color(0xFF75501A),topLeft=Offset(x-12,ground-24),size=androidx.compose.ui.geometry.Size(24f,18f))
  else { val h=(30+stage*20).dp.toPx().coerceAtMost(size.height*.75f); drawLine(green,Offset(x,ground),Offset(x,ground-h),8f)
   repeat(stage+1) { i -> val y=ground-h*(i+1)/(stage+2); val dx=if(i%2==0) 35f else -35f; drawLine(green,Offset(x,y),Offset(x+dx,y-20),12f); drawCircle(green,15f,Offset(x+dx,y-20)) }
   if(stage>=4) { drawCircle(Color(0xFF75501A),16f,Offset(x,ground-h)); if(stage==5) { drawCircle(Color(0xFF9B2424),12f,Offset(x+40,ground-h/2)); drawCircle(Color(0xFF9B2424),12f,Offset(x-38,ground-h*.7f)) } }
  }
 }
 Text(label,style=MaterialTheme.typography.titleMedium)
}
fun badgeLabel(id: String): Int = when(id) { "FIRST_STEPS"->R.string.first_steps; "PERFECT_ROUND"->R.string.perfect_round; "COMEBACK"->R.string.comeback; "UNIT_MASTER"->R.string.unit_master; "EXPLORER"->R.string.explorer; "PRACTICE_WEEK"->R.string.practice_week; "LISTENER"->R.string.listener; else->R.string.bilingual }
