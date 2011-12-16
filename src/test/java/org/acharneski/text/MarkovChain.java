package org.acharneski.text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class MarkovChain
{
  public static List<String> tail(List<String> list)
  {
    return list.subList(1, list.size());
  }

  private final int depth;
  private int weight;
  private final SortedMap<String, MarkovChain> data = new TreeMap<String, MarkovChain>();

  public MarkovChain(int depth)
  {
    this.depth = depth;
  }

  public void learn(InputStream stream)
  {
    learn(tokenize(stream));
  }

  public void learn(String string)
  {
    learn(tokenize(string));
  }

  protected void learn(Iterable<String> tokenStream)
  {
    // TODO: Process this with a limited-length list
    LinkedList<String> tokens = new LinkedList<String>();
    for (int i = 0; i < depth; i++)
    {
      tokens.add("");
    }
    for (String token : tokenStream)
    {
      tokens.remove();
      tokens.add(token);
      learnSegment(tokens);
    }
    tokens.remove();
    tokens.add("");
    learnSegment(tokens);
  }

  protected void learnSegment(List<String> tokens)
  {
    assert(tokens.size() == depth);
    weight += 1;
    if (0 < tokens.size())
    {
      String firstToken = tokens.get(0);
      MarkovChain subChain = data.get(firstToken);
      if (null == subChain)
      {
        subChain = new MarkovChain(depth - 1);
        data.put(firstToken, subChain);
      }
      List<String> subList = tail(tokens);
      subChain.learnSegment(subList);
    }
  }

  protected Iterable<String> tokenize(final InputStream stream)
  {
    final Iterator<String> iterator = new Iterator<String>()
    {

      public void remove()
      {
        throw new RuntimeException();
      }

      public String next()
      {
        try
        {
          return new String(new char[] { (char) stream.read() });
        } catch (IOException e)
        {
          throw new RuntimeException();
        }
      }

      public boolean hasNext()
      {
        try
        {
          return 0 < stream.available();
        } catch (IOException e)
        {
          e.printStackTrace();
          return false;
        }
      }
    };

    return new Iterable<String>()
    {
      public Iterator<String> iterator()
      {
        return iterator;
      }
    };
  }

  protected Iterable<String> tokenize(String string)
  {
    ArrayList<String> list = new ArrayList<String>();
    for (char c : string.toCharArray())
    {
      list.add(new String(new char[] { c }));
    }
    return list;
  }

  public int weight(String string)
  {
    ArrayList<String> list = new ArrayList<String>();
    for (String token : tokenize(string))
      list.add(token);
    return weight(list);
  }

  protected int weight(List<String> tokens)
  {
    if (0 == tokens.size())
      return weight;
    return data.get(tokens.get(0)).weight(tokens.subList(1, tokens.size()));
  }

  public int weight()
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
    for (int d = depth; d > 0; d--)
    {
      List<String> subList = fixSize(list, d);
      String generateNext = tryGenerateNext(subList);
      if (null != generateNext)
      {
        return generateNext;
      }
    }
    String randomNext = randomNext();
    if(null == randomNext) 
    {
      throw new RuntimeException();
    }
    return randomNext;
  }

  public static List<String> fixSize(List<String> list, int listSize)
  {
    ArrayList<String> newList = new ArrayList<String>();
    int nullElements = listSize - list.size();
    if (0 > nullElements)
      nullElements = 0;
    for (int i = 0; i < nullElements; i++)
    {
      newList.add("");
    }
    if (0 == nullElements)
    {
      newList.addAll(list.subList(list.size() - listSize, list.size()));
    }
    else
    {
      newList.addAll(list);
    }
    assert (listSize == newList.size());
    return newList;
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
      MarkovChain subChain = data.get(firstToken);
      if (null == subChain)
      {
        return null;
      }
      else
      {
        List<String> subList = tail(tokens);
        return subChain.tryGenerateNext(subList);
      }
    }
  }

  protected String randomNext()
  {
    double fate = new Random().nextDouble() * ((double) weight);
    for (Entry<String, MarkovChain> e : data.entrySet())
    {
      double w = e.getValue().weight();
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

}
