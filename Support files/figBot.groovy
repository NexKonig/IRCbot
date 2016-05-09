@Grab(group='org.jsoup', module='jsoup', version='1.8.3')
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')

import org.jsoup.Jsoup
import org.jsoup.helper.Validate
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*
import groovyx.net.http.HTTPBuilder

class figBot{

  static void main(String[] args){
    def gifUrl = "https://i.imgur.com/pnEjCLU.gifv"
    def url = "http://ezgif.com/reverse?url=$gifUrl"
    parser(url)

  }

  static parser(String url){
    Document doc = Jsoup.connect(url).get()
    Elements gifLink = doc.select('[name="file"]')
    def fileName = gifLink.attr("value").toString()
    println fileName
    Elements token = doc.select('[name="token"]')
    def tokenItem =token.attr("value").toString()
    println tokenItem

    def newURL = "http://ezgif.com/reverse/$fileName"
    println newURL
    getRevUrl(fileName,tokenItem)
  }

  static getRevUrl(String file, String token){

    def client = new RESTClient("http://ezgif.com/effects")
    client.getClient().getParams().setParameter("http.connection.timeout", new Integer(60000))
    client.getClient().getParams().setParameter("http.socket.timeout", new Integer(60000))
    //client.contentType = TEXT
    client.headers = [Accept : 'application/xml']
    def postResponse = client.post(
        body: [file:"$file", token:"$token", reverse:"on", "no-repeat":"on"],
        contentType: TEXT,
        requestContentType:"application/x-www-form-urlencoded; charset=UTF-8"
    )
    //println response.data.toString()
    def imgName = postResponse.data.text =~ /"\/\/(.*?)"/
    println "${imgName[0][1]}"
  }
}
