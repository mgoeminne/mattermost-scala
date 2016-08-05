package be.cetic.mattermostsc

import org.joda.time.LocalDateTime


/**
  * A channel is a place where messages can be sent and received.
  */
case class Channel(id: String,
                   create_at: LocalDateTime,
                   update_at: LocalDateTime,
                   delete_at: LocalDateTime,
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

}
