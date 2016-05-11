@Grab(group='pircbot', module='pircbot', version='1.5.0')
@Grab(group='com.h2database', module='h2', version='1.3.168')
@Grab(group='org.jsoup', module='jsoup', version='1.8.3')
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')

import groovy.json.*
import org.jibble.pircbot.*
import java.sql.*
import groovy.sql.Sql
import org.h2.jdbcx.JdbcConnectionPool
import org.jsoup.Jsoup
import org.jsoup.helper.Validate
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*
import groovyx.net.http.HTTPBuilder

public class MyBot extends PircBot {
  JdbcConnectionPool connPool
  Connection conn

  public MyBot(){
    setName "LeakyBot"
  }

  @Override public void onMessage(String channel, String sender, String login, String hostname, String message){
    message=message.trim()
    if(message =~ /(?i)^inv/){
      if(message =~ /(?i:inv)\s([^\s]+)/){
        def reqUser = message =~ /(?i:inv)\s([^\s]+)/
        sendMessage channel, userInv(reqUser[0][1], "other")
      }else{
        sendMessage channel, userInv(sender, "self")
      }
    }else if(message =~ /(?i)weekday/){
        sendMessage channel, weekday()
    }else if(message =~ /(?i)^tossrando/){
      if(message =~ /(?i:tossrando)\s([^\s]+)/){
        def targetUsr = message =~ /(?i:tossrando)\s([^\s]+)/
        sendMessage channel, tossRando(sender, "give", targetUsr[0][1])
      }else{
        sendMessage channel, tossRando(sender,"drop", sender)
      }
    }else if(message =~ /(?i)^&fig/){
      def gifStuff = message =~ /(?i:^&fig)\s([^\s]+)\.\b(gifv|gif).*/
        if(gifStuff[0][2].equalsIgnoreCase("gif")|gifStuff[0][2].equalsIgnoreCase("gifv")){
          println "Yup found "+gifStuff[0][2]
          println gifStuff[0][1]
          sendMessage channel, revGif(gifStuff[0][1],gifStuff[0][2])
        }else{
          sendMessage channel, "This is not the format you're looking for. Maybe: &fig [URL].gif|gifv"
        }
    }else if(message =~ /(?i)^gift/){
        if(message =~ /(?i:^gift)\s([^\s]+) maditem/){
          def target = message =~ /(?i:^gift)\s([^\s]+) maditem/
          def madMix = wordGen("adjective") + " " + wordGen("noun")
          sendMessage channel, gift(sender,target[0][1],madMix)
        }else if(message =~ /(?i:^gift)\s([^\s]+) (.+)/){
          def given = message =~ /(?i:^gift)\s([^\s]+) (.+)/
          sendMessage channel, gift(sender, given[0][1], given[0][2])
        }else{
          sendMessage channel, "You may want to try this format> gift usernick something(or maditem)"
        }
    }else if(message =~ /(?i:^robinhood)/){
      if(message =~ /(?i:^robinhood)\s([^\s]+)/){
        def user = message =~ /(?i:^robinhood)\s([^\s]+)/
        sendMessage channel, robinHood(user[0][1], sender)
      }else{
        sendMessage channel, robinHood("LeakyBot", sender)
      }
    }else if(message =~ /(?i)^madlib$/){
        sendMessage channel, madlib()
    }else if(message =~ /(?i)^&help/){
        sendMessage channel, help()
    }else if(message =~ /(?i)\bcrap\b/){
      def chance = new Random().nextInt(10)
      if(chance >= 7){
        sendMessage channel, "@sed $sender s/crap/carp"}
        else{println "No crapping this time"}
    }else if(message =~ /(?i)^oprah/){
        if(message =~ /(?i:^oprah)\s(.+)/){
          def item = message =~ /(?i:^oprah)\s(.+)/
          sendMessage channel, oprah(channel, item[0][1])
        }else{
          sendMessage channel, "You may want to try this format> oprah [thing you want to give]"
        }
    }
  }

  //@sed nick s/wordtoreplace/replacewith

  def revGif(String gifUrl, String ext){
    println "http://ezgif.com/reverse?url=$gifUrl.$ext"
    Document doc = Jsoup.connect("http://ezgif.com/reverse?url=$gifUrl.$ext").get()
    Elements ezgifNewImgNameElem = doc.select('[name="file"]')
    def ezgifNewImgName = ezgifNewImgNameElem.attr("value").toString()
    Elements ezgifTokenElem = doc.select('[name="token"]')
    def ezgifToken = ezgifTokenElem.attr("value").toString()
    println "From http://ezgif.com/reverse?url=$gifUrl.$ext \nI'm posting file name param: $ezgifNewImgName\nToken param: $ezgifToken\nas form to http://ezgif.com/effects"
    def client = new RESTClient("http://ezgif.com/effects")
    client.getClient().getParams().setParameter("http.connection.timeout", new Integer(45000))
    client.getClient().getParams().setParameter("http.socket.timeout", new Integer(45000))
    client.headers = [Accept : 'application/xml']
    def response = client.post(
        body: [file:"$ezgifNewImgName", token:"$ezgifToken", reverse:"on", "no-repeat":"on"],
        contentType: TEXT,
        requestContentType:"application/x-www-form-urlencoded; charset=UTF-8"
    )
    def imgName = response.data.text =~ /"\/\/(.*?)"/
    return wordGen("noun")+" "+wordGen("verb")+" "+wordGen("adjective")+" "+wordGen("noun")+", might be a good title for this gif: http://" + imgName[0][1]
  }

  def robinHood(String user, String robber){
    if(new Random().nextInt(8) > new Random().nextInt(10)){
      ResultSet rs = rsReturn("SELECT ITEM FROM INVENTORY WHERE USER='$user' ORDER BY RAND() LIMIT 1;")
      if(rs.isBeforeFirst()){
        rs.next()
        def item = rs.getString("ITEM")
        execOrder("DELETE TOP 1 FROM INVENTORY WHERE USER='$user' AND ITEM='$item';")
        execOrder("INSERT INTO INVENTORY (USER, ITEM) VALUES('$robber','$item');")
        return "$robber has sneakily sneaked $item from $user...*sniff sniff* is that payback I smell in the air?"
      }else{
        return "They've nothing to their name. Robbing the poor? Not very Robin Hood-like of you! Suffer justice!! " + tossRando(robber,"give",user)
      }
    }else{
      def fail = ["$user saw you coming a mile away...s/saw/smelled... No item for you!",
               "You 1d20 a 1, grats, your pickpocket skills are so bad your hands fall off. #mercilessGM",
               "Tsk tsk, this is no way to make friends",
               "$user saw a box creeping up next to him. $user sets the box on fire. The box then emits a faint cry for...snakes? ",]
      return "You were caught! "+fail[new Random().nextInt(fail.size)]
    }
  }

  def oprah(String channel, String item){
    def users = getUsers(channel)
    users.each{ user->
      execOrder("INSERT INTO INVENTORY (USER, ITEM) VALUES('$user','$item');")
    }
    def audience = users.join(", ")
    return "$item has been given to $audience. #makeitrain"
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

  def help(){
    def botCommands = ["weekday",
                    "inv [someone]",
                    "tossrando [someone]",
                    "gift (someone) (something or maditem)",
                    "madlib",
                    "oprah (something)",
                    "robinhood [someone]",
                    "&fig (URL) (title) //15MB limit"]
    return "My abilities are: "+botCommands.join('. ')
  }

  def userInv(String user, String source){
    ResultSet rs = rsReturn("SELECT ITEM FROM INVENTORY WHERE USER='$user';")
    if(source.equalsIgnoreCase("self")){
      if (!rs.isBeforeFirst()){
        return "Dang $user, you got a phat goose egg, nada, zilch, just one big inventory void :/"
      }else{
        def items = []
        while(rs.next()){
          items += rs.getString("ITEM")
        }
        return "$user, so far you've accumulated: "+items.join(', ')
      }
    }else{
      if (!rs.isBeforeFirst()){
        return "Looks like $user has no inventory :/ Be a good chap and toss them something"
      }else{
        def items = []
        while(rs.next()){
          items += rs.getString("ITEM")
        }
        def listing = ["At anytime $user could pull out: "+items.join(', '),
                       "$user's man-purse contains: "+items.join(', '),
                       "Watch out $user has a(n): "+items.join(', '),
                       "Most of these would go good with pancakes and syrup: "+items.join(', ')]
        return listing[new Random().nextInt(listing.size)]
      }
    }
  }

  def tossRando(String source, String action, String target){
    ResultSet rs = rsReturn("SELECT ITEM FROM INVENTORY WHERE USER='$source' ORDER BY RAND() LIMIT 1;")
    if(!rs.isBeforeFirst()){
      return "yeah...'bout that, there seems to be an empty...just one big empty."
    }else{
      rs.next()
      def dropee = rs.getString("ITEM")
      if(action.equalsIgnoreCase("drop")){
        execOrder("DELETE TOP 1 FROM INVENTORY WHERE USER='$source' AND ITEM='$dropee';")
        def info = ["Wooop, there goes $dropee",
                    "Ha! Look $source dropped $dropee!!",
                    "A ceremonial goodbye for the dropped $dropee",
                    "Yeah you probably didn't want $dropee anyway",]
        return info[new Random().nextInt(info.size)]
      }else{
        if(source.equalsIgnoreCase(target)){
          return "Ah, just juggling things to yourself eh? That's cool"
        }else{
          execOrder("DELETE FROM INVENTORY WHERE USER='$source' AND ITEM='$dropee';")
          execOrder("INSERT INTO INVENTORY (USER, ITEM) VALUES('$target','$dropee');")
          return  "$target now has $source's $dropee"
        }
      }
    }
  }

  def weekday() {
      def day = new java.util.Date().getDay()
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
              return "Woo week's half done...still have half left...a pessimistic example of half empty being good, and half full being bad"
          break
          case 4:
              return "Ah Thursday, a day that will always live in the shadow of the mighty Friday"
          break
          case 5:
              return "Hello Friday my old friend"
          break
          case 6:
              return "No workie! Make with the weekending"
          break
      }
  }

  def wordGen(String wordType){
    def endpoint = "http://api.wordnik.com:80/v4/words.json/randomWords?"
    def params = [includePartOfSpeech: "$wordType",
                  hasDictionaryDef: "true",
                  minLength: 3,
                  maxLength: 8,
                  limit: 10,
                  api_key: "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5"]

    def qs = params.collect {it}.join('&')
    def jsonTxt = "$endpoint$qs".toURI().toURL().text

    def json = new JsonSlurper().parseText(jsonTxt)
    def words = json.word
    return words[new Random().nextInt(words.size)]
  }

  def madlib(){
    def noun = wordGen("noun")
    def noun2 = wordGen("noun")
    def adj = wordGen("adjective")
    def adj2 = wordGen("adjective")
    def verb = wordGen("verb")
    def verb2 = wordGen("verb")
    def phrases = ["$noun doesn't always $verb, huh...$adj keyboards seem to be $verb2",
                   "Sorry I've been $verb, did you need $noun for something?",
                   "Heh, a(n) $adj $verb how most $adj2. $verb2 to see $noun",
                   "Well, I'm not sure but that looks like $adj $noun if I ever did see one",
                   "Yeah you should probs $adj $verb, and like soon. The $noun is $adj2",
                   "Ahhhh, $adj $noun, the best way to $verb yet another $adj2 $noun2 while $verb2"]
    return phrases[new Random().nextInt(phrases.size)]
  }

  def dbSetup(){
    connPool = JdbcConnectionPool.create("jdbc:h2:~/cris/IRCbot/LeakyBotDB", "idam", "aPw")
    conn = connPool.getConnection()
    Statement stat = conn.createStatement()
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
  }
}

  def bot = new MyBot()
  bot.dbSetup()
  bot.setVerbose true
  bot.connect "irc.freenode.net"
  bot.joinChannel "#bitswebteam"
