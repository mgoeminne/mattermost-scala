package be.cetic.mattermostsc

import java.net.URL

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import spray.json._
import DefaultJsonProtocol._

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
case class CompleteProfile(id: String,
                           first_name: String,
                           last_name: String,
                           username: String,
                           nickname: String,
                           email: String,
                           created_at: LocalDateTime,
                           last_activity_at: LocalDateTime,
                           roles: String,
                           locale: String,
                           auth_data: String,
                           delete_at: Option[LocalDateTime])
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
}

case class PartialProfile(id: String,
                          nickname: String,
                          email: String,
                          roles: String,
                          username: String)(implicit session: ClientSession)


object PartialProfile
{
   def apply(obj: JsObject)(implicit session: ClientSession) =
   {
      val data = obj.fields

      new PartialProfile(
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