package org.rural.learn.feature
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.rural.learn.AppContainer
import org.rural.learn.domain.*
import org.rural.learn.domain.content.*
import org.rural.learn.domain.gamification.*
class GardenViewModel(private val container: AppContainer): ViewModel() {
 private val repo=container.learning
 val settings=container.settings.flow.stateIn(viewModelScope,SharingStarted.Eagerly,Settings())
 val learners=repo.profiles.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val progress=repo.progress.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val xp=repo.xp.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val badges=repo.badges.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val days=repo.days.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val events=repo.events.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val installed=repo.installed.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val subjects=MutableStateFlow<List<Subject>>(emptyList())
 val packs=MutableStateFlow<List<Pack>>(emptyList())
 val storage=MutableStateFlow(0L)
 val run=MutableStateFlow<RunState?>(null)
 val error=MutableStateFlow("")
 val ready=MutableStateFlow(false)
 val busy=MutableStateFlow(false)
 val learner get()=settings.value.activeLearnerId
 val lesson get()=subjects.value.flatMap { it.units }.flatMap { it.lessons }.find { it.id==run.value?.lessonId }
 init {
  viewModelScope.launch { container.settings.flow.collect { s ->
   runCatching { if(s.language.isNotEmpty()) subjects.value=container.content.subjects(s.language); packs.value=container.content.packs(); storage.value=container.packs.storageBytes(); run.value=if(s.activeLearnerId.isNotEmpty()) repo.session(s.activeLearnerId) else null; ready.value=true }.onFailure { error.value=it.message ?: "Error"; ready.value=true }
  } }
 }
 private fun work(block: suspend () -> Unit) { if(busy.value) return; busy.value=true; viewModelScope.launch { try { block() } catch(e: kotlinx.coroutines.CancellationException) { throw e } catch(e: Exception) { error.value=e.message ?: "Error" } finally { busy.value=false } } }
 fun language(code: String)=work { container.settings.language(code); if(learner.isNotEmpty()) repo.profileLanguage(learner,code) else repo.log(learner,"language_selected",mapOf("lang" to code)) }
 fun select(id: String)=work { container.settings.learner(id); learners.value.find { it.id==id }?.let { container.settings.language(it.languageCode) } }
 fun create(name: String,avatar: Int)=work { container.settings.learner(repo.create(name,avatar,settings.value.language)) }
 fun start(lesson: Lesson)=work { val current=repo.session(learner); run.value=if(current?.lessonId==lesson.id && !current.complete) current else repo.start(learner,lesson,settings.value.language) }
 fun replay()=work { lesson?.let { run.value=repo.start(learner,it,settings.value.language) } }
 fun cardsDone()=work { run.value?.let { val next=it.copy(cardsDone=true); repo.save(learner,next); run.value=next } }
 fun answer(choice: String)=work { val s=run.value ?: return@work; val l=lesson ?: return@work; if(choice=="wrong") repo.log(learner,"wrong_tap",mapOf("screen" to "lesson","reason" to "pair_mismatch")); run.value=repo.answer(learner,s,l,choice) }
 fun next()=work { val s=run.value ?: return@work; val l=lesson ?: return@work; val next=LearningRules.next(s,System.currentTimeMillis()); repo.save(learner,next); run.value=if(next.index==l.activities.size) repo.finish(learner,next,l,subjects.value) else next }
 fun restoreCompletion()=work { val s=run.value ?: return@work; val l=lesson ?: return@work; if(!s.complete && s.index==l.activities.size) run.value=repo.finish(learner,s,l,subjects.value) }
 fun reload()=work { subjects.value=container.content.subjects(settings.value.language); packs.value=container.content.packs(); error.value="" }
 fun install(pack: Pack)=work { container.packs.install(pack); storage.value=container.packs.storageBytes() }
 fun feedback(answers: List<Int>,clarity: Boolean)=work { run.value?.let { run.value=repo.feedback(learner,it,answers,clarity) } }
 fun sync() { container.sync.schedule(); viewModelScope.launch { container.settings.syncStatus("running") } }
 fun boolean(key: String,value: Boolean)=work { container.settings.boolean(key,value) }
 fun scale(value: Float)=work { container.settings.scale(value) }
 fun log(type: String,payload: Map<String,String> = emptyMap()) { viewModelScope.launch { repo.log(learner,type,payload) } }
}
