package com.example

import androidx.appcompat.app.AppCompatActivity
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse

class Streamblasters : MainAPI() {
    override var mainUrl              = "https://www.tamildhool.net/"
    override var name                 = "Tamildhool"
    override val hasMainPage          = true
    override var lang                 = "hi"
    override val hasQuickSearch       = true
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie)

     override val mainPage = mainPageOf(
        "" to "Latest",
        "vijay-tv/vijay-tv-serial/baakiyalakshmi" to "Baakiyalakshmi",
        "vijay-tv/vijay-tv-serial/siragadikka-aasai/" to "Siragadikka Aasai",
         )

      override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
    val document = app.get("$mainUrl/${request.data}/page/$page/").document

    // Target <section id="recent-posts"> and get its <article> children
    val home = document.select("section#recent-posts > article")
        .mapNotNull { it.toSearchResult() }

    return newHomePageResponse(request.name, home)
}
