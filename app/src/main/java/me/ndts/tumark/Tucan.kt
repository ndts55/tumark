package me.ndts.tumark

import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.Jsoup

const val BaseTucanUrl = "https://www.tucan.tu-darmstadt.de"
const val UserAgent = "Mozilla/5.0"

fun retrieveGrades(tuId: String, password: String): Set<ExamEntry> {
    val (loggedInResponse, cookies) = login(tuId, password)
    return loggedInResponse // Retrieve HTML of logged in main page.
        .parse()
        .getElementById("link000324") // Retrieve link for field with text 'Modulergebnisse' or 'Module Results'.
        .child(0)
        .attr("abs:href")
        .connect()
        .cookies(cookies) // Always set our cookies before sending a request.
        .execute() // 'Click' on 'Modulergebnisse' / 'Module Results'.
        .parse()
        .getElementsByClass("nb list") // Extract data from the module results table.
        .first()
        .child(1)
        .children()
        .dropLast(1) // Remove last child because it contains the summary of all modules.
        .map {
            ExamEntry(
                identifier = it.child(0).text().trim(),
                name = it.child(1).text().trim(),
                grade = it.child(2).text().trim(),
                credits = it.child(3).text().trim(),
                status = it.child(4).text().trim(),
            )
        }
        .toSet()
}

private fun login(tuId: String, password: String): Pair<Response, Map<String, String>> {
    val responsePostLogin = requestTucanMainPage()
        .execute() // Load the main page.
        .loginActionLink() // Extract the login form submission link.
        .connect()
        .postLoginData(tuId, password) // Setup connection to post the login data.
        .execute()
    return Pair(
        if (responsePostLogin.statusCode() != 200) {
            responsePostLogin
        } else {
            responsePostLogin.requestLoggedInMainPage().execute()
        },
        responsePostLogin.cookies()
    )
}

private fun requestTucanMainPage(): Connection =
    BaseTucanUrl
        .connect()
        .get() // Retrieve page at base URL.
        .getElementsByClass("img_LangGerman") // 'Click' the first link with this class.
        .attr("abs:href") // This is really just to navigate the redirects and intermediate pages.
        .connect()
        .execute()
        .skipTucanIntermediatePage() // Skip the actual intermediate page.
        .referrer(BaseTucanUrl)
        .userAgent(UserAgent)

private fun Connection.postLoginData(tuId: String, password: String): Connection =
    // Setup connection to post the login data.
    this
        .referrer(BaseTucanUrl)
        .userAgent(UserAgent)
        .timeout(10 * 1000)
        .data(createPostData(tuId, password))
        .method(Connection.Method.POST)


private fun createPostData(tuId: String, password: String): Map<String, String> = mapOf(
    Pair("usrname", tuId),
    Pair("pass", password),
    Pair("APPNAME", "CampusNet"),
    Pair("PRGNAME", "LOGINCHECK"),
    Pair("ARGUMENTS", "clino,usrname,pass,menuno,menu_type,browser,platform"),
    Pair("clino", "000000000000001"),
    Pair("menuno", "000344"),
    Pair("menu_type", "classic"),
    Pair("browser", ""),
    Pair("platform", ""),
)

private fun Response.requestLoggedInMainPage(): Connection =
    // Create connection to request the logged in main page.
    this
        .refreshUrl()
        .connect()
        .cookies(this.cookies())
        .execute()
        .skipTucanIntermediatePage()
        .referrer(BaseTucanUrl)
        .userAgent(UserAgent)
        .cookies(this.cookies()) // Set response cookies as new request cookies as they contain the new cnsc number.

private fun String.connect(): Connection =
    Jsoup.connect(this)

private fun Response.refreshUrl(): String =
    // Construct the absolute refresh URL from the header data.
    BaseTucanUrl + this.header("REFRESH").split(";")[1].substringAfter(" URL=")

private fun Response.loginActionLink(): String =
    // Get form by id and get relative link in `action` attribute as an absolute link.
    this.parse().getElementById("cn_loginForm").attr("abs:action")

private fun Response.skipTucanIntermediatePage(): Connection =
    // Select the second link and get its relative link in `href` as an absolute link.
    this.parse().select("a")[1].attr("abs:href").connect()
