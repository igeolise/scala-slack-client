package com.igeolise.slack.json
import com.igeolise.slack.dto.InteractiveMessage._
import com.igeolise.slack.dto.TextObject.{Mrkdwn, PlainText, TextType}
import com.igeolise.slack.dto.{Button, ConfirmDialog, InteractiveMessage, TextObject}
import play.api.libs.functional.syntax._
import play.api.libs.json._

object InteractiveMessageWrites {

  val mrkdwnTextWrites: Mrkdwn => JsObject = (mrkdwn: Mrkdwn) => JsObject(Map(
    "type" -> JsString(mrkdwn.textType),
    "verbatim" -> JsBoolean(mrkdwn.verbatimEnabled)
  ))

  val plainTextWrites: PlainText => JsObject = (plainText: PlainText) => JsObject(Map(
    "type" -> JsString(plainText.textType),
    "emoji" -> JsBoolean(plainText.emojiEnabled)
  ))

  val textTypeWrites: TextType => JsObject = {
    case plainText: PlainText => plainTextWrites(plainText)
    case mrkdwn: Mrkdwn => mrkdwnTextWrites(mrkdwn)
  }

  implicit val textObjectWrites: Writes[TextObject] = (textObject: TextObject) =>
    textTypeWrites(textObject.textType) ++ JsObject(Map("text" -> JsString(textObject.text)))

  val sectionWrites: Writes[Section] = (section: Section) => JsObject(Map(
    "type" -> JsString(section.blockType),
    "text" -> textObjectWrites.writes(section.textObject)
  ))

  val confirmDialogWrites: Writes[ConfirmDialog] = (
    (__ \ "title").write[TextObject] and
    (__ \ "text").write[TextObject] and
    (__ \ "confirm").write[TextObject] and
    (__ \ "deny").write[TextObject]
  )(unlift(ConfirmDialog.unapply))

  val buttonWrites: Writes[Button] = (button: Button) => JsObject(
    Map(
      "type" -> Some(JsString("button")),
      "text" -> Some(textObjectWrites.writes(button.textObject)),
      "action_id" -> Some(JsString(button.actionId.id)),
      "url" -> button.url.map(JsString),
      "value" -> button.value.map(JsString),
      "style" -> button.style.map(style => JsString(style.literal)),
      "confirm" -> button.confirm.map(confirmDialogWrites.writes)
    ).collect{ case (key, Some(value)) => (key, value) }
  )

  val elementsWrites: Writes[Element] = Writes[Element] {
    case e: Button => buttonWrites.writes(e)
  }

  val actionsWrites: Writes[Actions] = (actions: Actions) => JsObject(Map(
    "type" -> JsString(actions.blockType),
    "elements" -> JsArray(actions.elements.map(elementsWrites.writes))
  ))

  val dividerWrites: Writes[Divider.type] = (_: InteractiveMessage.Divider.type) =>
    JsObject(Map("type" -> JsString("divider")))

  implicit val blockWrites: Writes[Block] = Writes[Block] {
    case b: Divider.type => dividerWrites.writes(b)
    case b: Section => sectionWrites.writes(b)
    case b: Actions => actionsWrites.writes(b)
  }

  implicit val interactiveMessageWrites: Writes[InteractiveMessage] = (
    (__ \ "channel").write[String].contramap[Channel](_.id) and
    (__ \ "blocks").write[Vector[Block]]
  )(unlift(InteractiveMessage.unapply))
}
