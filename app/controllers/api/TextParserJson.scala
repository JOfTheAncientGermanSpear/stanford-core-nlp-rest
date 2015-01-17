package controllers.api

import edu.stanford.nlp.dcoref.CorefChain
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.util.{IntPair, CoreMap}

import collection.JavaConverters._

import edu.stanford.nlp.ling.{IndexedWord, CoreLabel}
import edu.stanford.nlp.semgraph.{SemanticGraphEdge, SemanticGraph}
import edu.stanford.nlp.trees.{GrammaticalRelation, Tree}
import play.api.libs.json._

trait TextParserJson extends ITextParser {

  implicit val treeWrites = new Writes[Tree]{
    def writes(t: Tree):JsValue =
      if (t.isLeaf)
        JsString(t.value)
      else
        Json.obj(t.value -> t.children.map(writes))
  }

  implicit val coreLabelWrites = new Writes[CoreLabel]{
    def writes(c: CoreLabel) = Json.obj(
      "word" -> word(c),
      "lemma" -> lemma(c),
      "character_offset_begin" -> characterOffsetBegin(c),
      "character_offset_end" -> characterOffsetEnd(c),
      "pos" -> pos(c),
      "ner" -> ner(c),
      "speaker" -> speaker(c)
    )
  }

  implicit val indexedCoreLabelWrites = new Writes[(CoreLabel, Int)]{
    def writes(c: (CoreLabel, Int)) =
      Json.toJson(c._1) match {
        case o:JsObject => o ++ Json.obj("index" -> c._2)
      }
  }

  implicit val indexedWordWrites = new Writes[IndexedWord] {
    def writes(w: IndexedWord) =
      Json.obj(
        "begin_position" -> w.beginPosition,
        "end_position" -> w.endPosition,
        "index" -> w.index,
        "value" -> w.value
      )
  }

  implicit val grammaticalRelationWrites = new Writes[GrammaticalRelation] {
    def writes(r: GrammaticalRelation) =
      Json.obj(
        "value" -> r.getShortName,
        "description" -> r.getLongName
      )
  }

  implicit val semanticGraphEdgeWrites = new Writes[SemanticGraphEdge] {
    def writes(edge: SemanticGraphEdge) = {
      Json.obj(
        "dependent" -> Json.toJson(edge.getDependent),
        "governor" -> Json.toJson(edge.getGovernor),
        "relation" -> Json.toJson(edge.getRelation),
        "source" -> Json.toJson(edge.getSource),
        "target" -> Json.toJson(edge.getTarget),
        "is_extra" -> edge.isExtra
      )
    }
  }

  implicit val semanticGraphWrites = new Writes[SemanticGraph]{


    def writes(g: SemanticGraph) = {
      val iterator: Iterator[SemanticGraphEdge] = g.edgeIterable.iterator.asScala
      iterator.foldLeft(Json.arr()){
        (acc, e) => acc.append(Json.toJson(e))
      }
    }
  }

  implicit val coreMapWrites = new Writes[(CoreMap, Int)]{
    def writes(c: (CoreMap, Int)) =
      Json.obj(
        "index" -> c._2,
        "tree" -> Json.toJson(tree(c._1)),
        "dependencies" -> Json.toJson(dependencies(c._1)),
        "tokens" -> Json.toJson(tokens(c._1))
      )
  }

  type MentionMap = Map[IntPair, Set[CorefChain.CorefMention]]

  implicit val corefMentionWrites = new Writes[CorefChain.CorefMention]{
    def writes(cm: CorefChain.CorefMention) = Json.obj(
      "representativeness" -> cm.mentionType.representativeness,
      "sentence" -> cm.sentNum,
      "start" -> cm.startIndex,
      "end" -> cm.endIndex,
      "head" -> cm.headIndex,
      "text" -> cm.mentionSpan,
      "animacy" -> cm.animacy.name(),
      "gender" -> cm.gender.name(),
      "number" -> cm.number.name(),
      "mention_id" -> cm.mentionID,
      "mention_type" -> cm.mentionType.name(),
      "position" -> Json.obj(
        "sent_num" -> cm.position.get(0),
        "mention_num" -> cm.position.get(1)
      ),
      "coref_cluster_id" -> cm.corefClusterID
    )
  }

  implicit val mentionMapWrites = new Writes[MentionMap]{

    def writes(m: MentionMap) =
      m.foldLeft(Json.arr()){
        (acc, i) => acc.append(
          Json.obj("sentence" -> i._1.getSource,
            "head" -> i._1.getTarget,
            "mentions" -> Json.toJson(i._2.toList)))
      }
  }

  implicit val corefChainWrites = new Writes[CorefChain]{

    def isRepresentative(c: CorefChain,
                         mention: CorefChain.CorefMention):Boolean = {
      val rep = c.getRepresentativeMention
      mention.headIndex == rep.headIndex && mention.sentNum == mention.sentNum
    }

    def writes(c: CorefChain) =
      Json.obj(
        "id" -> c.getChainID,
        "mentions" -> c.getMentionsInTextualOrder.asScala.map{
          m => Json.toJson(m) match {
            case o:JsObject => o ++
              Json.obj("is_representative" -> isRepresentative(c, m))
          }
        }
      )
  }

  def convert_document(document: Annotation) = {
    Json.obj(
      "sentences" -> sentences(document).map(Json.toJson(_)),
      "coreferences" -> coreferences(document).map(Json.toJson(_))
    )
  }

}