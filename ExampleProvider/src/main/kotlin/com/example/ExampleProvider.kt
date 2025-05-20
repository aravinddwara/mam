package com.lagradost.cloudstream3.tamilproviders

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.Qualities
import org.jsoup.nodes.Element
import java.util.*

class TamilDhoolProvider : MainAPI() {
    override var mainUrl = "https://www.tamildhool.net"
    override var name = "TamilDhool"
    override val hasMainPage = true
    override var lang = "ta"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.TvSeries,
        TvType.Movie
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val homePageList = ArrayList<HomePageList>()

        // Featured Section
        val featuredShows = document.select("div.category-posts div.item")
        if (featuredShows.isNotEmpty()) {
            val homeFeatured = featuredShows.mapNotNull { show ->
                getSearchResultFromElement(show)
            }
            if (homeFeatured.isNotEmpty()) {
                homePageList.add(HomePageList("Featured Shows", homeFeatured))
            }
        }

        // Recent Episodes Section
        val recentEpisodes = document.select("div.recent-episodes div.item")
        if (recentEpisodes.isNotEmpty()) {
            val homeRecent = recentEpisodes.mapNotNull { episode ->
                getSearchResultFromElement(episode)
            }
            if (homeRecent.isNotEmpty()) {
                homePageList.add(HomePageList("Recent Episodes", homeRecent))
            }
        }

        // Categories
        document.select("div.categories-block").forEach { block ->
            val categoryTitle = block.select("h3.heading").text().trim()
            val shows = block.select("div.item").mapNotNull { show ->
                getSearchResultFromElement(show)
            }
            if (shows.isNotEmpty()) {
                homePageList.add(HomePageList(categoryTitle, shows))
            }
        }

        return HomePageResponse(homePageList)
    }

    private fun getSearchResultFromElement(element: Element): SearchResponse? {
        val title = element.selectFirst("h3.title a, div.title a")?.text()?.trim() ?: return null
        val href = element.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = element.selectFirst("img")?.attr("src")

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document

        return document.select("div.search-results div.item").mapNotNull { result ->
            getSearchResultFromElement(result)
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title = document.selectFirst("h1.title")?.text()?.trim() ?: return null
        val poster = document.selectFirst("div.poster img")?.attr("src")
        val plot = document.selectFirst("div.description")?.text()?.trim()
        
        val yearRegex = Regex("\\(([0-9]{4})\\)")
        val yearMatch = yearRegex.find(title)
        val year = yearMatch?.groupValues?.get(1)?.toIntOrNull()

        val tvType = if (url.contains("/movie/")) TvType.Movie else TvType.TvSeries

        if (tvType == TvType.Movie) {
            val videoLinks = document.select("div.video-links a").mapNotNull { link ->
                Pair(link.text().trim(), link.attr("href"))
            }

            return newMovieLoadResponse(title, url, TvType.Movie, videoLinks.firstOrNull()?.second ?: "") {
                this.posterUrl = poster
                this.year = year
                this.plot = plot
            }
        } else {
            val episodes = document.select("div.episodes-list div.episode").map { episode ->
                val epTitle = episode.selectFirst("h3.title")?.text()?.trim() ?: "Episode"
                val epHref = episode.selectFirst("a")?.attr("href") ?: ""
                val epThumb = episode.selectFirst("img")?.attr("src")
                val epNum = episode.selectFirst("span.episode-number")?.text()?.toIntOrNull() ?: 0
                val epDate = episode.selectFirst("span.date")?.text()?.trim()

                Episode(
                    epHref,
                    epTitle,
                    season = 1,
                    episode = epNum,
                    posterUrl = epThumb,
                    date = epDate
                )
            }

            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.year = year
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        // Direct video sources
        document.select("video source").forEach { source ->
            val url = source.attr("src")
            if (url.isNotEmpty()) {
                callback.invoke(
                    ExtractorLink(
                        this.name,
                        this.name,
                        url,
                        this.mainUrl,
                        Qualities.Unknown.value,
                        isM3u8 = url.contains(".m3u8")
                    )
                )
            }
        }

        // Iframe sources
        document.select("iframe").forEach { iframe ->
            val embedUrl = iframe.attr("src")
            if (embedUrl.isNotEmpty()) {
                loadExtractor(embedUrl, data, subtitleCallback, callback)
            }
        }

        // Other players
        document.select("div.player a").forEach { player ->
            val playerUrl = player.attr("href")
            if (playerUrl.isNotEmpty()) {
                loadExtractor(playerUrl, data, subtitleCallback, callback)
            }
        }

        return true
    }
}
