package controllers.api

import edu.stanford.nlp.dcoref.CorefChain
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation
import edu.stanford.nlp.util.CoreMap

import collection.JavaConverters._

trait ITextParser {

  val props = new java.util.Properties()
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref")

  val pipeline = new StanfordCoreNLP(props)

  def tree(sentence: CoreMap): Tree =
    sentence.get(classOf[TreeAnnotation])

  def dependencies(sentence: CoreMap): SemanticGraph =
    sentence.get(classOf[CollapsedCCProcessedDependenciesAnnotation])

  def corefChain(document: Annotation): Map[Integer, CorefChain] =
    document.get(classOf[CorefChainAnnotation]).asScala.toMap

  def tokens(sentence: CoreMap): List[(CoreLabel, Int)] =
    sentence.get(classOf[TokensAnnotation]).asScala.toList.zipWithIndex

  def word(token: CoreLabel): String = token.get(classOf[TextAnnotation])

  def lemma(token: CoreLabel): String = token.get(classOf[LemmaAnnotation])

  def characterOffsetBegin(token: CoreLabel): Int = token.get(classOf[CharacterOffsetBeginAnnotation])

  def characterOffsetEnd(token: CoreLabel): Int = token.get(classOf[CharacterOffsetEndAnnotation])

  def pos(token: CoreLabel): String = token.get(classOf[PartOfSpeechAnnotation])

  def ner(token: CoreLabel): String = token.get(classOf[NamedEntityTagAnnotation])

  def speaker(token: CoreLabel): String = token.get(classOf[SpeakerAnnotation])

  def sentences(document: Annotation): List[(CoreMap, Int)] =
    document.get(classOf[SentencesAnnotation]).asScala.toList.zipWithIndex

  def coreferences(document: Annotation): Iterable[CorefChain] =
    document.get(classOf[CorefChainAnnotation]).asScala.values

  def parse_text(text: String): Annotation = {
    val document: Annotation = new Annotation(text)
    pipeline.annotate(document)

    document
  }
  
}
