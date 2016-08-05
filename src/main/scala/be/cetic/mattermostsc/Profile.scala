package be.cetic.mattermostsc

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{HttpClientBuilder, LaxRedirectStrategy}
import org.joda.time.LocalDateTime

import scala.io.Source

import spray.json._
import DefaultJsonProtocol._

/**
  * A profile is the visible part of the other user accounts.
  */
case class Profile(  id: String,
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

      val answer = RestUtils.post_query(session, path, params)
                            .asJsObject
                            .fields

      return answer(this.id).convertTo[String]
   }
}
