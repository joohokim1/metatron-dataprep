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

import app.metatron.dataprep.PrepContext;
import app.metatron.dataprep.SourceDesc;
import app.metatron.dataprep.TargetDesc;
import app.metatron.dataprep.teddy.DataFrame;
import app.metatron.dataprep.teddy.exceptions.TeddyException;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
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
    String srcUri = cmd.getOptionValue("src-uri");
    String srcDriver = cmd.getOptionValue("src-driver");
    String srcConnStr = cmd.getOptionValue("src-conn-str");
    String srcUser = cmd.getOptionValue("src-user");
    String srcPw = cmd.getOptionValue("src-pw");
    String srcDb = cmd.getOptionValue("src-db");
    String srcTbl = cmd.getOptionValue("src-tbl");

    String targetType = cmd.getOptionValue("target-type");
    String targetUri = cmd.getOptionValue("target-uri");
    String targetDriver = cmd.getOptionValue("target-driver");
    String targetConnStr = cmd.getOptionValue("target-conn-str");
    String targetUser = cmd.getOptionValue("target-user");
    String targetPw = cmd.getOptionValue("target-pw");
    String targetDb = cmd.getOptionValue("target-db");
    String targetTbl = cmd.getOptionValue("target-tbl");

    verbose = cmd.hasOption("verbose");
    dryRun = cmd.hasOption("dry-run");

    if (srcType == null) {
      srcType = "URI";
    }

    if (targetType == null) {
      targetType = "URI";
    }

    if (verbose) {
      System.out.println("srcType=" + srcType);
      System.out.println("srcUri=" + srcUri);
      System.out.println("srcDriver=" + srcDriver);
      System.out.println("srcConnStr=" + srcConnStr);
      System.out.println("srcUser=" + srcUser);
      System.out.println("srcPw=" + srcPw);
      System.out.println("srcDb=" + srcDb);
      System.out.println("srcTbl=" + srcTbl);

      System.out.println("targetType=" + targetType);
      System.out.println("targetUri=" + targetUri);
      System.out.println("targetDriver=" + targetDriver);
      System.out.println("targetConnStr=" + targetConnStr);
      System.out.println("targetUser=" + targetUser);
      System.out.println("targetPw=" + targetPw);
      System.out.println("targetDb=" + targetDb);
      System.out.println("targetTbl=" + targetTbl);

      for (int i = 0; i < cmd.getArgs().length; i++) {
        System.out.println(String.format("Rule #%d: %s", i, cmd.getArgs()[i]));
      }
    }

    // Load source
    SourceDesc src = new SourceDesc(srcType);
    switch (src.getType()) {
      case URI:
        src.setStrUri(srcUri);
        break;
      case DATABASE:
        src.setDriver(srcDriver);
        src.setConnStr(srcConnStr);
        src.setUser(srcUser);
        src.setPw(srcPw);
        src.setDbName(srcDb);
        src.setTblName(srcTbl);
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
        target.setDriver(srcDriver);
        target.setConnStr(srcConnStr);
        target.setUser(srcUser);
        target.setPw(srcPw);
        target.setDbName(srcDb);
        target.setTblName(srcTbl);
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

  private static Options prepareOptions() {
    Options options = new Options();

    Option dryRun = Option.builder("d")
            .longOpt("dry-run")
            .build();

    Option verbose = Option.builder("v")
            .longOpt("verbose")
            .build();

    Option srcType = Option.builder()
            .longOpt("src-type")
            .hasArg()
            .build();

    Option srcUri = Option.builder()
            .longOpt("src-uri")
            .hasArg()
            .build();

    Option srcDriver = Option.builder()
            .longOpt("src-driver")
            .hasArg()
            .build();

    Option srcConnStr = Option.builder()
            .longOpt("src-conn-str")
            .hasArg()
            .build();

    Option srcUser = Option.builder()
            .longOpt("src-user")
            .hasArg()
            .build();

    Option srcPw = Option.builder()
            .longOpt("src-pw")
            .hasArg()
            .build();

    Option srcDb = Option.builder()
            .longOpt("src-db")
            .hasArg()
            .build();

    Option srcTbl = Option.builder()
            .longOpt("src-tbl")
            .hasArg()
            .build();

    Option targetType = Option.builder()
            .longOpt("targetType")
            .hasArg()
            .build();

    Option targetUri = Option.builder()
            .longOpt("target-uri")
            .hasArg()
            .build();

    Option targetDriver = Option.builder()
            .longOpt("target-driver")
            .hasArg()
            .build();

    Option targetConnStr = Option.builder()
            .longOpt("target-conn-str")
            .hasArg()
            .build();

    Option targetUser = Option.builder()
            .longOpt("target-user")
            .hasArg()
            .build();

    Option targetPw = Option.builder()
            .longOpt("target-pw")
            .hasArg()
            .build();

    Option targetDb = Option.builder()
            .longOpt("target-db")
            .hasArg()
            .build();

    Option targetTbl = Option.builder()
            .longOpt("target-tbl")
            .hasArg()
            .build();

    return options
            .addOption(verbose)
            .addOption(dryRun)
            .addOption(srcType)
            .addOption(srcUri)
            .addOption(srcDriver)
            .addOption(srcConnStr)
            .addOption(srcUser)
            .addOption(srcPw)
            .addOption(srcDb)
            .addOption(srcTbl)
            .addOption(targetType)
            .addOption(targetUri)
            .addOption(targetDriver)
            .addOption(targetConnStr)
            .addOption(targetUser)
            .addOption(targetPw)
            .addOption(targetDb)
            .addOption(targetTbl);
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
