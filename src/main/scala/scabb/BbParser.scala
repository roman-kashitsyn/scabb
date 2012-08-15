package scabb

import scala.xml._
import scala.util.parsing.combinator._

trait BbParser {
  def toHtml(input: String): NodeSeq
}

object BbParser extends BbParser {

  def toHtml(input: String): NodeSeq = new BbParsers().parseString(input).toHtml

  class BbParsers extends RegexParsers with ImplicitConversions {

    def parseString(input: String): BbAst =
      parseAll(bbCode, input).getOrElse(FormattedText(input))

    lazy val attrChars: Parser[String] = rep1(notClosedBracket) ^^
      { case chars => chars.mkString("") }

    def startTag(name: String) = '[' ~> name <~ ']'
    def startAttributedTag(name: String) = '[' ~> (name ~ opt('=' ~> attrChars)) <~ ']'
    def closeTag(name: String) = '[' ~ '/' ~> name <~ ']'

    def simpleTag(name: String, htmlName: Option[String]): Parser[BbAst] =
      startTag(name) ~> rep1(bbCode) <~ closeTag(name) ^^
        { case innerCode => SimpleTag(name, htmlName, innerCode) }

    lazy val bbCode: Parser[BbAst] = italic | bold | underline | striked | url | code | formatted

    // Simple tags
    lazy val italic = simpleTag("i", None)
    lazy val bold = simpleTag("b", None)
    lazy val underline = simpleTag("u", None)
    lazy val striked = simpleTag("s", Some("del"))

    // Text
    lazy val formatted = rep1(notOpenBracket) ^^ { case chars => FormattedText(chars.mkString) }
    lazy val raw = rep1(notCode) ^^ { case chars => RawText(chars.mkString("")) }

    // Attributed tags
    lazy val code = (startAttributedTag("code") ~ raw) <~ closeTag("code") ^^
      { case name ~ maybeAttr ~ rawCode => CodeTag(maybeAttr, rawCode) }
    lazy val url = (startAttributedTag("url") ~ formatted) <~ closeTag("url") ^^
      { case name ~ maybeAttr ~ contents => LinkTag(maybeAttr, contents) }

    // Chars flavors
    lazy val notOpenBracket: Parser[Char] = elem("simple char", c => c != '[' && !Character.isISOControl(c))
    lazy val notClosedBracket: Parser[Char] = elem("simple char", c => c != ']' && !Character.isISOControl(c))
    lazy val printableChar: Parser[Char] = elem("printable char", !Character.isISOControl(_))
    lazy val notCode: Parser[Char] = not("[/code]") ~> printableChar
  }
}