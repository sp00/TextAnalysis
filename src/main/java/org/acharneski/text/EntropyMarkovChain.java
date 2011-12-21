package org.acharneski.text;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class EntropyMarkovChain extends TextAnalyzer<MarkovChain<?>>
{
  GenericMarkovChain chain;
  
  public EntropyMarkovChain(int depth)
  {
    super(depth);
    chain = new GenericMarkovChain(depth);
  }
  
  public enum TokenType
  {
    Whitespace,
    Word
  }

  public static class EntropySegment
  {
    public final String text;
    public final double entropy;
    public final TokenType type;
    public EntropySegment(String text, double entropy, TokenType isDelimiter)
    {
      super();
      this.text = text;
      this.type = isDelimiter;
      this.entropy = entropy;
    }
    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("EntropySegment [\"");
      builder.append(displayText(text));
      builder.append("\", type=");
      builder.append(type);
      builder.append(", entropy=");
      builder.append(entropy);
      builder.append(", avg=");
      builder.append(averageWeight());
      builder.append("]");
      return builder.toString();
    }
    private String displayText(String s)
    {
      s = s.replaceAll("\n", "~n");
      s = s.replaceAll("\t", "~t");
      s = s.replaceAll("\r", "~r");
      return s;
    }
    public double averageWeight()
    {
      return entropy / text.length(); 
    }
  }
  
  public Iterable<EntropySegment> weigh(final InputStream stream)
  {
    return new Iterable<EntropyMarkovChain.EntropySegment>()
    {
      public Iterator<EntropySegment> iterator()
      {
        final LinkedList<String> tokens = new LinkedList<String>();
        for (int i = 0; i < depth; i++)
        {
          tokens.add("");
        }
        return new Iterator<EntropyMarkovChain.EntropySegment>()
        {
          Iterator<String> iterator = tokenize(stream).iterator();
          double entropy = 0;
          StringBuffer sb = new StringBuffer();
          TokenType currentType = TokenType.Whitespace;
          
          public void remove()
          {
            throw new RuntimeException();
          }
          
          public EntropySegment next()
          {
            EntropySegment entropySegment = null;
            while(iterator.hasNext())
            {
              String token = iterator.next();
              tokens.remove();
              tokens.add(token);
              TokenType nextType = getType(token);
              if(currentType != nextType)
              {
                entropySegment = new EntropySegment(sb.toString(), entropy, currentType);
                sb.delete(0, sb.length());
                entropy = 0;
                currentType = nextType;
              }
              entropy += weigh(tokens);
              sb.append(token);
              if(null != entropySegment) return entropySegment;
            }
            iterator = null;
            tokens.remove();
            tokens.add("");
            entropy += weigh(tokens);
            return new EntropySegment(sb.toString(), entropy, currentType);
          }
          
          public boolean hasNext()
          {
            return null != iterator;
          }
        };
      }
    };
  }
  
  static final Set<String> delimiters = new HashSet<String>();
  static {
    delimiters.add(" ");
    delimiters.add("\t");
    delimiters.add("\n");
    delimiters.add("\r");
    delimiters.add("-");
    delimiters.add("!");
    delimiters.add(".");
    delimiters.add("?");
    delimiters.add(",");
    delimiters.add(";");
    delimiters.add("_");
    delimiters.add("\"");
    //delimiters.add("'");
  }
  
  protected TokenType getType(String token)
  {
    return delimiters.contains(token)?TokenType.Whitespace:TokenType.Word;
  }
  
  static final double LN2 = Math.log(2);
  protected double weigh(List<String> tokens)
  {
    assert(tokens.size() == depth);
    final List<String> truncated = tokens.subList(0, tokens.size()-1);
    final double lw = getChain().getWeight(tokens);
    final double pw = getChain().getWeight(truncated);
    final double entropy = -Math.log(lw / pw) / LN2;
    assert(0 <= entropy);
    assert(!Double.isNaN(entropy));
    return entropy;
  }

  @Override
  public GenericMarkovChain getChain()
  {
    return chain;
  }

}
