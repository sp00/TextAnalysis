package org.acharneski.text;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import junit.framework.Assert;

import org.acharneski.text.EntropyMarkovChain.EntropySegment;
import org.acharneski.text.EntropyMarkovChain.TokenType;
import org.junit.Test;

public class MarkovChainTest
{
  @Test
  public void testSimple()
  {
    MarkovChain<?> chain = new GenericMarkovChain(2);
    chain.learn("ababab");
    chain.print(System.out);
    Assert.assertEquals(7., chain.getWeight());
    Assert.assertEquals(3., chain.getWeight("ab"));
    String generate = chain.generateString();
    System.out.println(generate);
  }

  @Test
  public void testBook()
  {
    MarkovChain<?> chain = new GenericMarkovChain(8);
    chain.learn(this.getClass().getClassLoader().getResourceAsStream("crime_and_punishment.txt"));
    int maxTokens = 10000;
    for (String t : chain.generateStream())
    {
      if(0 > maxTokens--) break;
      System.out.print(t);
    }
  }

  public static class Statistics
  {
    int count = 0;
    double total = 0;
    public void inc(double v)
    {
      count++;
      total += v;
    }
    public double avg()
    {
      return total/count;
    }
    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("[avg=");
      builder.append(avg());
      builder.append(", count=");
      builder.append(count);
      builder.append("]");
      return builder.toString();
    }
    
  }
  
  @Test
  public void testMeasureWords()
  {
    EntropyMarkovChain chain = new EntropyMarkovChain(2);
    chain.learn(this.getClass().getClassLoader().getResourceAsStream("crime_and_punishment.txt"));
    Map<String, Statistics> stats = new HashMap<String, MarkovChainTest.Statistics>();
    for (EntropySegment t : chain.weigh(this.getClass().getClassLoader().getResourceAsStream("crime_and_punishment.txt")))
    {
      if(TokenType.Word == t.type)
      {
        String key = t.text.toLowerCase();
        Statistics statistics = stats.get(key);
        if(null == statistics)
        {
          statistics = new Statistics();
          stats.put(key, statistics);
        }
        statistics.inc(t.averageWeight());
      }
    }
    TreeSet<Entry<String, Statistics>> sorted = new TreeSet<Map.Entry<String,Statistics>>(new Comparator<Entry<String, Statistics>>()
    {
      public int compare(Entry<String, Statistics> o1, Entry<String, Statistics> o2)
      {
        return Double.compare(o1.getValue().avg(), o2.getValue().avg());
      }
    });
    for(Entry<String, Statistics> e : stats.entrySet())
    {
      sorted.add(e);
    }
    int maxTokens = 10000;
    for(Entry<String, Statistics> e : sorted)
    {
      if(0 > maxTokens--) break;
      System.out.println(String.format("%s = %s", e.getKey(), e.getValue()));
    }
  }
}
