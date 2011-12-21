package org.acharneski.text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class TextAnalyzer<T extends Chain>
{

  public final int depth;

  public TextAnalyzer(int depth)
  {
    super();
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
    LinkedList<String> tokens = new LinkedList<String>();
    for (int i = 0; i < depth; i++)
    {
      tokens.add("");
    }
    for (String token : tokenStream)
    {
      tokens.remove();
      tokens.add(token);
      getChain().learnSegment(tokens, 1);
    }
    tokens.remove();
    tokens.add("");
    getChain().learnSegment(tokens, 1);
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

  public double getWeight(String string)
  {
    ArrayList<String> list = new ArrayList<String>();
    for (String token : tokenize(string))
      list.add(token);
    return getChain().getWeight(list);
  }

  public abstract T getChain();

}