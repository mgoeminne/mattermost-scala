package be.cetic.mattermostsc

import java.net.URL

import spray.json._
import DefaultJsonProtocol._
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{DefaultHttpClient, HttpClientBuilder, HttpClients, LaxRedirectStrategy}
import org.joda.time.LocalDateTime

import scala.io.Source




/**
  * Main object for managing a connection to a Mattermost server
  */
object Mattermost
{
   /**
     * Tries to create a connection between the client and a Mattermost server.
     *
     * @param path      The url of the Mattermost server to contact. For instance: "http://localhost"
     * @param team      The name of the Mattermost team to contact.
     * @param login     The login to use to represent itself as a Mattermost user.
     * @param password  The password associated to the user account.
     * @return          An object representing the client session, if the connection has been successfuly
     *                  established, None otherwise.
     */
   def connect(path: URL, team: String, login: String, password: String): Option[ClientSession] =
   {
      import collection.JavaConverters._

      val client = HttpClientBuilder.create()
                                    .setRedirectStrategy(new LaxRedirectStrategy())
                                    .build()

      val request = new HttpPost(s"""${path}/api/v3/users/login""")

      val input = new StringEntity(s"""{"name":"${team}","login_id":"${login}","password":"${password}"}""")
      input.setContentType("application/json")
      request.setEntity(input)

      val answer = client.execute(request)


      answer.getStatusLine.getStatusCode match {
         case 200 => {

            val headers = answer.headerIterator.asScala.map(entry => {
               val parts = entry.toString.split(":")
               parts(0) -> parts.drop(1).mkString(":")
            }).toMap

            val document = Source.fromInputStream(answer.getEntity.getContent)
                                 .getLines.mkString("")
                                 .parseJson
                                 .asJsObject
                                 .fields

            val client = Client(
               path.toString,
               document("id").convertTo[String],
               headers("Token"),
               headers("Set-Cookie"),
               new LocalDateTime(document("create_at").convertTo[Long]),
               new LocalDateTime(document("update_at").convertTo[Long]),
               new LocalDateTime(document("delete_at").convertTo[Long]),
               document("username").convertTo[String],
               document("auth_data").convertTo[String],
               document("auth_service").convertTo[String],
               document("email").convertTo[String],
               document("email_verified").convertTo[Boolean],
               document("nickname").convertTo[String],
               document("first_name").convertTo[String],
               document("last_name").convertTo[String],
               document("roles").convertTo[String],
               new LocalDateTime(document("last_activity_at").convertTo[Long]),
               new LocalDateTime(document("last_ping_at").convertTo[Long]),
               document("allow_marketing").convertTo[Boolean],
               document("notify_props").convertTo[Map[String, String]],
               new LocalDateTime(document("last_password_update").convertTo[Long]),
               new LocalDateTime(document("last_picture_update").convertTo[Long]),
               document("locale").convertTo[String]
            )

            client.team(team).map(t => ClientSession(client, t))
         }
         case _ => None
      }
   }

}
