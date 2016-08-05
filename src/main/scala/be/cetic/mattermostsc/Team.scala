package be.cetic.mattermostsc

import org.joda.time.LocalDateTime
import spray.json.JsArray

import spray.json._
import DefaultJsonProtocol._

/**
  * A team is an organisational unit Mattermost users can belong to.
  */
case class Team(  id: String,
                  name: String,
                  company_name: String,
                  email: String,
                  display_name: String,
                  update_at: LocalDateTime,
                  allowed_domains: String,
                  invite_id: String,
                  delete_at: LocalDateTime,
                  create_at: LocalDateTime,
                  `type`: String,
                  allow_open_invite: Boolean
               )
{
   /**
     * @param session The session that must be used for submitting queries.
     * @return the channels associated to this team.
     */
   def channels(session: ClientSession): Seq[Channel] =
   {
      RestUtils.get_query(session, s"/api/v3/teams/${id}/channels/").asJsObject.fields("channels") match {
         case JsArray(channels) => channels.map(channel => {
            val fields = channel.asJsObject.fields

            Channel(
               fields("id").convertTo[String],
               new LocalDateTime(fields("create_at").convertTo[Long]),
               new LocalDateTime(fields("update_at").convertTo[Long]),
               new LocalDateTime(fields("delete_at").convertTo[Long]),
               fields("team_id").convertTo[String],
               fields("type").convertTo[String],
               fields("display_name").convertTo[String],
               fields("name").convertTo[String],
               fields("header").convertTo[String],
               fields("purpose").convertTo[String],
               new LocalDateTime(fields("last_post_at").convertTo[Long]),
               fields("total_msg_count").convertTo[Long],
               new LocalDateTime(fields("extra_update_at").convertTo[Long]),
               fields("creator_id").convertTo[String]
            )
         })
      }
   }
}