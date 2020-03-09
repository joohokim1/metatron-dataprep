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
import static java.lang.System.exit;

import app.metatron.dataprep.exception.PrepException;
import app.metatron.dataprep.teddy.DataFrame;
import app.metatron.dataprep.teddy.exceptions.TeddyException;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

public class TestUtil {

  public static String getResourcePath(String relPath) {
    URL url = TestUtil.class.getClassLoader().getResource(relPath);
    return url.toString();
  }

  public static String loadCsv(PrepContext pc, String relPath, boolean autoType) {
    SourceDesc src = new SourceDesc(UPLOADED);
    src.setStrUri(getResourcePath(relPath));
    String dsId = pc.load(src, FilenameUtils.getBaseName(relPath));

    if (autoType) {
      DataFrame df = pc.fetch(dsId);
      List<String> ruleStrs = null;

      try {
        ruleStrs = pc.getAutoTypingRules(df);
      } catch (TeddyException e) {
        System.err.println(e.getMessage());
        assert false;
      }

      int stageIdx = 0;
      for (String ruleStr : ruleStrs) {
        pc.append(dsId, stageIdx++, ruleStr, null, true);   // suppress
      }
    }

    pc.fetch(dsId).show();
    return dsId;
  }

  public static String loadSalesNamed(PrepContext pc) {
    return loadCsv(pc, "sales_named.csv", true);
  }

  public static String loadSample(PrepContext pc) {
    return loadCsv(pc, "teddy/sample.csv", true);
  }

  public static String loadContract(PrepContext pc) {
    return loadCsv(pc, "teddy/contract.csv", true);
  }

  public static String loadStore(PrepContext pc) {
    return loadCsv(pc, "teddy/store.csv", true);
  }

  public static String loadProduct(PrepContext pc) {
    return loadCsv(pc, "teddy/product.csv", true);
  }

  public static String loadNullContained(PrepContext pc) {
    String dsId = loadCsv(pc, "teddy/null_contained.csv", false);

    pc.append(dsId, "header rownum: 1");
    pc.append(dsId, "set col: itemNo value: if(itemNo=='NULL', null, itemNo)");
    pc.append(dsId, "set col: name value: if(name=='NULL', null, name)");
    pc.append(dsId, "set col: speed value: if(speed=='NULL', null, speed)");
    pc.append(dsId, "settype col: itemNo type: long");
    pc.append(dsId, "settype col: speed type: long");
    pc.append(dsId, "settype col: weight type: long");

    return dsId;
  }

  public static String loadPivotTestMultipleColumn(PrepContext pc) {
    return loadCsv(pc, "teddy/pivot_test_multiple_column.csv", true);
  }

  public static String loadDateSample(PrepContext pc) {
    String dsId = loadCsv(pc, "teddy/date_sample.csv", false);

    pc.append(dsId, "header rownum: 1");
    pc.append(dsId, "settype col: birth_date type: timestamp format: 'MM.dd.yyyy HH:mm:ss'");
    pc.append(dsId, "set col: memo value: if(memo=='null', null, memo)");

    return dsId;
  }

  public static void append(PrepContext pc, String dsId, String ruleStr) {
    try {
      pc.append(dsId, ruleStr).show();
    } catch (PrepException e) {
      System.err.println(e.getMessageKey() + ": " + e.getMessageDetail());
      assert false;
    }
  }

}
