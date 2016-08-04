package be.cetic.mattermostsc

import org.joda.time.LocalDateTime

/**
  * A team is an organisational unit Mattermost users can belong to.
  */
case class Team(  id: String,
                  name: String,
                  company_name: String,
                  email: String,
                  display_name: String,
                  update_at: LocalDateTime,
                  allowed_domains: String,
                  invite_id: String,
                  delete_at: LocalDateTime,
                  create_at: LocalDateTime,
                  `type`: String,
                  allow_open_invite: Boolean
               )