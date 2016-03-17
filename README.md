# bingo

First, a disclaimer; I'm not a html designer.  I won't pretend that I'm any good at html design.  That's why, in the screenshot below, the font is all the same and the colors are pretty bland.  However I should be pretty good at writing web applications (not quite the same thing), and this was written using React.  

![Screenshot](./screenshot.png)

Anyway, the front-end is almost besides the point.  I wrote bingo in order to have a test bed for performance testing.  Don't expect tests in this repository.  This is not production code. The aim here was to develop a bingo application that could support 1000 concurrent users on a reasonably powerful machine.

### Concurrent User

First lets be clear on what we mean by 'concurrent' user.  When a user loads a bingo client, that bingo client will attempt to maintain the illusion of realtime updates of the game state for the user. This means that it polls the server for updates every 200 milliseconds.  And there is a chat client built into this application which polls for updates every 500 milliseconds.  So for every user that has a client active, there are seven requests per second that occur in order to keep each client in a game room current.

So the activity level of these users is much greater than a user going to a popular online mobile phone store and buying a new phone and tariff.  There may be one interaction every 30 seconds in that case.


### Testing environment

The other thing to bear in mind with these results is that I ran my test client on the same machine as the application and database server. Everything is on the same machine.  So these tests understate what is possible from that perspective.  

On the other hand, I'm not runing a true cluster.  Transactions and game state is persisted in mongo (FSYNCED), so by clustering I don't anticipate a terrificly additional performance cost.  Chat however is currently only using an in-memory solution, which is not appropriate. That said, there will be some impact on performance due to clustering. 

If I every get around to it, I will add in a JMS message broker to create a 'chat bus' which won't be durable, but I will log chat messages to specific log files.  Chat is a bit funny.  We don't absolutely need 100% reliability in delivering messages, but if someone swears or is abusive, we want to be able to track the user that said whatever it was that they said.  Hence the specific logs files for traceabilty and hence the non-durability of JMS topic.  Durable topics are quite dangerous as well.  This is because there needs to be a catalog of known nodes and if there are any nodes that haven't picked up messages, then the message broker needs to keep broadcast data which is normally very high in volume until the missing node comes alive again.  If a node is renamed, the messages will never be cleared, and the broker will eventually die.  Also a chat message is only usefull to someone if it is going to be presented in the chat pane, which has a limited screensize, so I feel that a chat message only has a usefull life of about a minute.  So durable message delivery is really not necesssary.

###Machine Spec

So at this point, I should probably mention the environment specifics.

|Item       | Details                                     |
|-----------|---------------------------------------------|
| Processor | Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz    |
| Memory    | 16GB                                        |
| Disk      | 2 Vertex SSD drives in Raid 0 configuration |
| OS        | Centos 7                                    |

The disk access is the real star here.  This RAID configuration doubles the disc access speed for an SSD drive which is already very quick. Is this cheating?  No, any production setup will run a proper RAID setup which will beat even this.

    [root@localhost proc]# hdparm -Tt /dev/md126
    /dev/md126:
     Timing cached reads:   28458 MB in  2.00 seconds = 14256.42 MB/sec
     Timing buffered disk reads: 1922 MB in  3.00 seconds = 640.32 MB/sec

So the drive setup is about 10 times faster than a normal SATA drive.

###Success

So anyway, it goes without saying that I have managed it.  (Or else I would hardly feel like publishing the results)

And along the way, I wrote my own performance testing tool called Hound.  You can download the report here.

[Results](./report.tar.gz)



 
 


