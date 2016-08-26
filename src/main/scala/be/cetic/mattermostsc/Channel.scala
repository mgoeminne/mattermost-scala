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
  */
case class Channel(id: String,
                   create_at: LocalDateTime,
                   update_at: LocalDateTime,
                   delete_at: Option[LocalDateTime],
                   team_id: String,
                   `type`: String,
                   display_name: String,
                   name: String,
                   header: String,
                   purpose: String,
                   last_post_at: LocalDateTime,
                   total_msg_count: Long,
                   extra_update_at: LocalDateTime,
                   creator_id: String)
{
   //TODO: set a header
   //TODO: Invite others to this channel
   //TODO: List members


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
   def members(implicit session: ClientSession): Seq[PartialProfile] =
   {
      RestUtils.get_query(
         session.client,
         s"api/v3/teams/${team_id}/channels/${id}/extra_info"
      ).asJsObject.fields("members") match {
         case JsArray(l) => l.map(member => PartialProfile(member.asJsObject)(session))
      }
   }

   /**
     * @return true if this is a private (active) channel, false otherwise.
     */
   def is_private = this.`type` == "P"
}
