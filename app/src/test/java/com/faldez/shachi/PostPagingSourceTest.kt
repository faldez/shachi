package com.faldez.shachi

import androidx.paging.PagingSource
import com.faldez.shachi.data.api.*
import com.faldez.shachi.data.model.ServerType
import com.faldez.shachi.data.model.ServerView
import com.faldez.shachi.data.model.response.GelbooruPost
import com.faldez.shachi.data.model.response.GelbooruPostResponse
import com.faldez.shachi.data.model.response.GelbooruPosts
import com.faldez.shachi.data.model.response.mapToPost
import com.faldez.shachi.data.repository.post.PostPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class PostPagingSourceTest {
    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    lateinit var gelbooruApi: GelbooruApi

    @Mock
    lateinit var danbooruApi: DanbooruApi

    @Mock
    lateinit var moebooruApi: MoebooruApi

    class BooruApiTest(
        override val gelbooru: GelbooruApi,
        override val moebooru: MoebooruApi,
        override val danbooru: DanbooruApi,
    ) : BooruApi {}

    @Mock
    lateinit var booruApi: BooruApi

    lateinit var postPagingSource: PostPagingSource

    companion object {
        val server = ServerView(serverId = 0,
            type = ServerType.Gelbooru,
            title = "Testbooru",
            url = "https://testbooru.com",
            username = null,
            password = null,
            blacklistedTags = null,
            selected = false)
        val gelbooruResponse = GelbooruPostResponse(
            GelbooruPosts(
                post = listOf(
                    GelbooruPost(
                        height = 900,
                        width = 1260,
                        score = null,
                        fileUrl = "https://img3.gelbooru.com/images/9d/a7/9da765664d5a897a24ca5cb67e4e307b.jpg",
                        parentId = null,
                        sampleUrl = null,
                        sampleWidth = 0,
                        sampleHeight = 0,
                        previewUrl = "https://img3.gelbooru.com/thumbnails/9d/a7/thumbnail_9da765664d5a897a24ca5cb67e4e307b.jpg",
                        previewWidth = null,
                        previewHeight = null,
                        rating = "safe",
                        tags = "1girl bangs bikini black_bikini black_hair blue_archive blue_eyes blue_hairband blue_ribbon blush book breasts closed_mouth crossed_bangs eyebrows_visible_through_hair hair_between_eyes hair_ribbon hairband highres holding holding_book jewelry konnyaku_(kk-monmon) long_hair low_twintails necklace ribbon signature simple_background small_breasts solo sweat swimsuit twintails ui_(blue_archive) very_long_hair wavy_mouth white_background",
                        id = 6984563,
                        change = 0,
                        md5 = "9da765664d5a897a24ca5cb67e4e307b",
                        creatorId = 6498,
                        hasChildren = false,
                        createdAt = null,//ZonedDateTime.from(DateTimeFormatter.ofPattern("eee MMM d HH:mm:ss Z yyyy")
                        //.parse("Mon Feb 28 00:30:41 -0600 2022")),
                        status = "active",
                        source = "https://twitter.com/konnyaksankaku/status/1498108188322713602",
                        hasNotes = false,
                        hasComments = false)
                ),
                count = 1,
                offset = 0)
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        MockitoAnnotations.openMocks(this)
        booruApi = BooruApiTest(gelbooru = gelbooruApi,
            danbooru = danbooruApi,
            moebooru = moebooruApi)
        postPagingSource =
            PostPagingSource(Action.SearchPost(server = server, tags = ""), booruApi)
    }

    @Test
    fun loadReturnsPostWhenOnSuccessfulLoad() = runBlockingTest {
        given(booruApi.gelbooru.getPosts(anyString())).willReturn(gelbooruResponse)

        val expectedResult = PagingSource.LoadResult.Page(
            data = gelbooruResponse.mapToPost(server.toServer()) ?: listOf(),
            prevKey = null,
            nextKey = 1
        )

        Assert.assertEquals(expectedResult, postPagingSource.load(PagingSource.LoadParams.Refresh(
            key = 0,
            loadSize = 1,
            placeholdersEnabled = true
        )))
    }
}