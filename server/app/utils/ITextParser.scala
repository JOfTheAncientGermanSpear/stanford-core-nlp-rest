package utils

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

  private val pipelineProps = new java.util.Properties()
  val stepsKey = "annotators"
  pipelineProps.put(stepsKey, "tokenize, ssplit, pos, lemma, ner, parse, dcoref")

  var pipelineInstantiated = false
  val pipelineInstantiatedErrorMsg = "Pipeline already instantiated, can not be altered."

  def pipelineSteps:Seq[String] = pipelineProps.getProperty(stepsKey).split(",").map(_.trim)

  lazy val cachedPipelineSteps = pipelineSteps
  def inPipeline(step: String):Boolean =
    if (pipelineInstantiated) cachedPipelineSteps.contains(step)
    else pipelineSteps.contains(step)

  def removePipelineStep(step:String) =
    if (pipelineInstantiated)
      sys.error(pipelineInstantiatedErrorMsg)
    else
      pipelineProps.setProperty(stepsKey,
        pipelineSteps.filterNot(_ == step).mkString(",")
      )


  def addPipelineStep(step: String) =
    if (pipelineInstantiated)
      sys.error(pipelineInstantiatedErrorMsg)
    else
      pipelineProps.setProperty(stepsKey,
        (step + pipelineSteps.toSet).mkString(",")
      )


  lazy val pipeline = {
    pipelineInstantiated = true
    new StanfordCoreNLP(pipelineProps)
  }

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

  def coreferences(document: Annotation): Option[Iterable[CorefChain]] =
    if (inPipeline("dcoref")) Some(document.get(classOf[CorefChainAnnotation]).asScala.values) else None

  def parse_text(text: String): Annotation = {
    val document: Annotation = new Annotation(text)
    pipeline.annotate(document)

    document
  }
  
}
