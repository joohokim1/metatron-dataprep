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

package app.metatron.dataprep.exec;

import app.metatron.dataprep.parser.RuleVisitorParser;
import app.metatron.dataprep.parser.exception.RuleException;
import app.metatron.dataprep.parser.rule.Join;
import app.metatron.dataprep.parser.rule.Rule;
import app.metatron.dataprep.teddy.DataFrame;
import app.metatron.dataprep.teddy.Row;
import app.metatron.dataprep.teddy.exceptions.TeddyException;
import app.metatron.dataprep.teddy.exceptions.TransformExecutionFailedException;
import app.metatron.dataprep.teddy.exceptions.TransformExecutionInterrupteddException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executor {

  private static Logger LOGGER = LoggerFactory.getLogger(Executor.class);

  private int dop;
  private int limitRows;
  private int timeout;
  Map<String, ExecutorService> jobs = new HashMap<>();

  public Executor() {
    dop = 2;
    limitRows = 1000000;
    timeout = 3600;
  }

  public Executor(int dop, int limitRows, int timeout) {
    this.dop = dop;
    this.limitRows = limitRows;
    this.timeout = timeout;
  }

  private void addJob(String jobId, ExecutorService es) {
    if (jobId == null) {
      return;
    }
    jobs.put(jobId, es);
  }

  private void removeJob(String jobId) {
    if (jobId == null) {
      return;
    }
    jobs.remove(jobId);
  }

  public void cancelJob(String jobId) {
    ExecutorService es = jobs.get(jobId);
    es.shutdownNow();
  }

  public DataFrame applyRule(DataFrame df, String ruleString, List<DataFrame> slaveDfs) throws TeddyException {
    return applyRule(df, ruleString, slaveDfs, null);
  }

  public DataFrame applyRule(DataFrame df, String ruleString, List<DataFrame> slaveDfs, String jobId)
          throws TeddyException {
    LOGGER.debug("applyRule(): start: ruleString={}", ruleString);

    Rule rule = new RuleVisitorParser().parse(ruleString);
    DataFrame newDf = DataFrame.getNewDf(rule, df.dsName, ruleString);

    try {
      ExecutorService es = Executors.newFixedThreadPool(dop);

      List<Object> args = newDf.prepare(df, rule, slaveDfs);
      int rowcnt = df.rows.size();
      List<Future<List<Row>>> futures = new ArrayList<>();

      if (rowcnt > 0) {
        if (DataFrame.isParallelizable(rule)) {
          int partSize = rowcnt / dop;

          // Outer joins cannot be parallelized. (But, implemented as prepare-gather structure)
          if (rule.getName().equals("join")
                  && ((Join) rule).getJoinType().equalsIgnoreCase("INNER") == false) {
            partSize = rowcnt;
          }

          for (int rowno = 0; rowno < rowcnt; rowno += partSize) {
            LOGGER.debug("applyRule(): add thread: rowno={} partSize={} rowcnt={}", rowno, partSize, rowcnt);
            futures.add(es.submit(new Combiner(newDf, df, args, rowno, Math.min(partSize, rowcnt - rowno), limitRows)));
          }

          addJob(jobId, es);
          es.shutdown();

          for (int i = 0; i < futures.size(); i++) {
            List<Row> rows = futures.get(i).get(timeout, TimeUnit.SECONDS);
            assert rows != null : rule.toString();
            newDf.rows.addAll(rows);
          }
          removeJob(jobId);
        } else {
          // if not parallelizable, newDf comes to be modified directly.
          // then, 'rows' returned is only for assertion.
          List<Row> rows = newDf.gather(df, args, 0, rowcnt, limitRows);
          assert rows == null : ruleString;
        }
      }
    } catch (RuleException e) {
      LOGGER.error("applyRule(): rule syntax error", e);
      throw TeddyException.fromRuleException(e);
    } catch (TimeoutException e) {
      String msg = "applyRule(): transform timeout";
      LOGGER.error(msg, e);
      throw new TransformExecutionInterrupteddException(msg);
    } catch (InterruptedException e) {
      String msg = "applyRule(): interrupted";
      LOGGER.error(msg, e);
    } catch (ExecutionException e) {
      String msg = "applyRule(): transform execution failed";
      LOGGER.error(msg, e);
      throw new TransformExecutionFailedException(msg);
    }

    LOGGER.debug("applyRule(): end: ruleString={}", ruleString);
    return df;
  }
}
