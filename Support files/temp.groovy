import groovy.json.*

def wordGen(String wordType, String excludeType){
  def endpoint = "http://api.wordnik.com:80/v4/words.json/randomWords?"
  def params = [includePartOfSpeech: "$wordType",
                excludePartOfSpeech: "$excludeType",
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

def noun1 = wordGen("noun","adjective")
def noun2 = wordGen("noun","adjective")
def verb1 = wordGen("verb","adjective")
def verb2 = wordGen("verb","adjective")
def adj1 = wordGen("adjective","verb")
def adj2 = wordGen("adjective","verb")

println "After a lot of $verb1 we decided to grab the $adj1 $noun1 and $verb2. All was well until we were stopped by the $adj2 $noun2"



//message = This #noun is #adjective. I really want to #verb but my #adjective #noun is in the way.