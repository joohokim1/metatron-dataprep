package app.metatron.dataprep;

import app.metatron.discovery.prep.parser.preparation.RuleVisitorParser;
import app.metatron.discovery.prep.parser.preparation.rule.Rule;

import org.junit.Test;

public class ParserTest {
  @Test
  public void test1() {
    String ruleCode = "drop col: test1, test5";
    Rule rule = new RuleVisitorParser().parse(ruleCode);
    System.out.println(rule);
  }
}
