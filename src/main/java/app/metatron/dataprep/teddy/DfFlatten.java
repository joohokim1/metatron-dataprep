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

package app.metatron.dataprep.teddy;

import static app.metatron.dataprep.util.GlobalObjectMapper.getDefaultMapper;

import app.metatron.dataprep.parser.rule.Flatten;
import app.metatron.dataprep.parser.rule.Rule;
import app.metatron.dataprep.teddy.exceptions.InvalidJsonException;
import app.metatron.dataprep.teddy.exceptions.TeddyException;
import app.metatron.dataprep.teddy.exceptions.WorksOnlyOnArrayException;
import app.metatron.dataprep.util.GlobalObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DfFlatten extends DataFrame {

  private static Logger LOGGER = LoggerFactory.getLogger(DfFlatten.class);

  public DfFlatten(String dsName, String ruleString) {
    super(dsName, ruleString);
  }

  @Override
  public List<Object> prepare(DataFrame prevDf, Rule rule, List<DataFrame> slaveDfs) throws TeddyException {
    List<Object> preparedArgs = new ArrayList<>();
    Flatten flatten = (Flatten) rule;

    String targetColName = flatten.getCol();
    int targetColno = prevDf.getColnoByColName(targetColName);

    if (prevDf.getColType(targetColno) != ColumnType.ARRAY) {
      throw new WorksOnlyOnArrayException(
              "DfFlatten.prepare(): works only on ARRAY: " + prevDf.getColType(targetColno));
    }

    // 컬럼 이름이 바뀌는 일은 없음
    interestedColNames.add(targetColName);

    for (int colno = 0; colno < prevDf.getColCnt(); colno++) {
      if (prevDf.getColName(colno).equals(targetColName)) {
        // 모두 동일한 type, timestamp style을 갖고 있을 가능성이 크고,
        // 아니더라도 mismatch 처리하면 되지만.. 일단 STRING으로 처리.
        addColumn(targetColName, ColumnType.STRING);
      } else {
        addColumnWithDf(prevDf, colno);
      }
    }

    preparedArgs.add(targetColno);
    return preparedArgs;
  }

  @Override
  public List<Row> gather(DataFrame prevDf, List<Object> preparedArgs, int offset, int length, int limit)
          throws InterruptedException, TeddyException {
    List<Row> rows = new ArrayList<>();
    int targetColno = (int) preparedArgs.get(0);

    LOGGER.trace("DfFlatten.gather(): start: offset={} length={} targetColno={}", offset, length, targetColno);

    try {
      for (int rowno = offset; rowno < offset + length; cancelCheck(++rowno)) {
        Row row = prevDf.rows.get(rowno);
        String jsonStr = (String) row.get(targetColno);
        if (jsonStr == null) {
          continue;
        }
        List<Object> list = getDefaultMapper().readValue(jsonStr, new TypeReference<List<Object>>() {});

        for (Object obj : list) {
          Row newRow = new Row();
          for (int colno = 0; colno < prevDf.getColCnt(); colno++) {
            String colName = prevDf.getColName(colno);

            if (colno == targetColno) {
              if (obj instanceof Map || obj instanceof List) {
                newRow.add(colName, getDefaultMapper().writeValueAsString(obj));
              } else {
                newRow.add(colName, obj);
              }
            } else {
              newRow.add(colName, row.get(colno));
            }
          }
          rows.add(newRow);
        }
      }
    } catch (IOException e) {
      LOGGER.error("DfFlatten.gather():", e);
      throw new InvalidJsonException(e.getMessage());
    }

    LOGGER.trace("DfFlatten.gather(): done: offset={} length={} targetColno={}", offset, length, targetColno);
    return rows;
  }
}

