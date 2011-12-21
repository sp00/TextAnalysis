package org.acharneski.text;

public class GenericMarkovChain extends MarkovChain<GenericMarkovChain>
{

  public GenericMarkovChain(int depth)
  {
    super(depth);
  }

  @Override
  protected GenericMarkovChain newChild()
  {
    return new GenericMarkovChain(depth-1);
  }

}
