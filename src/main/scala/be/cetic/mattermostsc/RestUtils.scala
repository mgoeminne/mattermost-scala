package be.cetic.mattermostsc

import java.io.File

import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.{HttpClientBuilder, LaxRedirectStrategy}
import spray.json.JsValue

import scala.io.Source
import spray.json._
import DefaultJsonProtocol._
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.entity.mime.{MultipartEntity, MultipartEntityBuilder}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.message.BasicNameValuePair

import collection.JavaConverters._


object RestUtils
{
   /**
     * Submits a POST query to the Mattermost server, and forwards the answer
     * @param client The session with which the query must be sent;
     * @param path The path tot he API endpoint.
     * @param params The parameters associated to the query, including the session token
     * @return a JSON element containing the answer of the query.
     */
   def post_query(client: Client, path: String, params: JsValue): JsValue =
   {
      val httpClient = HttpClientBuilder.create()
                                    .setRedirectStrategy(new LaxRedirectStrategy())
                                    .build()

      val request = new HttpPost(s"${client.url}/${path}")
      request.addHeader("Cookie", client.cookie)
      request.addHeader("X-Requested-With", "XMLHttpRequest")
      request.setEntity(new StringEntity(params.prettyPrint))

      val answer = httpClient.execute(request)

      val answer_headers = answer.headerIterator.asScala.map(entry => {
         val parts = entry.toString.split(":")
         parts(0) -> parts.drop(1).mkString(":")
      }).toMap

      client.cookie = answer_headers.get("Cookie") match {
         case Some(cookie) => cookie
         case None => client.cookie
      }

      Source.fromInputStream(answer.getEntity.getContent)
            .getLines.mkString("")
            .parseJson
   }

   /**
     * Submits a GET query to the Mattermost server, and forwards the answer
     * @param client The client with which the query must be sent;
     * @param path The path tot he API endpoint.
     * @param params The parameters associated to the query.
     * @return a JSON element containing the answer of the query.
     */
   def get_query(client: Client, path: String, params: Map[String, String] = Map()): JsValue =
   {
      val httpClient = HttpClientBuilder.create()
                                    .setRedirectStrategy(new LaxRedirectStrategy())
                                    .build()


      val parameters = new java.util.ArrayList[NameValuePair](params.map(entry => new BasicNameValuePair(entry._1, entry._2)).asJavaCollection)
      val paramString = URLEncodedUtils.format(parameters, "utf-8")

      val request = new HttpGet(s"${client.url}/${path}?${paramString}")
      request.addHeader("Cookie", client.cookie)
      request.addHeader("X-Requested-With", "XMLHttpRequest")

      val answer = httpClient.execute(request)

      val answer_headers = answer.headerIterator.asScala.map(entry => {
         val parts = entry.toString.split(":")
         parts(0) -> parts.drop(1).mkString(":")
      }).toMap

      client.cookie = answer_headers.get("Cookie") match {
         case Some(cookie) => cookie
         case None => client.cookie
      }

      Source.fromInputStream(answer.getEntity.getContent)
         .getLines.mkString("")
         .parseJson
   }

   def post_file(client: Client, channel: Channel, file: File): String =
   {
      val path = s"api/v3/teams/${channel.team_id}/files/upload"

      val httpClient = HttpClientBuilder.create()
                                    .setRedirectStrategy(new LaxRedirectStrategy())
                                    .build()


      val request = new HttpPost(s"${client.url}/${path}")
      request.addHeader("Cookie", client.cookie)
      request.addHeader("X-Requested-With", "XMLHttpRequest")

      val httpEntity = MultipartEntityBuilder.create()
                                             .addBinaryBody("files", file)
                                             .addTextBody("channel_id", channel.id)
                                             .build()


      request.setEntity(httpEntity)

      val response = httpClient.execute(request)

      val answer = Source.fromInputStream(response.getEntity.getContent)
                         .getLines
                         .mkString("")
                         .parseJson
                         .asJsObject
                         .fields

      (answer("filenames").convertTo[Seq[String]]).head
   }
}
