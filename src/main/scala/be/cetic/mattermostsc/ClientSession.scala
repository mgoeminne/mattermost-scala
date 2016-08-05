package be.cetic.mattermostsc

import java.net.URL

import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.{HttpClientBuilder, LaxRedirectStrategy}
import org.joda.time.LocalDateTime

import spray.json._
import DefaultJsonProtocol._

import collection.JavaConverters._

import scala.io.Source

/**
  * A session that allows a client to communicate with a Mattermost server.
  *
  * Obtaining such a session is the first step for collecting and sending messages and
  * documents to other Mattermost users.
  */
case class ClientSession(
   url: String,
   id: String,
   token: String,
   var cookie: String,
   create_at: LocalDateTime,
   update_dat: LocalDateTime,
   delete_at: LocalDateTime,
   username: String,
   auth_data: String,
   auth_service: String,
   email: String,
   email_verified: Boolean,
   nickname: String,
   first_name: String,
   last_name: String,
   roles: String,
   last_activity: LocalDateTime,
   last_ping_at: LocalDateTime,
   allow_marketing: Boolean,
   notify_props: Map[String, String],
   last_password_update: LocalDateTime,
   last_picture_update: LocalDateTime,
   locale: String)
{
   /**
     * @return  All the teams the user belongs to.
     *          TODO: Maybe all the teams are actually returned… must be tested
     */
   def teams: Seq[Team] =
   {
      val document = RestUtils.get_query(this, "api/v3/users/initial_load")
                              .asJsObject
                              .fields

      document("teams") match
      {
         case JsArray(teams) => teams.map(team =>
         {
            val data = team.asJsObject.fields

            new Team(
               data("id").convertTo[String],
               data("name").convertTo[String],
               data("company_name").convertTo[String],
               data("email").convertTo[String],
               data("display_name").convertTo[String],
               new LocalDateTime(data("update_at").convertTo[Long]),
               data("allowed_domains").convertTo[String],
               data("invite_id").convertTo[String],
               new LocalDateTime(data("delete_at").convertTo[Long]),
               new LocalDateTime(data("create_at").convertTo[Long]),
               data("type").convertTo[String],
               data("allow_open_invite").convertTo[Boolean]
            )
         })
      }
   }

   def profiles: Seq[Profile] =
   {
      val path = s"""${url}/api/v3/users/initial_load"""

      val client = HttpClientBuilder.create()
         .setRedirectStrategy(new LaxRedirectStrategy())
         .build()

      val request = new HttpGet(path)
      request.addHeader("Cookie", cookie)

      val answer = client.execute(request)

      val document = Source.fromInputStream(answer.getEntity.getContent)
         .getLines.mkString("")
         .parseJson
         .asJsObject
         .fields

      document("direct_profiles")
         .asJsObject
         .fields
         .map(field =>
         {
            val data = field._2.asJsObject.fields

            new Profile(
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
               new LocalDateTime(data("delete_at").convertTo[Long])
            )
         }).toSeq
   }
}