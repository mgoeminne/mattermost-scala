package be.cetic.mattermostsc

import java.io.File

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import spray.json.JsArray
import spray.json._
import DefaultJsonProtocol._

import scala.collection.Iterable
import spray.json._
import DefaultJsonProtocol._

/**
  * A channel is a place where messages can be sent and received.
  *
  * @param id
  * @param create_at
  * @param update_at
  * @param delete_at
  * @param team_id
  * @param `type`
  *  - P for "private"
  *  - O for "open" (public)
  *  - D for "direct"
  * @param display_name
  * @param name
  * @param _header
  * @param _purpose
  * @param last_post_at
  * @param total_msg_count
  * @param extra_update_at
  * @param creator_id
  */
case class Channel(id: String,
                   create_at: LocalDateTime,
                   update_at: LocalDateTime,
                   delete_at: Option[LocalDateTime],
                   team_id: String,
                   `type`: String,
                   display_name: String,
                   name: String,
                   private var _header: String,
                   private var _purpose: String,
                   last_post_at: LocalDateTime,
                   total_msg_count: Long,
                   extra_update_at: LocalDateTime,
                   creator_id: String)
{
   // TODO: Invite others to this channel
   // TODO: List members
   // TODO leave channel


   // If admnistrateur of the channel
   // TODO View info
   // TODO Add members
   // TODO Manage members (== invite others?)
   // TODO rename channel



   /**
     * @param session The session that must be used for submitting queries.
     * @return the posts associated to the channel, ordered anti-chronologically (the last post first).
     */
   def posts(implicit session: ClientSession): Stream[Post] =
   {
      val items_per_page = 60

      def postsInPage(page: Int): Iterable[Post] =
      {
         val path = s"api/v3/teams/${team_id}/channels/${id}/posts/page/${page}/${items_per_page}"
         val answer = RestUtils.get_query(session.client, path)
                               .asJsObject
                               .fields

         val order = answer("order").convertTo[Seq[String]]

         val items = answer("posts").asJsObject.fields.map {case (key, obj) => {
               val data = obj.asJsObject.fields

              Post(
                  data("id").convertTo[String],
                  new LocalDateTime(data("create_at").convertTo[Long]),
                  new LocalDateTime(data("update_at").convertTo[Long]),
                  data("delete_at").convertTo[Long] match {
                     case 0 => None
                     case date => Some(new LocalDateTime(date))
                  },
                  data("user_id").convertTo[String],
                  data("channel_id").convertTo[String],
                  data("root_id").convertTo[String],
                  data("parent_id").convertTo[String],
                  data("original_id").convertTo[String],
                  data("message").convertTo[String],
                  data("type").convertTo[String],
                  data("props").convertTo[Map[String, String]],
                  data("hashtags").convertTo[String],
                  data("filenames").convertTo[Seq[String]],
                  data("pending_post_id").convertTo[String]
               )
            }
         }

         order.flatMap(element => items.find(_.id == element))
      }

      def rec_posts(page: Int): Stream[Post] =
      {
         postsInPage(page).toSeq match {
            case Seq() => Stream.empty
            case l => {
               Stream(l :_*) #::: rec_posts(page+1)
            }
         }
      }

      rec_posts(0)
   }


   /**
     * Sends a message to a particular channel.
     * @param message The message to send.
     * @param files   Optional messages that can be joined to the message.
     * @param session The session that must be used for submitting queries.
     */
   def send(message: String, files: Seq[File] = Seq())(implicit session: ClientSession) =
   {
      val filenames = files.map(file => upload(file))

      val path = s"api/v3/teams/${team_id}/channels/${id}/posts/create"
      val now = DateTime.now(DateTimeZone.UTC).getMillis

      RestUtils.post_query(session.client, path, Map(
         "channel_id" -> id.toJson,
         "create_at" -> now.toJson,
         "filenames" -> filenames.toJson,
         "message" -> message.toJson,
         "pending_post_id" -> (session.client.id + ":" + now).toJson,
         "user_id" -> session.client.id.toJson
      ).toJson)
   }

   /**
     * Uploads a file to this channel.
     * @param file The file to share.
     * @param session The session that must be used for submitting queries.
     * @return The path of the file on the Mattermost server.
     */
   def upload(file: File)(implicit session: ClientSession) = RestUtils.post_file(session.client, this, file)


   /**
     * @param session The session that must be used for submitting queries.
     * @return The members who subscribed to this channel.
     */
   def members(implicit session: ClientSession): Seq[Profile] =
   {
      RestUtils.get_query(
         session.client,
         s"api/v3/teams/${team_id}/channels/${id}/extra_info"
      ).asJsObject.fields("members") match {
         case JsArray(l) => l.map(member => Profile(member.asJsObject)(session))
      }
   }

   /**
     * @return true if this is a private (active) channel, false otherwise.
     */
   def is_private = this.`type` == "P"

   /**
     * @param session The session that must be used for submitting queries.
     * @return true if the client is the owner of this channel; false otherwise.
     */
   def is_mine(implicit session: ClientSession) = creator_id == session.client.id

   /**
     * Tries to delete the channel.
     * Only works if the client is the owner (or an admin).
     *
     * @param session The session that must be used for submitting queries.
     * @return true if the channel has been successfully deleted; false otherwise
     */
   def delete(implicit session: ClientSession): Boolean =
   {
      val ret = RestUtils.post_query(session.client, s"api/v3/teams/${session.team.id}/channels/${this.id}/delete", JsNull)
                         .asJsObject

      ret.fields.keySet == Set("id")
   }

   def header = _header
   def header_=(content: String)(implicit session: ClientSession)
   {
      _header = content

      val params = Map(
         "channel_header" -> content,
         "channel_id" -> this.id
      ).toJson

      RestUtils.post_query(session.client, s"api/v3/teams/${session.team.id}/channels/update_header", params)
   }

   def purpose = _purpose
   def purpose_=(content: String)(implicit session: ClientSession)
   {
      _purpose = content

      val params = Map(
         "channel_purpose" -> content,
         "channel_id" -> this.id
      ).toJson

      RestUtils.post_query(session.client, s"api/v3/teams/${session.team.id}/channels/update_purpose", params)
   }
}

object Channel
{
   def apply(obj: JsObject) =
   {
      val fields = obj.fields

      new Channel(
         fields("id").convertTo[String],
         new LocalDateTime(fields("create_at").convertTo[Long]),
         new LocalDateTime(fields("update_at").convertTo[Long]),
         fields("delete_at").convertTo[Long] match {
            case 0 => None
            case date => Some(new LocalDateTime(date))
         },
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
   }
}
