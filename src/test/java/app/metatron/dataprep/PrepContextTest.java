/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.metatron.dataprep;

import static app.metatron.dataprep.SourceDesc.Type.UPLOADED;
import static app.metatron.dataprep.TestCommon.getResourcePath;
import static org.junit.Assert.assertEquals;

import app.metatron.dataprep.teddy.DataFrame;
import app.metatron.dataprep.teddy.exceptions.TeddyException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PrepContextTest {

  PrepContext pc;

  @Before
  public void setUp() {
    pc = PrepContext.DEFAULT.withCacheMB(1000);
  }

  private String loadSalesNamed() {
    SourceDesc src = new SourceDesc(UPLOADED);
    src.setStrUri(getResourcePath("sales_named.csv"));
    return pc.load(src, "sales_named");
  }

  @Test
  public void testLoad() {
    String dsId = loadSalesNamed();
    DataFrame df = pc.getCurDf(dsId);
    df.show();
  }

  @Test
  public void testAutoTypingPreview() throws TeddyException {
    String dsId = loadSalesNamed();
    DataFrame df = pc.applyAutoTyping(pc.getCurDf(dsId));
    df.show();
  }

  private String testCreateInternal() throws TeddyException {
    String dsId = loadSalesNamed();
    DataFrame df = pc.getCurDf(dsId);
    List<String> ruleStrs = pc.getAutoTypingRules(df);

    int stageIdx = 0;
    for (String ruleStr : ruleStrs) {
      pc.append(dsId, stageIdx++, ruleStr, null, true);   // suppress
    }

    System.err.println(String.format("Rule#%d: create with %s", 0, pc.getCurDf(dsId).dsName));
    for (int i = 0; i < ruleStrs.size(); i++) {
      System.err.println(String.format("Rule#%d: %s", i + 1, ruleStrs.get(i)));
    }

    pc.fetch(dsId, pc.getCurStageIdx(dsId)).show();
    assertEquals(5, pc.getCurStageIdx(dsId));
    return dsId;
  }

  @Test
  public void testCreate() throws TeddyException {
    testCreateInternal();
  }

  @Test
  public void testDrop() throws TeddyException {
    String dsId = testCreateInternal();
    pc.append(dsId, pc.getCurStageIdx(dsId), "drop col: due", null, false).show();
    assertEquals(6, pc.getCurStageIdx(dsId));
  }
}
