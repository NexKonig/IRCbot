@Grab(group='pircbot', module='pircbot', version='1.5.0')
@GrabConfig(systemClassLoader=true)
@Grab(group='com.h2database', module='h2', version='1.3.168')

import java.sql.*
import groovy.sql.Sql
import org.jibble.pircbot.*
import org.h2.jdbcx.JdbcConnectionPool


public class MyBot extends PircBot {
    public MyBot() {
        setName "SuperAwesomeBot"
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
    //&time gives the time
        if (message.equalsIgnoreCase("&time")) {
            def time = new Date()
            sendMessage channel, "$sender: The time is now $time. Woo."
    //Gets 'triggered' by "You people"
        }else if (message =~ /(?i)you people/) {
            sendMessage channel, "$sender: What do you mean, \"YOU PEOPLE\"?!"
    //Mention "weekday" and bot will let you know how he feels about it
        }else if (message =~ /(?i)weekday/){
            sendMessage channel, "$sender: " + weekday()
    //Say givecandy to add to the bot's candy stash
        }else if (message =~ /(?i)^givecandy (.+)$/){
            def givenCandy = message =~ /(?i)^givecandy (.+)$/
            sendMessage channel, addCandy(givenCandy[0][1], sender, nick)
    //Mentioning "candy" will get ya some
        }else if (message =~ /(?i)candy$/){
                sendMessage channel, getCandy(sender)
    //Saying "candybag" will get you a list of what's in the stash
        }else if (message =~ /(?i)candybag$/){
                sendMessage channel, candyBag()
    //If tweet is mentioned, bot will make a remark
        }else if (message =~ /(?i)tweet/){
                sendMessage channel, tweet()
        }else if (message =~ /(?i)^addtweetreply (.+)/){
            def reply = message =~ /(?i)^addtweetreply (.+)/
                sendMessage channel, addTweetReply(reply[0][1])
        }
    }
//------------------------------If someone wants some candy
    def getCandy(String sender){
        ResultSet rs = tblQuery("SELECT * FROM CANDYBAG;")
        def count = tblQuery("SELECT COUNT(*) FROM CANDYBAG;")
        println "the rows count is "+count
            if(!rs.next()){
                return "Sorry "+sender+", bag's empty :/"
            }else{
                def idx = new Random().nextInt(count)
                def randomCandy = (rs.absolute(idx))
                rs.deleteRow()
                return "Here ya go "+sender+", a nice "+randomCandy+"! Just for you"
            }
    }
//------------------------------Candy given

//------------------------------Adding some candy to the pile (array) and sending back a thank you
    def addCandy(String candy, String sender, String nick){
        sqlInsert(candy)
        def thanksMsg = ["OoOoOo piece of candy!",
                      "AWW "+sender+" you shouldn't have! You're such a sweetheart",
                      "Oh yay another "+candy+", what a joyous occasion",
                      "*eats "+candy+" violently* Yo man, got any moar dem "+candy+"?",
                      "Master has given "+nick+" a "+candy+"..."+nick+" is free!"]

        return thanksMsg[new Random().nextInt(thanksMsg.size)]
    }
//------------------------------Done adding candy

//------------------------------List of candy in the candyBag[]
    def candyBag(){
        rs = stat.executeQuery("SELECT * FROM CANDYBAG;")
        if(!rs.next()){
            return "Huh, drier than a desert... on a hot, dry, day...I am bot, not poet."
        }else{
            def items = []
            while (rs.next()) {
                items += rs.getString("CANDY")
            }
            return "Here's what I got: "+ items.join(', ')
        }
    }
//------------------------------List returned

//------------------------------Tweet reply array
    def tweetReplies = ["OoOoo neato!","Huh didn't know that",
                        "SNAP! gonna keep that one handy",
                        "MmmMmkay I'll use that at the bar tonight and let you know where it gets me",
                        "I may have my reservations with some of that content",
                        "Sounds good, I'm gonna give it a go"]

//------------------------------Add tweet reply
    def addTweetReply(String reply){
        tweetReplies += reply;
        return ":/ hmm k got it, my replies aren't good enough for ya, no probs I'll just add your ever insightful words of wisdom to my list....jerk :'("
    }
//------------------------------End add tweet reply

    def tweet(){
        return tweetReplies[new Random().nextInt(tweetReplies.size)]
    }
//------------------------------End bird watcher

//------------------------------Response to weekday inquiry
    def weekday() {
        def day = new Date().getDay()
        switch(day) {
            case 0:
                return "No, stop! Go away!!! It's Sunday, why do you do this? Leave me be!"
            break
            case 1:
                return "T.T ugh, nnnooooo....uuuuggghhhhhh *sigh* Mondays: the most hellish of all humps"
            break
            case 2:
                return "Tuesdays never seem close enough to Fridays"
            break
            case 3:
                return "Woo week's half done...still have half left...good example of half empty being optimistic, bt half full being pessimistic"
            break
            case 4:
                return "Ah Thursday, the ****job of weekdays, it's nice, but we all know what we really want"
            break
            case 5:
                return "Yay it's Bakeday!"
            break
            case 6:
                return "No worky, make with the weekending"
            break
        }
    }
//------------------------------End weekday response
}
//-------------------------------H2 Db setup
//def sql = Sql.newInstance("jdbc:h2:~/candyList", "sa", "", "org.h2.Driver")
//sql.execute("CREATE TABLE IF NOT EXISTS CANDYBAG(CANDY VARCHAR(255));")

JdbcConnectionPool cp = JdbcConnectionPool.create("jdbc:h2:~/candyList", "sa", "")
Connection conn = cp.getConnection()
Statement stat = conn.createStatement()
stat.execute("CREATE TABLE IF NOT EXISTS CANDYBAG(CANDY VARCHAR(255));")
ResultSet rs

def tblQuery(String cmd){
    return stat.executeQuery(cmd)
}

def sqlInsert(String item){
    stat.execute("INSERT INTO CANDYBAG VALUES('"+item+"');")
}

//Setup MyBot and connection variables
def bot = new MyBot()
bot.setVerbose true
bot.connect "irc.freenode.net"
bot.joinChannel "#silly-bot-test"
