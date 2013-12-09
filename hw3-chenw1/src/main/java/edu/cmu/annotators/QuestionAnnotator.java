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
 * QuestionAnnotator
 * - the annotator looks for questions in the input file using regular expression
 * 
 */

public class QuestionAnnotator extends JCasAnnotator_ImplBase {

  private Pattern question = Pattern.compile("^Q\\s([^\n]*)\n");
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();
    Matcher matcher = question.matcher(docText);
    int pos = 0;
    while(matcher.find(pos))
    {
      Question annotation = new Question(aJCas);
      annotation.setBegin(matcher.start(1));
      annotation.setEnd(matcher.end(1));
      annotation.addToIndexes();
      pos = matcher.end();
    }
    
  }

}
