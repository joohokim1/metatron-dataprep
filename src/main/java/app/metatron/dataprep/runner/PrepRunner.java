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

import static app.metatron.dataprep.teddy.TeddyUtil.getDateTimeStr;

import app.metatron.dataprep.PrepContext;
import app.metatron.dataprep.SourceDesc;
import app.metatron.dataprep.TargetDesc;
import app.metatron.dataprep.file.PrepCsvUtil;
import app.metatron.dataprep.teddy.ColumnDescription;
import app.metatron.dataprep.teddy.ColumnType;
import app.metatron.dataprep.teddy.DataFrame;
import app.metatron.dataprep.teddy.Row;
import app.metatron.dataprep.teddy.exceptions.TeddyException;
import java.io.IOException;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVPrinter;
import org.joda.time.DateTime;

public class PrepRunner {

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

    String type = cmd.getOptionValue("type");
    String srcUri = cmd.getOptionValue("src-uri");
    String targetUri = cmd.getOptionValue("target-uri");
    verbose = cmd.hasOption("verbose");
    dryRun = cmd.hasOption("dry-run");

    if (type == null) {
      type = "URI";
    }

    if (verbose) {
      System.out.println("type=" + type);
      System.out.println("srcUri=" + srcUri);
      System.out.println("targetUri=" + targetUri);
      for (String ruleStr : cmd.getArgs()) {
        System.out.println(ruleStr);
      }
    }

    PrepContext pc = new PrepContext();

    SourceDesc src = new SourceDesc();
    src.setStrUri(srcUri);

    TargetDesc target = new TargetDesc();
    target.setStrUri(targetUri);

    String dsId = pc.load(src, "runner");
    DataFrame df = pc.fetch(dsId);
    show(df);
    for (String ruleStr : cmd.getArgs()) {
      df = pc.apply(df, ruleStr);
      show(df);
    }

    if (dryRun) {
      System.exit(0);
    }

    CSVPrinter printer = PrepCsvUtil.DEFAULT.getPrinter(targetUri);

    for (int colno = 0; colno < df.getColCnt(); colno++) {
      printer.print(df.getColName(colno));
    }
    printer.println();

    for (int rowno = 0; rowno < df.rows.size(); rowno++) {
      Row row = df.rows.get(rowno);
      for (int colno = 0; colno < df.getColCnt(); ++colno) {
        ColumnDescription colDesc = df.getColDesc(colno);
        Object obj = row.get(colno);

        if (colDesc.getType() == ColumnType.TIMESTAMP && obj != null) {
          printer.print(getDateTimeStr(colDesc, obj));
        } else {
          printer.print(obj);
        }
      }
      printer.println();
    }

    printer.close(true);
  }

  private static Options prepareOptions() {
    Options options = new Options();

    Option dryRun = Option.builder("d")
            .longOpt("dry-run")
            .build();

    Option verbose = Option.builder("v")
            .longOpt("verbose")
            .build();

    Option type = Option.builder()
            .longOpt("type")
            .hasArg()
            .build();

    Option srcUri = Option.builder()
            .longOpt("src-uri")
            .hasArg()
            .build();

    Option targetUri = Option.builder()
            .longOpt("target-uri")
            .hasArg()
            .build();

    return options
            .addOption(verbose)
            .addOption(dryRun)
            .addOption(type)
            .addOption(srcUri)
            .addOption(targetUri);
  }

  private static void show(DataFrame df) {
    if (verbose) {
      df.show();
    }
  }
}
