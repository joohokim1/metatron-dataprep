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

import app.metatron.dataprep.teddy.exceptions.ColumnNotContinuousException;
import app.metatron.dataprep.teddy.exceptions.NeedBeforeOrAfterException;
import app.metatron.dataprep.teddy.exceptions.TeddyException;
import app.metatron.dataprep.teddy.exceptions.WrongTargetColumnExpressionException;
import app.metatron.dataprep.teddy.exceptions.WrongTargetPositionException;
import app.metatron.dataprep.parser.rule.Move;
import app.metatron.dataprep.parser.rule.Rule;
import app.metatron.dataprep.parser.rule.expr.Expression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DfMove extends DataFrame {

  private static Logger LOGGER = LoggerFactory.getLogger(DfMove.class);

  public DfMove(String dsName, String ruleString) {
    super(dsName, ruleString);
  }

  @Override
  public List<Object> prepare(DataFrame prevDf, Rule rule, List<DataFrame> slaveDfs) throws TeddyException {
    List<Object> preparedArgs = new ArrayList<>();
    Move move = (Move) rule;

    Expression targetColExpr = move.getCol();
    List<Integer> targetColnos = new ArrayList<>();
    String beforeColName = move.getBefore();
    String afterColName = move.getAfter();
    int destColno = -1;

    LOGGER.debug("DfMove.prepare(): start");

    if (beforeColName != null) {
      for (int colno = 0; colno < prevDf.getColCnt(); colno++) {
        if (beforeColName.equals(prevDf.getColName(colno))) {
          destColno = colno;
          break;
        }
      }
    } else if (afterColName != null) {
      for (int colno = 0; colno < prevDf.getColCnt(); colno++) {
        if (afterColName.equals(prevDf.getColName(colno))) {
          destColno = colno + 1;  // 원 DF를 기준으로 하기 때문에, 기존에는 없던 colno를 가리킬 수도 있음.
          // 아래에서 알아서 처리될 예정
          break;
        }
      }
    } else {
      throw new NeedBeforeOrAfterException("DfMove.prepare(): \"before:\" or \"after:\" clause is needed: " + move);
    }

    List<String> targetColNames = TeddyUtil.getIdentifierList(targetColExpr);
    if (targetColNames.isEmpty()) {
      throw new WrongTargetColumnExpressionException(
              "DfMove.prepare(): wrong target column expression: " + targetColExpr.toString());
    }

    for (String targetColName : targetColNames) {
      targetColnos.add(prevDf.getColnoByColName(targetColName));
    }
    Collections.sort(targetColnos);

    for (int i = 1; i < targetColnos.size(); i++) {
      if (targetColnos.get(i) != targetColnos.get(i - 1) + 1) {
        throw new ColumnNotContinuousException("DfMove.prepare(): columns are not continuous: " + targetColExpr);
      }
    }

    if (targetColnos.get(0) == destColno) {
      throw new WrongTargetPositionException("DfMove.prepare(): target position is same to current position: " + move);
    }

    List<Integer> targetOrder = new ArrayList<>();

    for (int colno = 0; colno < prevDf.getColCnt(); colno++) {
      if (targetColnos.contains(colno)) {   // src 위치면 그냥 통과 (목표 컬럼 위치에 2번 넣을 예정)
        interestedColNames.add(prevDf.getColName(colno));
        continue;
      } else if (colno == destColno) {      // dest 위치면 src 컬럼을 넣고, 그 위치에 있던 컬럼을 넣는다.
        targetOrder.addAll(targetColnos);
        targetOrder.add(colno);
        continue;
      }
      targetOrder.add(colno);           // src도 dest도 아닌 경우
    }
    if (destColno == prevDf.getColCnt()) {     // dest 위치가 제일 끝인 경우
      assert targetOrder.size() == prevDf.getColCnt() - targetColnos.size();
      targetOrder.addAll(targetColnos);
    }

    for (int i = 0; i < targetOrder.size(); i++) {
      int colno = targetOrder.get(i);
      addColumnWithDf(prevDf, colno);
    }

    preparedArgs.add(targetOrder);
    return preparedArgs;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Row> gather(DataFrame prevDf, List<Object> preparedArgs, int offset, int length, int limit)
          throws InterruptedException {
    List<Row> rows = new ArrayList<>();
    List<Integer> targetOrder = (List<Integer>) preparedArgs.get(0);

    LOGGER.trace("DfMove.gather(): start: offset={} length={} targetOrder={}", offset, length, targetOrder);

    for (int rowno = offset; rowno < offset + length; cancelCheck(++rowno)) {
      Row row = prevDf.rows.get(rowno);
      Row newRow = new Row();
      for (int i = 0; i < targetOrder.size(); i++) {
        int colno = targetOrder.get(i);
        newRow.add(prevDf.getColName(colno), row.get(colno));
      }
      rows.add(newRow);
    }

    LOGGER.trace("DfMove.gather(): done: offset={} length={} targetOrder={}", offset, length, targetOrder);
    return rows;
  }
}

