package scabb

import org.specs2.mutable._
import scala.xml.Text

class BbSpec extends Specification {

  import BbParser._

  "The BB parser" should {

    "understand simple formatted text" in {
      toHtml("one thing\nanother thing") must ==/(
          Text("one thing") ++ <br/> ++ Text("another thing")).ordered
    }

    "deal with text in bold face" in {
      toHtml("[b]bold text[/b]") must ==/(<b>bold text</b>)
    }

    "deal with italic text" in {
      toHtml("[i]italic text[/i]") must ==/(<i>italic text</i>)
    }

    "deal with underlined text" in {
      toHtml("[u]underlined text[/u]") must ==/(<u>underlined text</u>)
    }

    "deal with striked text" in {
      toHtml("[s]striked text[/s]") must ==/(<del>striked text</del>)
    }

    "deal with different text decorations interchanged" in {
      toHtml("[b][i][u][s]overdecorated[/s][/u][/i][/b]") must ==/(
          <b><i><u><del>overdecorated</del></u></i></b>)
      toHtml("[b]some [i]italic[/i] and [s]deleted[/s] text[/b]") must ==/(
          <b>some <i>italic</i> and <del>deleted</del> text</b>)
    }

    "deal with code blocks" in {
      toHtml("[code]val x = 5[/code]") must ==/(<code>val x = 5</code>)
      toHtml("[code=haskell]main = getLine >> putStrLn[/code]") must ==/(
            <code class="haskell">main = getLine >> putStrLn</code>)
    }

    "deal with links" in {
      toHtml("[url]http://google.com[/url]") must ==/(
          <a href="http://google.com">http://google.com</a>)
      toHtml("[url=http://google.com]Google[/url]") must ==/(
          <a href="http://google.com">Google</a>)
    }

    "work greacefully with unclosed tags" in {
      toHtml("[b]closed [/b] or not [i]") must ==/(<b>closed </b> ++ Text(" or not [i]"))
    }

  }
}