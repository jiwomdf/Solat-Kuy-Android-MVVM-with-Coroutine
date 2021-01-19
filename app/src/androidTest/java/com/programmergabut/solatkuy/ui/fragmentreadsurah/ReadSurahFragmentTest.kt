package com.programmergabut.solatkuy.ui.fragmentreadsurah

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.programmergabut.solatkuy.DummyRetValueAndroidTest
import com.programmergabut.solatkuy.R
import com.programmergabut.solatkuy.launchFragmentInHiltContainer
import com.programmergabut.solatkuy.ui.MyViewAction
import com.programmergabut.solatkuy.ui.SolatKuyFragmentFactoryAndroidTest
import com.programmergabut.solatkuy.ui.main.fragmentquran.StaredSurahAdapter
import com.programmergabut.solatkuy.ui.main.fragmentreadsurah.ReadSurahFragment
import com.programmergabut.solatkuy.ui.main.fragmentreadsurah.ReadSurahViewModel
import com.programmergabut.solatkuy.util.idlingresource.EspressoIdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class ReadSurahFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @Inject
    lateinit var fragmentFactory: SolatKuyFragmentFactoryAndroidTest

    @Before
    fun setUp() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.espressoTestIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.espressoTestIdlingResource)
    }

    @Test
    fun testVisibilityAndData(){
        var testViewModel: ReadSurahViewModel? = null
        val initData = DummyRetValueAndroidTest.fetchAllSurah<ReadSurahFragmentTest>().data.last()
        val arg = Bundle()
        arg.putString("selectedSurahId", initData.number.toString())
        arg.putString("selectedSurahName", initData.englishName)
        arg.putString("selectedTranslation", initData.englishNameTranslation)
        arg.putBoolean("isAutoScroll", false)
        launchFragmentInHiltContainer<ReadSurahFragment>(
            fragmentArgs = arg,
            fragmentFactory = fragmentFactory
        ) {
            testViewModel = viewModel
        }

        onView(withId(R.id.ab_readQuran)).check(matches(isDisplayed()))
        onView(withId(R.id.tb_readSurah)).check(matches(isDisplayed()))
        onView(withId(R.id.i_star_surah)).check(matches(isDisplayed()))
        onView(withId(R.id.rv_read_surah)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_brightness)).check(matches(isDisplayed()))
        onView(withId(R.id.tb_readSurah)).check(matches(hasDescendant(withText(testViewModel?.selectedSurahAr?.value?.data?.data?.englishName))))
    }

    @Test
    fun testScrollToTheLastAyah(){
        var testViewModel: ReadSurahViewModel? = null
        val initData = DummyRetValueAndroidTest.fetchAllSurah<ReadSurahFragmentTest>().data.last()
        val arg = Bundle()
        arg.putString("selectedSurahId", initData.number.toString())
        arg.putString("selectedSurahName", initData.englishName)
        arg.putString("selectedTranslation", initData.englishNameTranslation)
        arg.putBoolean("isAutoScroll", false)
        launchFragmentInHiltContainer<ReadSurahFragment>(
            fragmentArgs = arg,
            fragmentFactory = fragmentFactory
        ){
            testViewModel = viewModel
        }

        onView(withId(R.id.rv_read_surah)).check(matches(isDisplayed()))

        val selectedSurahAr = testViewModel!!.selectedSurahAr.value
        val totalAyah = selectedSurahAr?.data!!.data.ayahs.size - 1
        onView(withId(R.id.rv_read_surah)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<RecyclerView.ViewHolder>(totalAyah, ViewActions.scrollTo())
        )
    }

    @Test
    fun testOpenFirstSurah_thenClickFavorite_assertMsFavSurahHasChange(){
        var testViewModel: ReadSurahViewModel? = null
        val initData = DummyRetValueAndroidTest.fetchAllSurah<ReadSurahFragmentTest>().data.last()
        val arg = Bundle()
        arg.putString("selectedSurahId", initData.number.toString())
        arg.putString("selectedSurahName", initData.englishName)
        arg.putString("selectedTranslation", initData.englishNameTranslation)
        arg.putBoolean("isAutoScroll", false)
        launchFragmentInHiltContainer<ReadSurahFragment>(
            fragmentArgs = arg,
            fragmentFactory = fragmentFactory
        ){
            testViewModel = viewModel
        }

        onView(withId(R.id.i_star_surah)).check(matches(isDisplayed()))
        onView(withId(R.id.i_star_surah)).perform(click())
    }

    @Test
    fun testOpenLastSurah_thanSwipeLeftFirstAyah(){
        var testViewModel: ReadSurahViewModel? = null
        val initData = DummyRetValueAndroidTest.fetchAllSurah<ReadSurahFragmentTest>().data.last()
        val arg = Bundle()
        arg.putString("selectedSurahId", initData.number.toString())
        arg.putString("selectedSurahName", initData.englishName)
        arg.putString("selectedTranslation", initData.englishNameTranslation)
        arg.putBoolean("isAutoScroll", false)
        launchFragmentInHiltContainer<ReadSurahFragment>(
            fragmentArgs = arg,
            fragmentFactory = fragmentFactory
        ) {
            testViewModel = viewModel
        }

        onView(withId(R.id.rv_read_surah)).check(matches(isDisplayed()))
        onView(withId(R.id.rv_read_surah)).perform(
            RecyclerViewActions
                .actionOnItemAtPosition<RecyclerView.ViewHolder>(0, swipeLeft())
        )
    }

    @Test
    fun testLikeAllAnNaasAyah(){
        var testViewModel: ReadSurahViewModel? = null
        val initData = DummyRetValueAndroidTest.fetchAllSurah<ReadSurahFragmentTest>().data.last()
        val arg = Bundle()
        arg.putString("selectedSurahId", initData.number.toString())
        arg.putString("selectedSurahName", initData.englishName)
        arg.putString("selectedTranslation", initData.englishNameTranslation)
        arg.putBoolean("isAutoScroll", false)
        launchFragmentInHiltContainer<ReadSurahFragment>(
            fragmentArgs = arg,
            fragmentFactory = fragmentFactory
        ) {
            testViewModel = viewModel
        }

        onView(withId(R.id.rv_read_surah)).check(matches(isDisplayed()))
        for (i in testViewModel!!.selectedSurahAr.value!!.data!!.data.ayahs.indices){
            onView(withId(R.id.rv_read_surah)).perform(
                RecyclerViewActions.actionOnItemAtPosition<StaredSurahAdapter.StaredSurahViewHolder>(
                    i,
                    MyViewAction.clickChildViewWithId(R.id.iv_listFav_fav)
                )
            )
        }
        testViewModel?.msFavAyahBySurahID?.value?.data?.forEachIndexed { index, data ->
            assertEquals(data.ayahID, (index + 1))
        }
    }

}