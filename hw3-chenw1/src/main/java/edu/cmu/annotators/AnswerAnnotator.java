package edu.cmu.annotators;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import edu.cmu.deiis.types.*;
import java.util.regex.*;

/**
 * 
 * @author chenw1
 * 
 * AnswerAnnotator
 * - the annotator looks for answers in the input file using regular expression
 * - the annotator also sets the isCorrect flag in answer annotation type
 * 
 */
public class AnswerAnnotator extends JCasAnnotator_ImplBase {
  private Pattern answer= Pattern.compile("[\n]*A\\s([01])\\s([^\n]*)[\n]*");
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();
    Matcher matcher = answer.matcher(docText);
    int pos = 0;
    while(matcher.find(pos))
    {
      Answer annotation = new Answer(aJCas);
      annotation.setBegin(matcher.start(2));
      annotation.setEnd(matcher.end(2));
      annotation.setIsCorrect(matcher.group(1).compareTo("1") == 0);
      annotation.addToIndexes();
      pos = matcher.end();
    }
  }

}
