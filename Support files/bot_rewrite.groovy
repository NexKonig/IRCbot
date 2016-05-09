@Grab(group='pircbot', module='pircbot', version='1.5.0')
@Grab(group='com.h2database', module='h2', version='1.3.168')

import org.jibble.pircbot.*
import java.sql.*
import groovy.sql.Sql
import org.h2.jdbcx.JdbcConnectionPool

public class MyBot extends PircBot {
  JdbcConnectionPool connPool
  Connection conn

  public MyBot(){
    setName "LeakyBot"
  }

  @Override public void onMessage(String channel, String sender, String login, String hostname, String message){
    message=message.trim()
    if (message.equalsIgnoreCase("&time")){
      def time = new java.util.Date()
      sendMessage channel, "$sender: The time is now $time."

    }else if (message =~ /(?i)^givecandy/){
      def givenCandy = message =~ /(?i)^givecandy (.+)$/
      sendMessage channel, addCandy(givenCandy[0][1], sender, nick)
    }else if (message =~ /(?i)candybag/){
      sendMessage channel, "$sender: " + candyBagContents()

    }else if (message =~ /(?i)^myinv$/){
      sendMessage channel, inventory(sender)
    }else if (message =~ /(?i)^gift/){ //Triggers on gift, captures anything after
        if(message =~ /(?i:^gift)\s([^\s]+)\:(.+)/){
            def given = message =~ /(?i:^gift)\s([^\s]+)\:(.+)/ //Groups on gift, captures nick:item
            sendMessage channel, gift(sender, given[0][1], given[0][2])
        }else{
          sendMessage channel, "You may want to try this format> gift usernick:something"
        }
    }else if (message =~ /(?i)^inv/){ //Triggers on inv, captures all
      if(message =~ /(?i:inv)\s([^\s]+)/){
        def reqUserInv = message =~ /(?i:inv)\s([^\s]+)/ //Groups on inv, captures nick
        sendMessage channel, userInv(reqUserInv[0][1])
      }else{
        sendMessage channel, "You may want to try this format> inv usernick (note: No spaces in nicks)"
      }
    }else if (message =~ /(?i)^candy/){
        sendMessage channel, getCandy()
    }else if (message =~ /(?i)^what can leakybot do/){
      sendMessage channel, help()
    }
  }
  
  def help(){
    return "My abilities -in [syntax]:action format- are: 1-['&time']:Displays time and date. 2-['givecandy something']:Gives me a piece of candy to store in my candybag. 3-['candybag']:Lists contents of my candy bag. 4-['myinv']:Lists items in your Inventory. 5-['gift someone:something']:Gives someone a something. 6-['inv someone']:Lists someone's inventory. 7-['what can leakybot do']:...welp, here ya are!"
  }

//Inventory stuff
  def inventory(String user){
    ResultSet rs = rsReturn("SELECT ITEM FROM INVENTORY WHERE USER='$user';")
    if (!rs.isBeforeFirst()){
      return "Dang $user, you got a phat goose egg, nada, zilch, just one big inventory void :/"
    }else{
      def items = []
      while(rs.next()){
        items += rs.getString("ITEM")
      }
      return "$user, so far you've accumulated: "+items.join(', ')
    }
  }

  def userInv(String user){
    ResultSet rs = rsReturn("SELECT ITEM FROM INVENTORY WHERE USER='$user';")
    if (!rs.isBeforeFirst()){
      return "Looks like $user has no inventory :/ Be a good chap and toss them something"
    }else{
      def items = []
      while(rs.next()){
        items += rs.getString("ITEM")
      }
      def listing = ["Struggling to dig into the pits of my trench coat I present $user with: "+items.join(', '),
                     "As $user tips his fedora, you notice the following in his man-purse: "+items.join(', '),
                     "Watch out $user's got: "+items.join(', '),
                     "Most of these would go good with pancakes and syrup: "+items.join(', '),
                     "",
                     "",]
      return listing[new Random().nextInt(listing.size)]
    }
  }

  def gift(String sender, String receiver, String item){
    execOrder("INSERT INTO INVENTORY (USER, ITEM) VALUES('$receiver','$item');")
    def praises = ["Aww I'm so glad to see $sender and $receiver getting along",
                   "Ah yes the gift of $item, that keeps on giving",
                   "Wow a whole $item?!?! Dang $receiver you must mean a lot to $sender",
                   "Really $sender, you're giving $receiver $item ...I thought you were better than that",
                   "Hey guess what $receiver!! You've been given a 4year old crusty $item",
                   "And on the conclusion of this joyous occasion $sender has bestowed upon $receiver $item"]
    return praises[new Random().nextInt(praises.size)]
  }

//Candy stuff
  def candyBagContents(){
    ResultSet rs = rsReturn("SELECT * FROM CANDYBAG;")
    if (!rs.isBeforeFirst()){
      return "You must construct additional pixie-sticks"
    }else{
      def items = []
      while(rs.next()){
        items += rs.getString("CANDY")
      }
      return "Here's what I have so far- "+items.join(', ') +".But you know, feel free to like, givecandy anytime"
    }
  }

  def addCandy(String candy, String sender, String botName){
    //Statement stat = conn.createStatement()
    //stat.execute("INSERT INTO CANDYBAG VALUES('$candy')")
    execOrder("INSERT INTO CANDYBAG VALUES('$candy');")
      def thanksMsg = ["OoOoOo piece of candy!",
                       "AWW $sender you shouldn't have! You're such a sweetheart",
                       "Oh yay another $candy, what a joyous occasion",
                       "*eats $candy violently* Yo $sender, got any moar dem $candy?",
                       "Master has given $nick $candy...$nick is free!"]

      return thanksMsg[new Random().nextInt(thanksMsg.size)]
  }


    def getCandy(){
      ResultSet rs = rsReturn("SELECT CANDY FROM CANDYBAG ORDER BY RAND() LIMIT 1;")
      if(rs.next()){
        def candy = rs.getString("CANDY")
        execOrder("DELETE FROM CANDYBAG WHERE CANDY='$candy';")
        return "Here's a $candy for ya ^.^"
      }else{
        return "Sorry pal, bag's empty"
      }
    }

    def dbSetup(){
      connPool = JdbcConnectionPool.create("jdbc:h2:~/LeakyBotDB", "idam", "aPw")
      conn = connPool.getConnection()
      Statement stat = conn.createStatement()
      stat.execute("CREATE TABLE IF NOT EXISTS CANDYBAG(CANDY VARCHAR(255));")
      println "Candybag table confirmed"
      stat.execute("CREATE TABLE IF NOT EXISTS INVENTORY(USER VARCHAR(255), ITEM VARCHAR(255));")
      println "Inventory table confirmed"
    }
    def rsReturn(String query){
      Statement stat = conn.createStatement()
      return stat.executeQuery(query)
    }
    def execOrder(String order){
      Statement stat = conn.createStatement()
      stat.execute(order)
      println("Order executed successfully")
    }
}


def bot = new MyBot()
bot.dbSetup()
bot.setVerbose true
bot.connect "irc.freenode.net"
bot.joinChannel "#silly-bot-test"
