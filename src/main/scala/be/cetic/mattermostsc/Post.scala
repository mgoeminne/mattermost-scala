package be.cetic.mattermostsc

import org.joda.time.LocalDateTime

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
   def user: Profile = ???
   def channel: Channel = ???
   def root: Post = ???
   def parent: Post = ???
   def original: Post = ???
}
