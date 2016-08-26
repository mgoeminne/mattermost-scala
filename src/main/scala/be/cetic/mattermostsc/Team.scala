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
                  delete_at: Option[LocalDateTime],
                  create_at: LocalDateTime,
                  `type`: String,
                  allow_open_invite: Boolean
               )
{
   /**
     * @param session The session that must be used for submitting queries.
     * @return the channels associated to this team.
     */
   def channels(implicit session: ClientSession): Seq[Channel] =
   {
      val regular = RestUtils.get_query(session.client, s"/api/v3/teams/${id}/channels/").asJsObject.fields("channels") match {
         case JsArray(channels) => channels.map(channel => Channel(channel.asJsObject))
      }

      val more = RestUtils.get_query(session.client, s"/api/v3/teams/${id}/channels/more").asJsObject.fields("channels") match {
         case JsArray(channels) => channels.map(channel => Channel(channel.asJsObject))
      }

      regular ++ more
   }

   /**
     * @param session The session that must be used for submitting queries.
     * @param name The name of a channel
     * @return All the channels that belong to this team and having the specified name
     */
   def channels(name: String)(implicit session: ClientSession): Seq[Channel] = channels(session).filter(_.name == name)

   /**
     * @param session The session that must be used for submitting queries.
     * @param name The name of a channel.
     * @return The first channel the name of which corresponds to the researched one, or None if
     *         no channel corresponds.
     */
   def channel(name: String)(implicit session: ClientSession) = channels(name)(session).headOption

   /**
     * Tries to create a new channel for this team.
     * @param name The name of the channel.
     * @param slug The url slug associated to the channel. Must be unique among the team's channels.
     * @param purpose A description of the channel purpose.
     * @param open true if the channel is open, false otherwise.
     * @return some channel if it has been created, none otherwise.
     */
   def create_channel(name: String, slug: String, purpose: String, open: Boolean)(implicit session: ClientSession) : Option[Channel] =
   {
      val params = Map(
         "display_name" -> name,
         "name" -> slug,
         "purpose" -> purpose,
         "type" -> (open match {
            case true => "O"
            case false => "P"
         })
      ).toJson.asJsObject

      val ret = RestUtils.post_query(session.client, s"api/v3/teams/${session.team.id}/channels/create", params)

      ret.asJsObject.fields.contains("status_code") match {
         case true => None
         case false => {
            Some(Channel(ret.asJsObject))
         }
      }
   }
}