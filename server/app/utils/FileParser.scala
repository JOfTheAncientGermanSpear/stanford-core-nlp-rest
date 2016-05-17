package utils

import java.io.{OutputStreamWriter, ByteArrayOutputStream, File}

import org.apache.tika.detect.DefaultDetector
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.sax.BodyContentHandler
import play.api.libs.json.JsValue

object FileParser extends ITextParser with TextParserJson {

  case class StringProcessFns(filter:Traversable[String] => Traversable[String] = identity,
                                 split:String => Traversable[String] = l => l.split("\n"),
                                 join:Traversable[String] => String = l => l.mkString("\n")) {
    def process(input: String) : String = join(filter(split(input)))
  }

  def _fileToString(filePath:String,
                     processFns: StringProcessFns = new StringProcessFns()
                     ): String = {
    val file = new File(filePath)
    val url = file.toURI.toURL

    val metadata = new Metadata()
    val input = TikaInputStream.get(url, metadata)

    val outputStream = new ByteArrayOutputStream()

    val outputWriter = new OutputStreamWriter(outputStream, "UTF-8")

    val handler = new BodyContentHandler(outputWriter)

    val p = new AutoDetectParser(new DefaultDetector())
    val context = new ParseContext()
    context.set(classOf[Parser], p)
    p.parse(input, handler, metadata, context)

    input.close()
    outputStream.flush()


    processFns.process(outputStream.toString("UTF-8"))

  }

  def fileToJson(filePath: String, processFns: StringProcessFns = new StringProcessFns()):JsValue = {
    val document = parse_text(_fileToString(filePath, processFns))
    convert_document(document)
  }

  def example(filePath: String):JsValue = {

    //dependency co referencing is memory intensive
    removePipelineStep("dcoref")

    val moreThanFiveWordsAndNewLines = (l:Traversable[String]) => l.filter(e => e.split(" ").length > 5 || e == "\n")

    val processFns = StringProcessFns(filter = moreThanFiveWordsAndNewLines)
    fileToJson(filePath, processFns)
  }

}
