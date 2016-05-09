@Grab(group='pircbot', module='pircbot', version='1.5.0')
import org.jibble.pircbot.*

public class MyBot extends PircBot {
    public MyBot() {
        setName "SuperAwesomeBot"
    }

    @Override
    public void onMessage(String channel, String sender,
                          String login, String hostname, String message) {
        // &time gives the time
        if (message.equalsIgnoreCase("&time")) {
            def time = new Date()
            sendMessage channel, "$sender: The time is now $time. Woo."
            //Gets 'triggered' by "You people"
        }else if (message =~ /(?i)you people/) {
            sendMessage channel, "$sender: What do you mean, \"YOU PEOPLE\"?!"
            //Mention "weekday" and bot will let you know how he feels about it
        }else if (message =~ /(?i)weekday/) {
            sendMessage channel, "$sender: " + weekday()
            //Say givecandy to add to the bot's candy stash
        }else if (message =~ /(?i)^givecandy (.+)$/) {
            def givenCandy = message =~ /(?i)^givecandy (.+)$/
            sendMessage channel, addCandy(givenCandy[0][1], sender, nick)
            //Mentioning "candy" will get ya some
        }else if (message =~ /(?i)candy$/) {
            sendMessage channel, getCandy(sender)
            //Saying "candybag" will get you a list of what's in the stash
        }else if (message =~ /(?i)candybag$/) {
            sendMessage channel, candyBag()
        // }else if (message =~ /(?i)^\tweet/) {
        //     sendMessage channel, tweet()
        }else if (message =~ /(?i)^addtweetreply (.+)$/) {
            def reply = message =~ /(?i)^addtweetreply (.+)/
                sendMessage channel, addTweetReply(reply[0][1])
        }
    }

//-----------------------------Candy stuff goes here
//-----------------------------Setup empty candy array
    def candyList = []

//-----------------------------Calling for candy
    def getCandy(String sender){
        if(candyList.size <= 0){
            return "Sorry "+sender+", bag's empty :/"
        }else{
            def idx = new Random().nextInt(candyList.size)
            def randomCandy = (candyList[idx])
            candyList.remove(idx)
            return "Here ya go "+sender+", a nice "+randomCandy+"! Just for you"
        }
    }
//-----------------------------End candy call

//-----------------------------Adding some candy to the pile (array) and sending back a thank you
    def addCandy(String candy, String sender, String nick){
        candyList += candy

        def thanksMsg = ["OoOoOo piece of candy!",
                         "AWW "+sender+" you shouldn't have! You're such a sweetheart",
                         "Oh yay another "+candy+", what a joyous occasion",
                         "*eats "+candy+" violently* Yo man, got any moar dem "+candy+"?",
                         "Master has given "+nick+" a "+candy+"..."+nick+" is free!"]

        return thanksMsg[new Random().nextInt(thanksMsg.size)]
    }
//-----------------------------End add and thank you

//-----------------------------List of candy in the array
    def candyBag(){
        if(candyList.size <= 0){
            return "Heh, emptier than a *insert PC,PR,and HR worse nightmare quote here* ammirite folks?!?!"
        }else{
            return "Let's see what have we here...hhmmm..AHA here we are: "+ candyList.join(', ')
        }
    }
//-----------------------------End candy listing

//------------------------------Watch for tweets
    def tweetReplies = ["OoOoo neato!","Huh didn't know that",
                        "SNAP! gonna keep that one handy",
                        "MmmMmkay I'll use that at the bar tonight and let you know where it gets me",
                        "I may have my reservations with some of that content",
                        "Sounds good, I'm gonna give it a go"]
    def tweet(){
        return tweetReplies[new Random().nextInt(tweetReplies.size)]
    }
//-----------------------------End tweet watcher

//------------------------------Add tweet reply
    def addTweetReply(String reply){
        tweetReplies += reply;
        return ":/ hmm k got it, my replies aren't good enough for ya, no probs I'll just add your ever insightful words of wisdom to my list....jerk :'("
    }
//------------------------------End add tweet reply

//------------------------------Response to weekday inquiry
    def weekday = {
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
                return "Ah Thursday, a day that will always live in the shadow of the mighty Friday"
                break
            case 5:
                return "Yay it's Bakeday!"
                break
            case 6:
                return "No worky, make with the weekend and knock out your to-do list"
                break
        }
    }
//------------------------------End weekday response

}

//------------------------------Setup MyBot and connection variables
def bot = new MyBot()
bot.setVerbose true
bot.connect "irc.freenode.net"
bot.joinChannel "#silly-bot-test"
