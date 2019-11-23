package com.igeolise.slack

import com.igeolise.slack.HttpSlackClient.PayloadMapper
import com.igeolise.slack.SlackClient.Attachment
import com.igeolise.slack.SlackClient.Color.{Gray, Green}
import com.igeolise.slack.SlackClient.Notify.{Channel, UserGroup, UserID}
import org.scalacheck.Gen
import org.scalatest.Matchers
import org.scalatest.FunSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class HttpSlackClientTest extends FunSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  describe("HttpSlackClient") {
    it("treat 2xx response as success") {
      val successCodeGen = Gen.choose(200, 299)

      forAll(successCodeGen) { code =>
        HttpSlackClient.isResponseSuccess(code) shouldEqual true
      }
    }

    it("treat non 2xx response as failure") {
      val failureCodeGen = for {
        bellow <- Gen.choose(-1000, 199)
        above  <- Gen.choose(300, 1000)
        code   <- Gen.oneOf(bellow, above)
      } yield code

      forAll(failureCodeGen) { code =>
        HttpSlackClient.isResponseSuccess(code) shouldEqual false
      }
    }
  }

  describe("PayloadMapper") {
    it("should map to json string") {
      val mapped =
        PayloadMapper
          .toBody(
            notify = Seq(Channel, UserID("W123"), UserGroup("dev-ops", "SGQKH63CF")),
            msg = "some message text",
            attachments = Seq(Attachment("some attachment text 1", Green), Attachment("some attachment text 2", Gray))
          ).toString()

      val expected = """{"text":"<!channel> <@W123> <!subteam^SGQKH63CF|dev-ops> some message text",""" +
                     """"attachments":[""" +
                     """{"text":"some attachment text 1","color":"#008000","mrkdwn_in":["text"]},""" +
                     """{"text":"some attachment text 2","color":"#808080","mrkdwn_in":["text"]}""" +
                     """],"link_names":1}"""
      mapped shouldEqual expected
    }
  }
}
