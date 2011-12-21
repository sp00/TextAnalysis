package org.acharneski.text;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class MarkovChain<T extends MarkovChain<T>> extends TextAnalyzer<T> implements Chain
{
  public static List<String> tail(List<String> list)
  {
    return list.subList(1, list.size());
  }

  private double weight = 0.;
  protected final SortedMap<String, T> data = new TreeMap<String, T>();

  public MarkovChain(int depth)
  {
    super(depth);
  }

  public void learnSegment(List<String> tokens, double weight)
  {
    assert(tokens.size() == depth);
    this.weight += weight;
    if (0 < tokens.size())
    {
      String firstToken = tokens.get(0);
      T subChain = getChild(firstToken);
      List<String> subList = tail(tokens);
      subChain.learnSegment(subList, weight);
    }
  }

  protected T getChild(String firstToken)
  {
    T subChain = data.get(firstToken);
    if (null == subChain)
    {
      subChain = newChild();
      data.put(firstToken, subChain);
    }
    return subChain;
  }

  protected abstract T newChild();

  public double getWeight(List<String> tokens)
  {
    if (0 == tokens.size())
    {
      return getWeight();
    }
    String firstToken = tokens.get(0);
    T subChain = getChild(firstToken);
    return subChain.getWeight(tokens.subList(1, tokens.size()));
  }

  public double getWeight()
  {
    return weight;
  }

  public String generateString()
  {
    StringBuffer sb = new StringBuffer();
    for (String t : generateStream())
    {
      sb.append(t);
    }
    return sb.toString();
  }

  public Iterable<String> generateStream()
  {
    return new Iterable<String>()
    {
      
      public Iterator<String> iterator()
      {
        return new Iterator<String>()
        {
          String nextToken = null;
          LinkedList<String> list = new LinkedList<String>();
          
          public void remove()
          {
            throw new RuntimeException();
          }
          
          public String next()
          {
            nextToken = generateNext(list);
            while(list.size() < depth) list.add("");
            list.add(nextToken);
            if(list.size() > depth) list.remove();
            return nextToken;
          }
          
          public boolean hasNext()
          {
            return !"".equals(nextToken);
          }
        };
      }
    };
  }

  public String generateNext(List<String> list)
  {
    LinkedList<String> subList = new LinkedList<String>(list);
    while(0 < subList.size())
    {
      String generateNext = tryGenerateNext(subList);
      if (null != generateNext)
      {
        return generateNext;
      }
      else
      {
        subList.remove();
      }
    }
    String randomNext = randomNext();
    if(null == randomNext) 
    {
      throw new RuntimeException();
    }
    return randomNext;
  }

  protected String tryGenerateNext(List<String> tokens)
  {
    if (0 == tokens.size())
    {
      return randomNext();
    }
    else
    {
      String firstToken = tokens.get(0);
      T subChain = getChild(firstToken);
      List<String> subList = tail(tokens);
      return subChain.tryGenerateNext(subList);
    }
  }

  protected String randomNext()
  {
    double fate = new Random().nextDouble() * ((double) getWeight());
    for (Entry<String, T> e : data.entrySet())
    {
      double w = e.getValue().getWeight();
      if (fate < w)
      {
        return e.getKey();
      }
      else
      {
        fate -= w;
      }
    }
    return null;
  }

  protected void print(List<String> prefix, PrintStream out)
  {
    out.println(String.format("%s = %s", 0==prefix.size()?null:Arrays.toString(prefix.toArray()), weight));
    for (Entry<String, T> e : data.entrySet())
    {
      ArrayList<String> prefix2 = new ArrayList<String>(prefix);
      prefix2.add(e.getKey());
      e.getValue().print(prefix2, out);
    }
  }

  public void print(PrintStream out)
  {
    print(new ArrayList<String>(), out);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getChain()
  {
    return (T) this;
  }

}
