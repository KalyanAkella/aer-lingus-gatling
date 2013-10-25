package com.aerlingus

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import scala.concurrent.duration._
import bootstrap._
import assertions._

class BasicExampleSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://www.aerlingus.com")
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
//    .disableFollowRedirect

  val headers_1 = Map(
    "Cache-Control" -> "max-age=0",
    "Connection" -> "keep-alive"
  )

  val headers_2 = Map(
    "Connection" -> "keep-alive",
    "Content-Length" -> "373",
    "Content-Type" -> "application/x-www-form-urlencoded",
    "Referer" -> "http://www.aerlingus.com/en-US/home/index.jsp"
  )

  val headers_3 = Map(
    "Connection" -> "keep-alive",
    "Content-Type" -> "application/x-www-form-urlencoded",
    "Referer" -> "http://www.aerlingus.com/cgi-bin/obel01im1/bookonline/chooseFlight.jsp"
  )

  val headers_4 = Map(
    "Connection" -> "keep-alive",
    "Content-Type" -> "application/x-www-form-urlencoded",
    "Referer" -> "http://www.aerlingus.com/cgi-bin/obel01im1/bookonline/flexibleSearchSchedule.do"
  )

  val headers_5 = Map(
    "Connection" -> "keep-alive",
    "Content-Type" -> "application/x-www-form-urlencoded",
    "Referer" -> "http://www.aerlingus.com/cgi-bin/obel01im1/bookonline/flightScheduleLookupDispatchAction.do"
  )

  val scn = scenario("Book a flight")
    .group("Search for a journey") {
      exec(
        http("retrieve search form")
          .get("/en-US/home/index.jsp")
          .headers(headers_1))
        .pause(3, 5).
      exec(
        http("perform flight search")
          .post("/cgi-bin/obel01im1/bookonline/chooseFlight.jsp")
          .param("selectedFlightType", "RETURN")
          .param("depart", "Manchester+%28MAN%29%2C+United+Kingdom")
          .param("selectedSourceAirport_1", "MAN")
          .param("destination", "Dublin+%28DUB%29%2C+Ireland")
          .param("selectedDestinationAirport_1", "DUB")
          .param("selectedDay_1", "11")
          .param("selectedMonth_1", "10")
          .param("selectedDay_2", "22")
          .param("selectedMonth_2", "10")
          .param("selectedSearchType", "FLEXIBLE")
          .param("selectedAdultNumber", "1")
          .param("selectedChildrenNumber", "0")
          .param("selectedInfantNumber", "0")
          .param("promoCode", "")
          .param("methodToUse", "Book+Now")
          .headers(headers_2)
          .check(status.is(200)))
//          .check(xpath("""//*[@name="workflowToken"]/@value""").find(0).saveAs("workflowToken")))
//          .check(css("""input[name="workflowToken"]""", "value").find(0).saveAs("workflowToken")))
//          .check(status.is(302))
//          .check(regex("""<input type="hidden" name="workflowToken" value="([a-f0-9]+)">""").find.saveAs("workflowToken")))
        .pause(1, 3)
    }.exec(
      http("select dates")
        .post("/cgi-bin/obel01im1/bookonline/flexibleSearchSchedule.do")
        .param("methodToUse", "Continue+%3E%3E")
        .param("tripSummaryPinned", "false")
        .param("selectedInboundDate", "2013/11/22%2006%3A30")
        .param("selectedOutboundDate", "2013/11/11%2008%3A00")
        .param("flowType", "STANDARD_FLOW")
//        .param("workflowToken", "${workflowToken}")
        .headers(headers_3)
        .check(status.is(200)))
      .pause(1, 3).
    exec(
      http("select flights")
        .post("/cgi-bin/obel01im1/bookonline/flightScheduleLookupDispatchAction.do")
//        .param("workflowToken", "${workflowToken}")
        .param("flowType", "STANDARD_FLOW")
        .param("cheaperFareSearch", "false")
        .param("changeDayFlightDirection", "-1")
        .param("methodToUse", "Continue+%3E%3E")
        .param("selectedOption_1", "0")
        .param("selectedOption_2", "0")
        .param("tripSummaryPinned", "false")
        .headers(headers_4)
        .check(status.is(200)))
      .pause(1, 3).
    exec(
      http("price")
        .post("/cgi-bin/obel01im1/bookonline/fareQuoteLookupDispatchAction.do")
//        .param("workflowToken", "${workflowToken}")
        .param("flowType", "STANDARD_FLOW")
        .param("upgradeIndexValues", "0")
        .param("methodToUse", "Continue+%3E%3E")
        .param("rulesAuth", "on")
        .param("tripSummaryPinned", "false")
        .headers(headers_5)
        .check(status.is(200)))
      .pause(1, 3)

  setUp(scn.inject(ramp(100 users) over (10 seconds)))
    .protocols(httpConf)
    .assertions(
      global.successfulRequests.percent.is(100)
    )
}
