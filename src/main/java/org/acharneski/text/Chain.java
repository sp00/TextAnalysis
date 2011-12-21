package org.acharneski.text;

import java.util.List;

public interface Chain
{

  double getWeight(List<String> list);

  void learnSegment(List<String> tokens, double w);

}
