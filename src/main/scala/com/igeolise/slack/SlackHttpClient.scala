package com.igeolise.slack

import com.igeolise.slack.HooksSlackClient.{Error, HookMessage}
import com.igeolise.slack.dto.InteractiveMessage
import com.igeolise.slack.json.InteractiveMessageWrites._
import com.igeolise.slack.util.PayloadMapper
import dispatch.{Http, url => Url, _}
import play.api.libs.json.{JsValue, Json}
import java.nio.charset.StandardCharsets.UTF_8

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SlackHttpClient extends HooksSlackClient with ApiSlackClient {

  def sendMsg(hooksMessage: HookMessage, token: String): Future[Either[Error, Unit]] = {
    val body = PayloadMapper.toBodyJson(hooksMessage)
    val url = hooksUrl + token

    sendPost(url, body, Map.empty)
  }

  def sendInteractiveMessage(interactiveMessage: InteractiveMessage, authToken: String): Future[Either[Error, Unit]] =
    sendPost(
      postMessageUrl,
      Json.toJson(interactiveMessage),
      Map("Authorization" -> Seq(authToken))
    )

  private def sendPost(url: String, body: JsValue, headers: Map[String, Seq[String]]): Future[Either[Error, Unit]] = {
    val request =
      Url(url)
        .POST
        .setBody(body.toString)
        .setHeaders(headers)
        .setContentType(jsonContentType, UTF_8)

    Http
      .default(request)
      .either
      .map {
        case Left(t) => Left(Error.Exception(t))
        case Right(r) => if (isResponseSuccess(r.getStatusCode)) Right(())
        else Left(Error.UnexpectedStatusCode(r.getStatusCode, r.getResponseBody))
      }
  }

  def isResponseSuccess(code: Int): Boolean = code >= 200 && code <= 299
}