@file:Suppress("EnumValuesSoftDeprecate")

package com.github.panpf.zoomimage.sample.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_debug
import com.github.panpf.zoomimage.sample.resources.ic_pexels
import com.github.panpf.zoomimage.sample.resources.ic_phone
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.gallery.LocalPhotoListPage
import com.github.panpf.zoomimage.sample.ui.gallery.PexelsPhotoListPage
import com.github.panpf.zoomimage.sample.ui.test.TestPage
import com.github.panpf.zoomimage.sample.util.isMobile
import com.github.panpf.zoomimage.sample.util.runtimePlatformInstance
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

val gridCellsMinSize: Dp = if (runtimePlatformInstance.isMobile()) 100.dp else 150.dp

@Composable
expect fun HomeHeader()

enum class HomeTab(
    val title: String,
    val icon: DrawableResource,
    val content: @Composable Screen.() -> Unit
) {
    LOCAL("Local", Res.drawable.ic_phone, { LocalPhotoListPage() }),
    PEXELS("Pexels", Res.drawable.ic_pexels, { PexelsPhotoListPage() }),
    TEST("Test", Res.drawable.ic_debug, { TestPage() }),
}

object VerHomeScreen : BaseScreen() {

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    override fun DrawContent() {
        Column(Modifier.fillMaxSize()) {
            HomeHeader()

            val coroutineScope = rememberCoroutineScope()
            val context = LocalPlatformContext.current
            val appSettings = context.appSettings
            val homeTabs = remember { HomeTab.values() }

            val pagerState = rememberPagerState(
                initialPage = appSettings.currentPageIndex.value.coerceIn(0, homeTabs.size - 1),
                pageCount = { homeTabs.size }
            )
            LaunchedEffect(Unit) {
                snapshotFlow { pagerState.currentPage }.collect { index ->
                    appSettings.currentPageIndex.value = index
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().weight(1f),
            ) { pageIndex ->
                homeTabs[pageIndex].content.invoke(this@VerHomeScreen)
            }

            NavigationBar(Modifier.fillMaxWidth()) {
                homeTabs.forEachIndexed { index, homeTab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(homeTab.icon),
                                contentDescription = homeTab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(homeTab.title) },
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.scrollToPage(index) } }
                    )
                }
            }
        }
    }
}