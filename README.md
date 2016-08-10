# Mattermost-scala

A Scala client for Mattermost.

# Quickstart

```bash
$ git clone https://github.com/mgoeminne/mattermost-scala.git ./mattermost-scala
$ cd mattermost-scala
$ sbt package
```

# Getting started

[Mattermost](https://www.mattermost.org/) is an open source alternative to Slack, a team communication tool offering IRC-like features.

This project aims to offer a comprehensive Scala client for connecting the user to a Mattermost server. Currently, only basic operations, such as reading and writing posts are implemented,
but feel free to propose a better coverage of the Mattermost services.


## Creating a Connection

In Mattermost, a user must be connected in order to access the services proposed by a particular server. Once connected, she has to chose 
the _team_ to which she wants to belong. Depending on the selected team, several _channels_ are proposed. Each channel is essentially a sequence of posts
sent by other contributors.

First, connect yourself using your login and password:
 
```scala
val mb_session = Mattermost.connect(
    new URL("https://mattermost.myserver.com"),
    "my-team",
    "login",
    "password")
```

_session_ may be a ClientSession object that combines a connection to the specified server with the specified team. 
If any problem occurs during the connection, for instance because of a wrong password or because the team does not exist, a None is retured.

The session essentially contains a token that identifies you when you submit any further query to the server. Therefore, the session must be 
given as a parameter to must method call. For convenience, this parameter is _implicit_, which means you can avoid to explicitly send it by 
defining your session object as implicit:

```scala
if(mb_session isDefined){
    implicit val session = mb_session.get
    …
}
```

## Browsing the channels

Once a connection has been established, you may want to know the public channels associated to the team you joined.

A channel object contains basic informations such as its name, the number of posts contained in the channel, etc. You can also directly ask for 
a particular channel. The name of the channel is a convenient way to identify it, but unfortunately several channels may have the same name simultaneously.
Therefore, several methods are proposed to access a channel based on its name:

```scala
val channels: Seq[Channel] = session.channels // All the public channels
val channelsBis: Seq[Channel] = session.channels("off-topic") // Returns all the public channels, the name of which is "off-topic". 
val channel: Option[Channel] = session.channel("off-topic") // Returns one of the public channels, the name of which is "off-topic", if any.
```

## Interacting with channel

After you selected a channel, you probably want to retrieve the last messages that have been published in it. 

```scala
val messages = channel.posts
```

The messages are presented by anti-chronological order (the last message first). The Mattermost server proposes a pagined view of these messages, so 
that retrieving the entire history of a channel will typically need to submit several queries to the server.

In addition to the message itself, the name and url of the files attached to a post can be retrieved as well.

```scala
val allFilesUrl = channel.posts.flatMap(post => post.attachments.map(_._2))
```

You are not limited to a passive observation of your team's channels: you can send messages as well! 

```scala
channel.send("Hello, world")
```

If you attach some files to a message, the client will automatically upload them to the server for you.

```scala
channel.send(
    "Here are my holiday pictures. Enjoy!", 
    Seq(new File("/foo/bar.jpg"), new File("/foo/biz.jpg"))
)
```

Optionally, you could be interested in simply submitting files to Mattermost.

```scala
val url = channel.upload(new File("/foo/bar.png"))
```

The resulting URL is the place where users can retrieve the submitted file.

# Call for participation

Any pull request is very welcome. Here is a list of basic features that 
must be implemented in order to make this client useful:

- Change user profiles (name, nickname, etc.)
- Create, manage, list, and delete public and private channels
- Invite / ban users in public and private channels
- Provide an API so that a user of this client can react to 
incoming events. Should probably use the Mattermost websocket based API. 

# Licence

LGPL 3