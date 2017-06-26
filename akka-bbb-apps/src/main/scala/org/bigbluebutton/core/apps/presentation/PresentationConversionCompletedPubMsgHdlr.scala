package org.bigbluebutton.core.apps.presentation

import org.bigbluebutton.common2.domain.PageVO
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.common2.messages.MessageBody.PresentationConversionCompletedEvtMsgBody
import org.bigbluebutton.common2.messages._
import org.bigbluebutton.core.apps.Presentation

trait PresentationConversionCompletedPubMsgHdlr {
  this: PresentationApp2x =>

  val outGW: OutMessageGateway

  def handlePresentationConversionCompletedPubMsg(msg: PresentationConversionCompletedPubMsg): Unit = {
    log.debug("PresentationConversionCompletedPubMsg ")
    def broadcastEvent(msg: PresentationConversionCompletedPubMsg): Unit = {
      val routing = Routing.addMsgToClientRouting(MessageTypes.BROADCAST_TO_MEETING, liveMeeting.props.meetingProp.intId, msg.header.userId)
      val envelope = BbbCoreEnvelope(PresentationConversionCompletedEvtMsg.NAME, routing)
      val header = BbbClientMsgHeader(PresentationConversionCompletedEvtMsg.NAME, liveMeeting.props.meetingProp.intId, msg.header.userId)

      val body = PresentationConversionCompletedEvtMsgBody(msg.body.messageKey, msg.body.code, msg.body.presentation)
      val event = PresentationConversionCompletedEvtMsg(header, body)
      val msgEvent = BbbCommonEnvCoreMsg(envelope, event)
      outGW.send(msgEvent)

      //record(event)
    }

    val pages = new collection.mutable.HashMap[String, PageVO]

    msg.body.presentation.pages.foreach { p =>
      val page = PageVO(p.id, p.num, p.thumbUri, p.swfUri, p.txtUri, p.svgUri, p.current, p.xOffset, p.yOffset,
        p.widthRatio, p.heightRatio)
      pages += page.id -> page
    }

    val pres = new Presentation(msg.body.presentation.id, msg.body.presentation.name, msg.body.presentation.current,
      pages.toMap, msg.body.presentation.downloadable)

    log.debug("PresentationConversionCompletedPubMsg name={}", pres.name)

    presentationConversionCompleted(pres)
    broadcastEvent(msg)
  }
}