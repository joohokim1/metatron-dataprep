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

package app.metatron.dataprep.db;

import static app.metatron.dataprep.SourceDesc.Type.DATABASE;
import static app.metatron.dataprep.TestUtil.append;
import static app.metatron.dataprep.TestUtil.createSalesTbl;
import static app.metatron.dataprep.TestUtil.loadTblSales;
import static org.junit.Assert.assertEquals;

import app.metatron.dataprep.PrepContext;
import app.metatron.dataprep.SourceDesc;
import app.metatron.dataprep.TargetDesc;
import app.metatron.dataprep.TargetDesc.Type;
import org.junit.BeforeClass;
import org.junit.Test;

public class JdbcTest {

  private static PrepContext pc;

  @BeforeClass
  public static void setUp() {
    pc = PrepContext.DEFAULT.withCacheMB(1000);

    // Prepare a table with independent test code.
    createSalesTbl();
  }

  @Test
  public void loadByRealCode() {
    String dsId = loadTblSales(pc);
    pc.fetch(dsId).show();
  }

  @Test
  public void transform() {
    String dsId = loadTblSales(pc);
    pc.fetch(dsId).show();
    assertEquals(16, pc.fetch(dsId).getColCnt());
    append(pc, dsId, "drop col: due");
    assertEquals(15, pc.fetch(dsId).getColCnt());
  }

  @Test
  public void snapshot() {
    String dsId = loadTblSales(pc);
    pc.fetch(dsId).show();

    TargetDesc target = new TargetDesc(Type.DATABASE);
    target.setDriver("com.mysql.jdbc.Driver");
    target.setConnStr("jdbc:mysql://localhost:3306");
    target.setUser("polaris");
    target.setPw("polaris");
    target.setDbName("test");
    target.setTblName("snapshot");

    pc.flush(dsId, target);
  }

  //  @Test
  public void loadWholeNestedCase() {
    SourceDesc src = new SourceDesc(DATABASE);
    src.setDriver("com.mysql.jdbc.Driver");
    src.setConnStr("jdbc:mysql://c5:3306");
    src.setUser("polaris");
    src.setPw("Metatron123$");
    src.setDbName("campaign");
    src.setTblName("nospcampaigns");

    String dsId = pc.load(src, "nosp list");
    pc.fetch(dsId).show();

    append(pc, dsId, "drop col: id, eventtime");
    append(pc, dsId, "settype col: content type: map");
    append(pc, dsId, "unnest col: content idx: 'result'");
    append(pc, dsId, "drop col: content");
    append(pc, dsId, "flatten col: result");
    pc.fetch(dsId).show();
  }

  @Test
  public void snapshotWholeNestedCase() {
    SourceDesc src = new SourceDesc(DATABASE);
    src.setDriver("com.mysql.jdbc.Driver");
    src.setConnStr("jdbc:mysql://c5:3306");
    src.setUser("polaris");
    src.setPw("Metatron123$");
    src.setDbName("campaign");
    src.setTblName("nospcampaigns");

    String dsId = pc.load(src, "nosp list");
    pc.fetch(dsId).show();

    append(pc, dsId, "drop col: id, eventtime");
    append(pc, dsId, "settype col: content type: map");
    append(pc, dsId, "unnest col: content idx: 'result'");
    append(pc, dsId, "drop col: content");
    append(pc, dsId, "flatten col: result");
    pc.fetch(dsId).show();

    TargetDesc target = new TargetDesc(Type.DATABASE);
    target.setDriver("com.mysql.jdbc.Driver");
//    target.setConnStr("jdbc:mysql://localhost:3306");
//    target.setUser("polaris");
//    target.setPw("polaris");
//    target.setDbName("test");
//    target.setTblName("flat");
    target.setConnStr("jdbc:mysql://c5:3306");
    target.setUser("polaris");
    target.setPw("Metatron123$");
    target.setDbName("campaign");
    target.setTblName("flat");

    pc.flush(dsId, target);
  }
}
