package be.cetic.mattermostsc

/**
  * A session that allows a client to communicate with a Mattermost server.
  *
  * Obtaining such a session is the first step for collecting and sending messages and
  * documents to other Mattermost users.
  */
case class ClientSession(client: Client, team: Team)
{
   def channels = team.channels(this)
   def channels(name: String): Seq[Channel] = channels filter (_.name == name)
   def channel(name: String): Option[Channel] = channels(name).headOption



}
