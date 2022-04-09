package me.ndts.tumark

import org.jsoup.Connection
import org.jsoup.Connection.Response

const val BaseTucanUrl = "https://www.tucan.tu-darmstadt.de"
const val UserAgent = "Mozilla/5.0"

fun retrieveGrades(tuId: String, password: String): Result<Set<ExamEntry>> =
    login(tuId, password)
        .flatMap { (loggedInResponse, cookies) ->
            loggedInResponse
                .safeParse() // Retrieve HTML of logged in main page.
                .map { document -> document.getElementById("link000324") }
                .filter(Exception("unable to find module results link")) { it != null }
                .flatMap { element -> element.safeChild(0) }
                .map { element -> element.attr("abs:href") }
                .filter(Exception("module results link is empty")) { it.isNotEmpty() }
                .map { url -> url.connect().cookies(cookies) }
                .flatMap(Connection::safeExecute)
        }
        .flatMap(Response::safeParse)
        .map { document -> document.getElementsByClass("nb list").first() }
        .filter(Exception("unable to find module results table")) { it != null }
        .flatMap { element -> element.safeChild(1) }
        .map { table ->
            table
                .children()
                .dropLast(1) // Remove the last child because it contains the summary of all modules.
                .map {
                    ExamEntry(
                        identifier = it.safeChild(0).map { c -> c.text().trim() }.getOrDefault(""),
                        name = it.safeChild(1).map { c -> c.text().trim() }.getOrDefault(""),
                        grade = it.safeChild(2).map { c -> c.text().trim() }.getOrDefault(""),
                        credits = it.safeChild(3).map { c -> c.text().trim() }.getOrDefault(""),
                        status = it.safeChild(4).map { c -> c.text().trim() }.getOrDefault(""),
                    )
                }
                .toSet()
        }


private fun login(tuId: String, password: String): Result<Pair<Response, Map<String, String>>> =
    requestTucanMainPage()
        .flatMap(Connection::safeExecute) // Load the main page.
        .flatMap(Response::loginActionLink) // Extract the login form submission link.
        .map { actionLink ->
            actionLink
                .connect()
                .postLoginData(tuId, password) // Setup connection to post login data.
        }
        .flatMap(Connection::safeExecute)
        .map { responsePostLogin ->
            Pair(
                if (responsePostLogin.statusCode() != 200) {
                    responsePostLogin
                } else {
                    responsePostLogin
                        .requestLoggedInMainPage()
                        .flatMap(Connection::safeExecute)
                        .getOrDefault(responsePostLogin)
                },
                responsePostLogin.cookies()
            )
        }

private fun requestTucanMainPage(): Result<Connection> =
    BaseTucanUrl
        .connect()
        .safeGet()
        .map { document ->
            document
                .getElementsByClass("img_LangGerman") // 'Click' the first link with this class.
                .attr("abs:href") // This is really just to navigate the redirects and intermediate pages.
        }
        .filter(Exception("intermediate page link is empty")) { it.isNotEmpty() }
        .map(String::connect)
        .flatMap(Connection::safeExecute)
        .flatMap(Response::skipTucanIntermediatePage)
        .map { connection ->
            connection
                .referrer(BaseTucanUrl)
                .userAgent(UserAgent)
        }

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

private fun Response.requestLoggedInMainPage(): Result<Connection> =
    this
        .refreshUrl()
        .map { url -> url.connect().cookies(this.cookies()) }
        .flatMap(Connection::safeExecute)
        .flatMap(Response::skipTucanIntermediatePage)
        .map { connection ->
            connection
                .referrer(BaseTucanUrl)
                .userAgent(UserAgent)
                .cookies(this.cookies()) // Set response cookies as new request cookies as they contain the new cnsc number.
        }

private fun Response.refreshUrl(): Result<String> =
    try {
        Result.success(
            // Construct the absolute refresh URL from the header data.
            BaseTucanUrl + this.header("REFRESH").split(";")[1].substringAfter(" URL=")
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

private fun Response.loginActionLink(): Result<String> =
    this
        .safeParse()
        .map { document -> document.getElementById("cn_loginForm") }
        .filter(Exception("unable to find login form")) { it != null }
        .map { it.attr("abs:action") }
        .filter(Exception("action link is empty")) { it.isNotEmpty() }

private fun Response.skipTucanIntermediatePage(): Result<Connection> =
    this
        .safeParse()
        .map { it.select("a") }
        .filter(Exception("main page link not found")) { it.size > 1 }
        .map { it[1].attr("abs:href") }
        .filter(Exception("main page link is empty")) { it.isNotEmpty() }
        .map(String::connect)
