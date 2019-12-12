package com.igeolise.slack.dto

import com.igeolise.slack.dto.Button.{ActionId, ButtonStyle}
import com.igeolise.slack.dto.InteractiveMessage.Element

/**
 * [[https://api.slack.com/reference/block-kit/block-elements#button]]
 */
case class Button(
  textObject: TextObject,
  actionId: ActionId,
  style: Option[ButtonStyle] = None,
  url: Option[String] = None,
  value: Option[String] = None,
  confirm: Option[ConfirmDialog] = None
) extends Element

object Button {
  case class ActionId(id: String)

  sealed abstract class ButtonStyle(val literal: String)
  case object Primary extends ButtonStyle("primary")
  case object Danger extends ButtonStyle("danger")
}
