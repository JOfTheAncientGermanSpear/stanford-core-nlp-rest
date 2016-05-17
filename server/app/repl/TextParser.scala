package repl

import edu.stanford.nlp.pipeline.Annotation
import play.api.mvc._

//just change routes from controllers.api to repl
//to quickly view JSON output in browser
object TextParser extends Controller with utils.TextParserJson {
  def parse_text_action(text: String) = Action {
    val filePath = text
    val doc:Annotation =
      Serializer.deserialize(filePath) match {
        case a:Annotation => a
      }
    val json = convert_document(doc)
    Ok(json)
  }
}
