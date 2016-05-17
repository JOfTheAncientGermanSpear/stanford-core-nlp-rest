package controllers.api

import edu.stanford.nlp.pipeline.Annotation
import play.api.mvc._


object TextParser extends Controller with utils.TextParserJson {

  def parse_text_action(text: String) = Action {
    val doc:Annotation = parse_text(text)
    val json = convert_document(doc)
    Ok(json)
  }

}
