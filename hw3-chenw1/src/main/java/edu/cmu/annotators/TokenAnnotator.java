package edu.cmu.annotators;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.*;

import java.util.regex.*;

/**
 * 
 * @author chenw1
 * - this annotator looks for tokens in the given text input using
 *   regular expression
 * - tokens are parts of the text with only word character, seperated
 *   by non-word characters.
 *
 */
public class TokenAnnotator extends JCasAnnotator_ImplBase {
  private Pattern token = Pattern.compile("[\\W]*\\b(\\w\\w+)\\b[\\W]*");
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();
    Matcher matcher = token.matcher(docText);
    int pos = 0;
    while(matcher.find(pos))
    {
      Token annotation = new Token(aJCas);
      annotation.setBegin(matcher.start(1));
      annotation.setEnd(matcher.end(1));
      annotation.addToIndexes();
      pos = matcher.end();
    }

  }

}
