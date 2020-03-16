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

package app.metatron.dataprep.runner;

import static app.metatron.dataprep.util.PrepUtil.getLinesFromFile;
import static app.metatron.dataprep.util.PrepUtil.getMapFromJsonFile;
import static app.metatron.dataprep.util.RunnerUtil.prepareOptions;

import app.metatron.dataprep.PrepContext;
import app.metatron.dataprep.SourceDesc;
import app.metatron.dataprep.TargetDesc;
import app.metatron.dataprep.teddy.DataFrame;
import app.metatron.dataprep.teddy.exceptions.TeddyException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class PrepRunner {

  private static PrepContext pc = new PrepContext();

  private static boolean verbose;
  private static boolean dryRun;

  public static void main(String[] args) throws TeddyException, IOException {

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;
    Options options = prepareOptions();

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("PrepRunner", options);
      System.exit(-1);
    }

    String srcType = cmd.getOptionValue("src-type");
    String srcLimit = cmd.getOptionValue("src-limit");
    String srcUri = cmd.getOptionValue("src-uri");
    String srcDriver = cmd.getOptionValue("src-driver");
    String srcConnStr = cmd.getOptionValue("src-conn-str");
    String srcUser = cmd.getOptionValue("src-user");
    String srcPw = cmd.getOptionValue("src-pw");
    String srcDb = cmd.getOptionValue("src-db");
    String srcTbl = cmd.getOptionValue("src-tbl");
    String srcQueryStmt = cmd.getOptionValue("src-query-stmt");

    String targetType = cmd.getOptionValue("target-type");
    String targetUri = cmd.getOptionValue("target-uri");
    String targetDriver = cmd.getOptionValue("target-driver");
    String targetConnStr = cmd.getOptionValue("target-conn-str");
    String targetUser = cmd.getOptionValue("target-user");
    String targetPw = cmd.getOptionValue("target-pw");
    String targetDb = cmd.getOptionValue("target-db");
    String targetTbl = cmd.getOptionValue("target-tbl");

    String srcDescFile = cmd.getOptionValue("src-desc-file");
    String targetDescFile = cmd.getOptionValue("target-desc-file");
    String ruleListFile = cmd.getOptionValue("rule-list-file");

    verbose = cmd.hasOption("verbose");
    dryRun = cmd.hasOption("dry-run");

    if (srcType == null) {
      srcType = "URI";
    }

    if (targetType == null) {
      targetType = "URI";
    }

    if (srcDescFile != null) {
      Map<String, Object> srcDesc = getMapFromJsonFile(srcDescFile);
      srcType = (String) srcDesc.get("type");
      srcLimit = String.valueOf(srcDesc.get("limit"));
      srcUri = (String) srcDesc.get("strUri");
      srcDriver = (String) srcDesc.get("driver");
      srcConnStr = (String) srcDesc.get("connStr");
      srcUser = (String) srcDesc.get("user");
      srcPw = (String) srcDesc.get("pw");
      srcDb = (String) srcDesc.get("db");
      srcTbl = (String) srcDesc.get("tbl");
      srcQueryStmt = (String) srcDesc.get("queryStmt");
    }

    if (targetDescFile != null) {
      Map<String, Object> targetDesc = getMapFromJsonFile(targetDescFile);
      targetType = (String) targetDesc.get("type");
      targetUri = (String) targetDesc.get("strUri");
      targetDriver = (String) targetDesc.get("driver");
      targetConnStr = (String) targetDesc.get("connStr");
      targetUser = (String) targetDesc.get("user");
      targetPw = (String) targetDesc.get("pw");
      targetDb = (String) targetDesc.get("db");
      targetTbl = (String) targetDesc.get("tbl");
    }

    List<String> ruleStrs;
    if (ruleListFile != null) {
      ruleStrs = getLinesFromFile(ruleListFile);
    } else {
      ruleStrs = Arrays.asList(cmd.getArgs());
    }

    if (verbose) {
      System.out.println("srcType=" + srcType);
      System.out.println("srcLimit=" + srcLimit);
      System.out.println("srcUri=" + srcUri);
      System.out.println("srcDriver=" + srcDriver);
      System.out.println("srcConnStr=" + srcConnStr);
      System.out.println("srcUser=" + srcUser);
      System.out.println("srcPw=" + srcPw);
      System.out.println("srcDb=" + srcDb);
      System.out.println("srcTbl=" + srcTbl);
      System.out.println("srcQueryStmt=" + srcQueryStmt);

      System.out.println("targetType=" + targetType);
      System.out.println("targetUri=" + targetUri);
      System.out.println("targetDriver=" + targetDriver);
      System.out.println("targetConnStr=" + targetConnStr);
      System.out.println("targetUser=" + targetUser);
      System.out.println("targetPw=" + targetPw);
      System.out.println("targetDb=" + targetDb);
      System.out.println("targetTbl=" + targetTbl);

      System.out.println("srcDescFile=" + srcDescFile);
      System.out.println("targetDescFile=" + targetDescFile);
      System.out.println("ruleListFile=" + ruleListFile);

      for (int i = 0; i < ruleStrs.size(); i++) {
        System.out.println(String.format("Rule #%d: %s", i, ruleStrs.get(i)));
      }
    }

    // Load source
    SourceDesc src = new SourceDesc(srcType);
    if (srcLimit != null) {
      src.setLimit(Integer.valueOf(srcLimit));
    }

    switch (src.getType()) {
      case URI:
        src.setStrUri(srcUri);
        break;
      case DATABASE:
        src.setDriver(srcDriver);
        src.setConnStr(srcConnStr);
        src.setUser(srcUser);
        src.setPw(srcPw);

        if (srcQueryStmt == null) {
          src.setDbName(srcDb);
          src.setTblName(srcTbl);
        } else {
          src.setQueryStmt(srcQueryStmt);
        }
        break;
      case STAGE_DB:
        assert false;
        break;
    }

    String dsId = pc.load(src, "runner");
    show(dsId);

    // Transform
    DataFrame df = process(dsId, cmd.getArgs());

    if (dryRun) {
      System.exit(0);
    }

    // Save to target
    TargetDesc target = new TargetDesc(targetType);
    switch (target.getType()) {
      case URI:
        target.setStrUri(targetUri);
        break;
      case DATABASE:
        target.setDriver(targetDriver);
        target.setConnStr(targetConnStr);
        target.setUser(targetUser);
        target.setPw(targetPw);
        target.setDbName(targetDb);
        target.setTblName(targetTbl);
        break;
      case STAGING_DB:
        assert false;
        break;
    }

    show(df);
    pc.save(df, target);
  }

  private static DataFrame process(String dsId, String[] ruleStrs) throws TeddyException {
    DataFrame df = pc.fetch(dsId);
    for (String ruleStr : ruleStrs) {
      df = pc.apply(df, ruleStr);
      show(dsId);
    }
    return df;
  }

  private static void show(DataFrame df) {
    if (verbose) {
      df.show();
    }
  }

  private static void show(String dsId) {
    show(pc.fetch(dsId));
  }
}
