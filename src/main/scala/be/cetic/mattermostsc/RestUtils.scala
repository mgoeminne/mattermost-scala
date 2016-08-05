package be.cetic.mattermostsc

import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.{HttpClientBuilder, LaxRedirectStrategy}
import spray.json.JsValue

import scala.io.Source
import spray.json._
import DefaultJsonProtocol._
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair

import collection.JavaConverters._


object RestUtils
{
   /**
     * Submits a POST query to the Mattermost server, and forwards the answer
     * @param session The session with which the query must be sent;
     * @param path The path tot he API endpoint.
     * @param params The parameters associated to the query, including the session token
     * @return a JSON element containing the answer of the query.
     */
   def post_query(session: ClientSession, path: String, params: JsValue): JsValue =
   {
      val client = HttpClientBuilder.create()
                                    .setRedirectStrategy(new LaxRedirectStrategy())
                                    .build()

      val request = new HttpPost(session.url + "/" + path)
      request.addHeader("Cookie", session.cookie)
      request.addHeader("X-Requested-With", "XMLHttpRequest")
      request.setEntity(new StringEntity(params.prettyPrint))

      val answer = client.execute(request)

      val answer_headers = answer.headerIterator.asScala.map(entry => {
         val parts = entry.toString.split(":")
         parts(0) -> parts.drop(1).mkString(":")
      }).toMap

      session.cookie = answer_headers.get("Cookie") match {
         case Some(cookie) => cookie
         case None => session.cookie
      }

      Source.fromInputStream(answer.getEntity.getContent)
            .getLines.mkString("")
            .parseJson
   }

   /**
     * Submits a GET query to the Mattermost server, and forwards the answer
     * @param session The session with which the query must be sent;
     * @param path The path tot he API endpoint.
     * @param params The parameters associated to the query.
     * @return a JSON element containing the answer of the query.
     */
   def get_query(session: ClientSession, path: String, params: Map[String, String] = Map()): JsValue =
   {
      val client = HttpClientBuilder.create()
                                    .setRedirectStrategy(new LaxRedirectStrategy())
                                    .build()


      val parameters = new java.util.ArrayList[NameValuePair](params.map(entry => new BasicNameValuePair(entry._1, entry._2)).asJavaCollection)
      val paramString = URLEncodedUtils.format(parameters, "utf-8")

      val request = new HttpGet(session.url + "/" + path + "?" + paramString)
      request.addHeader("Cookie", session.cookie)
      request.addHeader("X-Requested-With", "XMLHttpRequest")

      val answer = client.execute(request)

      val answer_headers = answer.headerIterator.asScala.map(entry => {
         val parts = entry.toString.split(":")
         parts(0) -> parts.drop(1).mkString(":")
      }).toMap

      session.cookie = answer_headers.get("Cookie") match {
         case Some(cookie) => cookie
         case None => session.cookie
      }

      Source.fromInputStream(answer.getEntity.getContent)
         .getLines.mkString("")
         .parseJson
   }
}
