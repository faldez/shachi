package com.faldez.shachi

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.faldez.shachi.data.model.ServerType
import com.faldez.shachi.data.model.ServerView
import com.faldez.shachi.ui.search.SearchFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {
    @Test
    fun testSuggestionTagsLayoutIsVisible() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setViewModelStore(ViewModelStore())
        val bundle = bundleOf("server" to ServerView(serverId = 0,
            type = ServerType.Gelbooru,
            title = "Gelbooru",
            url = "https://gelbooru.com",
            username = null,
            password = null,
            blacklistedTags = null,
            selected = false),
            "tags" to "")
        val scenario =
            launchFragmentInContainer(fragmentArgs = bundle, themeResId = R.style.Theme_Shachi) {
                SearchFragment().also { fragment ->
                    fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                        if (viewLifecycleOwner != null) {
                            // The fragment’s view has just been created
                            navController.setGraph(R.navigation.browse)
                            Navigation.setViewNavController(fragment.requireView(), navController)
                        }
                    }
                }
            }

        Espresso.onView(ViewMatchers.withId(R.id.searchTagsInputText))
            .perform(ViewActions.typeText("silver_hair"))
        Espresso.onView(ViewMatchers.withId(R.id.suggestionTagLayout))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.searchHistoryLayout))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        Espresso.onView(ViewMatchers.withId(R.id.selectedTagsLayout))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }
}