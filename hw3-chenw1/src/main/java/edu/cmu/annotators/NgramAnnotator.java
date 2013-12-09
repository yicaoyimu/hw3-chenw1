package edu.cmu.annotators;

import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;
import org.cleartk.ne.type.NamedEntity;

import edu.cmu.deiis.types.*;

/**
 * 
 * @author chenw1
 *
 * NgramAnnotator
 * 
 * - this annotator looks for tokens in the text input and
 *   annotates unigrams, bigrams and trigrams based on the 
 *   tokens
 *   
 * - bigrams and trigrams are annotated if two or three
 *   consecutive tokens are in the same line respectively
 * 
 */
public class NgramAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    JFSIndexRepository repo = aJCas.getJFSIndexRepository();
    FSIterator<TOP> iter = repo.getAllIndexedFS(NamedEntity.type);
    
    while(iter.hasNext())
    {
      NamedEntity cur = (NamedEntity) iter.next();
      for(int i = 0; i < cur.getMentions().size(); i++)
      {
        NGram toadd = new NGram(aJCas, cur.getMentions(i).getBegin(), cur.getMentions(i).getEnd());
        toadd.addToIndexes();
      }
    }


    String docText = aJCas.getSofaDataString();
    FSIndex tokenIndex = aJCas.getAnnotationIndex(Token.type);
    
    Iterator tokenIter = tokenIndex.iterator();
        
    Token[] prev = new Token[2];
    prev[0] = null;
    prev[1] = null;
    
    while(tokenIter.hasNext())
    {
      Token cur = (Token) tokenIter.next();
      
      // 1-Gram
      NGram uniGram = new NGram(aJCas, cur.getBegin(), cur.getEnd());
      uniGram.addToIndexes();
      
      // 2-Gram
      if(prev[1] != null)
      {
        if(!(docText.substring(prev[1].getEnd(), cur.getBegin()).contains("\n")))
        {
          NGram biGram = new NGram(aJCas, prev[1].getBegin(), cur.getEnd());
          biGram.addToIndexes();
        }
      }
      
      // 3-Gram
      if(prev[0] != null && prev[1] != null)
      {
        if( !(docText.substring(prev[0].getEnd(), prev[1].getBegin()).contains("\n")) &&
            !(docText.substring(prev[1].getEnd(), cur.getBegin()).contains("\n")) ) 
        {
          NGram triGram = new NGram(aJCas, prev[0].getBegin(), cur.getEnd());
          triGram.addToIndexes();
        }
      }
      
      prev[0] = prev[1];
      prev[1] = cur;
    }
  }

}
