package edu.cmu.annotators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.*;

/**
 * 
 * @author chenw1
 *
 * - this annotator serves as the final "merging" component
 *   that takes all the previous annotations and assign
 *   scores to each answer based on all existing anntation
 *   
 * - this annotator adopts N-Gram scoring method in which the
 *   score of an answer is calculated based on the the ratio
 *   between question N-Grams that are found in answer N-Grams
 *   and the total number of answer N-Grams (for 1-, 2- and 3-grams)
 * 
 * - this annotator also prints out the final result to console,
 *   including the gold answer, the calculated result, original text
 *   input, and precision at N.
 *   
 */
public class AnswerScoreAnnotator extends JCasAnnotator_ImplBase {

  /**
   * A helper function that calculate the answer score
   * @param question the set of N-Grams for question
   * @param answer the set of N-Grams for answer
   * @param docText the whole text input
   * @return the score of the input answer, using N-Gram scoring method
   */
  private static double evaluate(ArrayList<NGram> question, ArrayList<NGram> answer, String docText) {
    int overlap = 0;
    for (int i = 0; i < question.size(); i++)
      for (int j = 0; j < answer.size(); j++) {
        NGram q = question.get(i);
        NGram a = answer.get(j);
        String qText = docText.toLowerCase().substring(q.getBegin(), q.getEnd());
        String aText = docText.toLowerCase().substring(a.getBegin(), a.getEnd());
        if (qText.compareTo(aText) == 0)
          overlap++;
      }
    
    return (double) overlap / (double) answer.size();
  }

  /**
   * The comparator class that is used to sort AnswerScore object
   * Sort the AnswerScore object in decreasing order in terms of their answer score
   * @author chenw1
   *
   */
  private static class AnswerComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
      AnswerScore a1 = (AnswerScore) o1;
      AnswerScore a2 = (AnswerScore) o2;
      return a2.getScore() - a1.getScore() < 0 ? -1 : 1;
    }

  }

  /**
   * Print the final result
   * @param q the Question
   * @param score ArrayList of AnswerScore objects that will be printed as part of the final result
   * @param docText the whole input text file
   * @param N the number of correct answer for the given question
   */
  private static void printResult(Question q, ArrayList<AnswerScore> score, String docText, int N) {
    Collections.sort(score, new AnswerComparator());
    System.out.print("Q ");
    System.out.println(docText.substring(q.getBegin(), q.getEnd()));
    int correctCount = 0;
    for (int i = 0; i < score.size(); i++) {
      AnswerScore as = score.get(i);
      String lable = "";
      lable += as.getAnswer().getIsCorrect() ? "1 " : "0 ";
      lable += i < N ? "1 " : "0 ";
      if(i < N && as.getAnswer().getIsCorrect()) correctCount++;
      System.out.println("A " + lable + docText.substring(as.getBegin(), as.getEnd()));
    }
    
    System.out.println("Precision at " + N + ": " + (double)correctCount / (double)N);
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText;
    Question q = null;
    int N = 0;
    ArrayList<AnswerScore> score = new ArrayList<AnswerScore>();

    docText = aJCas.getDocumentText();
    FSIndex answerIndex = aJCas.getAnnotationIndex(Answer.type);
    FSIndex questionIndex = aJCas.getAnnotationIndex(Question.type);
    Iterator questionIter = questionIndex.iterator();
    Iterator answerIter = answerIndex.iterator();

    // get question N-Grams
    ArrayList<NGram> questionGram = new ArrayList<NGram>();
    while (questionIter.hasNext()) {
      q = (Question) questionIter.next();
      FSIndex ngramIndex = aJCas.getAnnotationIndex(NGram.type);
      Iterator ngramIter = ngramIndex.iterator();
      while (ngramIter.hasNext()) {
        NGram cur = (NGram) ngramIter.next();
        if (cur.getBegin() >= q.getBegin() && cur.getEnd() <= q.getEnd())
          questionGram.add(cur);
      }
    }

    // assign score to each answer
    while (answerIter.hasNext()) {
      Answer answer = (Answer) answerIter.next();
      N += answer.getIsCorrect() ? 1 : 0;

      // get answer N-Grams
      ArrayList<NGram> answerGram = new ArrayList<NGram>();
      FSIndex ngramIndex = aJCas.getAnnotationIndex(NGram.type);
      Iterator ngramIter = ngramIndex.iterator();
      while (ngramIter.hasNext()) {
        NGram curNGram = (NGram) ngramIter.next();
        if (curNGram.getBegin() >= answer.getBegin() && curNGram.getEnd() <= answer.getEnd())
          answerGram.add(curNGram);
      }

      AnswerScore annotation = new AnswerScore(aJCas, answer.getBegin(), answer.getEnd());
      annotation.setAnswer(answer);
      annotation.setScore(evaluate(questionGram, answerGram, docText));
      annotation.addToIndexes();
      score.add(annotation);
    }

    // printResult(q, score, docText, N);
    
    
  }

}
