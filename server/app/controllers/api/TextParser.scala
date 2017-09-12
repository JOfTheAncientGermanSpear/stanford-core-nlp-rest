package controllers.api

import edu.stanford.nlp.pipeline.Annotation
import play.api.libs.json._
import play.api.mvc._


object TextParser extends Controller with utils.TextParserJson {

  def parse_to_json(text:String) = {
    val doc:Annotation = parse_text(text)
    convert_document(doc)
  }

  def parse_text_action(text: String) = Action {
    Ok(parse_to_json(text))
  }

  def parse_body_action = Action { request: Request[AnyContent] =>
    val body: AnyContent = request.body
    val data = body.asFormUrlEncoded.get

    data.get("text") match {
      case Some(text_seq) =>  
        if (text_seq.length == 1) 
          Ok(parse_to_json(text_seq.head)) else 
          BadRequest("text field must be length 1")
      case None => BadRequest(
        "Expecting request body with text field")
    }
  }

}
