Stanford Core NLP REST Wrapper
=================================


###Stanford Core NLP Functionality
[Stanford Core NLP][1] provides tasks crucial for Natural Language Processing, such as:

1.  Tokenization
2.  Parse Tree generation from sentences
3.  Finding Coreferences
4.  Detecting Relations/Dependencies between words in sentences

###It's in Java & I Wish to Use Python
However, Stanford Core NLP is written in Java. And I wish to use the REPL that [IPython][2] provides.

###Rest Service
Therefore, this application wraps Stanford Core NLP in a simple Scala-based [Play Framework][3] RESTful web service.

##Installation
1. Verify [Java 8 JDK][4] is installed
1. Clone this repository
1. CD to root level of cloned code
1. Execute `./activator run`
1. Type http://localhost:9000/api/[Some sentence] in your web browser's location bar.
1. JSON result will be returned

[1]: nlp.stanford.edu/software/corenlp.shtml
[2]: www.ipython.org
[3]: https://www.playframework.com/
[4]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
