package edu.cmu.annotators;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import edu.cmu.deiis.types.*;

/**
 * AnnotationAnnotator
 * - annotator to annotate Annotation type defined in give type system
 * - take the whole input document as one annotation
 * 
 * @author chenw1
 *
 */
public class AnnotationAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();
    
    Annotation annotation = new Annotation(aJCas);
    annotation.setBegin(0);
    annotation.setEnd(docText.length() - 1);
    annotation.addToIndexes();
  }

}
