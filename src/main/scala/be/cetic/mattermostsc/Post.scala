package be.cetic.mattermostsc

import java.io.InputStream
import java.net.URL

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

/**
  * A post sent in a channel.
  */
case class Post(  id: String,
                  create_at: LocalDateTime,
                  update_at: LocalDateTime,
                  delete_at: Option[LocalDateTime],
                  user_id: String,
                  channel_id: String,
                  root_id: String,
                  parent_id: String,
                  original_id: String,
                  message: String,
                  `type`: String,
                  props: Map[String, String],
                  hashtags: String,
                  filenames: Seq[String],
                  pending_post_id: String
               )
{
   def user: Option[CompleteProfile] = user_id match {
      case "" => None
      case id => ???
   }

   def channel(implicit session: ClientSession): Channel = session.channels.find(_.id == channel_id).get

   def root(implicit session: ClientSession): Option[Post] = root_id match {
      case "" => None
      case r => channel(session).posts.find(_.id == r)
   }

   def parent(implicit session: ClientSession): Option[Post] = parent_id match {
      case "" => None
      case p => channel(session).posts.find(_.id == p)
   }

   def original(implicit session: ClientSession): Option[Post] = original_id match {
      case "" => None
      case o => channel(session).posts.find(_.id == o)
   }

   /**
     * @param session
     * @return All the posts that are considered as answers to this post.
     */
   def children(implicit session: ClientSession): Stream[Post] = ???

   /**
     * @param session The session that must be used for submitting queries.
     * @return A list of the name of attached files, as well as direct url for downloading them.
     */
   def attachments(implicit session: ClientSession): Seq[(String, String)] =
   {
      filenames.map(name => (
         name.split("/").last,
         s"${session.client.url}/api/v3/teams/${this.channel.team_id}/files/get/${name}")
      )
   }

   def avatar(implicit session: ClientSession): URL =
      new URL(s"${session.client.url}/api/v3/users/${user_id}/image?time=${DateTime.now(DateTimeZone.UTC).getMillis}")
}