package org.acharneski.text;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

public class MarkovChainTest
{
  @Test
  public void testSimple()
  {
    MarkovChain chain = new MarkovChain(2);
    chain.learn("ababab");
    Assert.assertEquals(7, chain.weight());
    Assert.assertEquals(3, chain.weight("ab"));
    String generate = chain.generateString();
    System.out.println(generate);
  }

  @Test
  public void testBook()
  {
    MarkovChain chain = new MarkovChain(2);
    InputStream stream = this.getClass().getClassLoader().getResourceAsStream("crime_and_punishment.txt");
    chain.learn(stream);
    for (String t : chain.generateStream())
    {
      System.out.print(t);
    }
  }
}
