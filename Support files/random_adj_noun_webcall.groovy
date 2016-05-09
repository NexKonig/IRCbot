import groovy.json.*

def endpoint = "http://api.wordnik.com/v4/words.json/search/%3F?"

def nounPull = [includePartOfSpeech: "noun",
                minLength: 5,
                maxLength: 7,
                limit: 30,
                api_key: "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5"]

def nounQs = nounPull.collect {it}.join('&')
def nounJsonTxt = "$endpoint$nounQs".toURI().toURL().text

def nounJson = new JsonSlurper().parseText(nounJsonTxt)
def nouns = nounJson.searchResults.word


def adjPull = [includePartOfSpeech: "adjective",
               minLength: 5,
               maxLength: 12,
               limit: 30,
               api_key: "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5"]

def adjQs = adjPull.collect {it}.join('&')
def adjJsonTxt = "$endpoint$adjQs".toURI().toURL().text

def json = new JsonSlurper().parseText(adjJsonTxt)
def adjs = json.searchResults.word

def noun = []

nouns.each {n ->
    noun += n
}

def adj = []
adjs.each{a ->
    adj += a
}

println "Here, take this "+adj[new Random().nextInt(adj.size)]+" "+noun[new Random().nextInt(noun.size)]
println "Here, take this "+adj[new Random().nextInt(adj.size)]+" "+noun[new Random().nextInt(noun.size)]
println "Here, take this "+adj[new Random().nextInt(adj.size)]+" "+noun[new Random().nextInt(noun.size)]
println "Here, take this "+adj[new Random().nextInt(adj.size)]+" "+noun[new Random().nextInt(noun.size)]
println "Here, take this "+adj[new Random().nextInt(adj.size)]+" "+noun[new Random().nextInt(noun.size)]