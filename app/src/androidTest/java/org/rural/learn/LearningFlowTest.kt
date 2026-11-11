package org.rural.learn

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearningFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun resetOnboarding() {
        runBlocking {
            val c = (compose.activity.application as GardenApp).container
            c.settings.learner("")
            c.settings.language("")
            c.settings.scale(1.5f)
        }
    }

    private fun label(id: Int) = compose.activity.getString(id)

    private fun waitFor(text: String) {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun tap(text: String) {
        waitFor(text)
        compose.onNodeWithText(text).performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun finishMathLesson(left: List<String>) {
        waitFor(label(R.string.start))
        compose.onAllNodesWithText(label(R.string.start))[0].performScrollTo().performClick()
        tap(label(R.string.begin_quiz))
        tap("5")
        tap(label(R.string.next))
        tap("3")
        tap(label(R.string.next))
        tap(left[0])
        tap("1")
        tap(left[1])
        tap("2")
        tap(label(R.string.next))
        tap("6")
        tap(label(R.string.check))
        tap(label(R.string.next))
        tap("9")
        tap(label(R.string.next))
        waitFor(label(R.string.result))
        compose.onNodeWithText("${label(R.string.stars)}: 3 / 3").assertExists()
        compose.activityRule.scenario.recreate()
        waitFor(label(R.string.result))
        compose.onNodeWithText(label(R.string.home)).performClick()
        waitFor("Asha")
    }

    @Test fun allFourActivityTypesInAllThreeLanguagesAtLargeText() {
        tap("English")
        waitFor(label(R.string.name))
        compose.onNodeWithText(label(R.string.name)).performTextInput("Asha")
        tap(label(R.string.create))
        finishMathLesson(listOf("one seed", "two cows"))

        tap(label(R.string.settings))
        tap("हिन्दी")
        waitFor("सेटिंग्स")
        compose.onNodeWithText(label(R.string.home)).performClick()
        finishMathLesson(listOf("एक बीज", "दो गायें"))

        tap(label(R.string.settings))
        tap("मराठी")
        waitFor("सेटिंग्ज")
        compose.onNodeWithText(label(R.string.home)).performClick()
        finishMathLesson(listOf("एक बी", "दोन गायी"))

        runBlocking {
            val c = (compose.activity.application as GardenApp).container
            val id = c.settings.flow.first().activeLearnerId
            assertEquals(240, c.learning.xp.first().filter { it.learnerId == id }.sumOf { it.amount })
            assertTrue(c.learning.badges.first().any { it.learnerId == id && it.badgeId == "BILINGUAL" })
        }
    }
}
