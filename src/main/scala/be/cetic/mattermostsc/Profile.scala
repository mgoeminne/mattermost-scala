package be.cetic.mattermostsc

import org.joda.time.LocalDateTime

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
                     delete_at: LocalDateTime)
