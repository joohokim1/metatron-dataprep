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

import app.metatron.dataprep.teddy.DataFrame;
import org.junit.Test;

public class PrepContextTest {

  @Test
  public void testBasic() {
    PrepContext pc = PrepContext.DEFAULT.withCacheMB(1000);

    SourceDesc src = new SourceDesc(UPLOADED);
    src.setStrUri(getResourcePath("sales_named.csv"));
    String dsId = pc.load(src, "testBasic dataset");
    DataFrame df = pc.getCurDf(dsId);
    df.show();
  }
}
