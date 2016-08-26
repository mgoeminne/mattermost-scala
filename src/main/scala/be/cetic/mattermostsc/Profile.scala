package be.cetic.mattermostsc

import java.net.URL

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import spray.json._
import DefaultJsonProtocol._

class Profile(val id: String,
              val nickname: String,
              val email: String,
              val roles: String,
              val username: String)
{
   /**
     * @param session The session that must be used for submitting queries.
     * @return The current status of this profile
     */
   def status(implicit session: ClientSession): String =
   {
      val path = "api/v3/users/status"
      val params = Seq(this.id).toJson

      RestUtils.post_query(session.client, path, params)
         .asJsObject
         .fields(this.id).convertTo[String]
   }

   def avatar(implicit session: ClientSession): URL =
      new URL(s"${session.client.url}/api/v3/users/${id}/image?time=${DateTime.now(DateTimeZone.UTC).getMillis}")

   /**
     * @param session The session that must be used for submitting queries.
     * @return The direct channel shared with this profile, if such a channel exists.
     */
   def direct_channel(implicit session: ClientSession): Option[Channel] = session.channel(session.client.id + "__" + this.id)

   /**
     * Creates a direct channel with this profile, if such a channel does not exist.
     * @param session The session that must be used for submitting queries.
     * @return The existing direct channel shared with this profile, if such a channel exist,
     *         or the freshly created direct channel shared with this profile, otherwise.
     */
   def create_direct_channel(implicit session: ClientSession): Channel =
   {
      direct_channel match {
         case Some(existing) => existing
         case None => {
            val params = Map(
               "user_id" -> this.id
            ).toJson

            val ret = RestUtils.post_query(
               session.client,
               s"api/v3/teams/${session.team.id}/channels/create_direct",
               params
            )

            Channel(ret.asJsObject)
         }

      }

   }

   override def toString = username
}


/**
  * A profile that directly contains all the informations that relate to other members.
  * @param id
  * @param first_name
  * @param last_name
  * @param username
  * @param nickname
  * @param email
  * @param created_at
  * @param last_activity_at
  * @param roles
  * @param locale
  * @param auth_data
  * @param delete_at
  */
class CompleteProfile(id: String,
                      val first_name: String,
                      val last_name: String,
                      username: String,
                      nickname: String,
                      email: String,
                      val created_at: LocalDateTime,
                      val last_activity_at: LocalDateTime,
                      roles: String,
                      val locale: String,
                      val auth_data: String,
                      val delete_at: Option[LocalDateTime]) extends Profile(id, nickname, email, roles, username)
{
   /**
     * @return true if the profile is still active on the server, false otherwise.
     */
   def is_active = delete_at match {
      case None => true
      case _ => false
   }
}


object Profile
{
   def apply(obj: JsObject)(implicit session: ClientSession) =
   {
      val data = obj.fields

      new Profile(
         data("id").convertTo[String],
         data("nickname").convertTo[String],
         data("email").convertTo[String],
         data("roles").convertTo[String],
         data("username").convertTo[String]
      )
   }
}


object CompleteProfile
{
   def apply(obj: JsObject) =
   {
      val data = obj.fields

      new CompleteProfile(
         data("id").convertTo[String],
         data("first_name").convertTo[String],
         data("last_name").convertTo[String],
         data("username").convertTo[String],
         data("nickname").convertTo[String],
         data("email").convertTo[String],
         new LocalDateTime(data("create_at").convertTo[Long]),
         new LocalDateTime(data("last_activity_at").convertTo[Long]),
         data("roles").convertTo[String],
         data("locale").convertTo[String],
         data("auth_data").convertTo[String],
         data("delete_at").convertTo[Long] match {
            case 0 => None
            case date => Some(new LocalDateTime(data("delete_at").convertTo[Long]))
         }
      )
   }
}