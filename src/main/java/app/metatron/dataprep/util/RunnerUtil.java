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

package app.metatron.dataprep.util;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class RunnerUtil {

  public static Options prepareOptions() {
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

    Option srcLimit = Option.builder()
            .longOpt("src-limit")
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

    Option srcQueryStmt = Option.builder()
            .longOpt("src-query-stmt")
            .hasArg()
            .build();

    Option targetType = Option.builder()
            .longOpt("target-type")
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

    Option srcDescFile = Option.builder()
            .longOpt("src-desc-file")
            .hasArg()
            .build();

    Option targetDescFile = Option.builder()
            .longOpt("target-desc-file")
            .hasArg()
            .build();

    Option ruleListFile = Option.builder()
            .longOpt("rule-list-file")
            .hasArg()
            .build();

    return options
            .addOption(verbose)
            .addOption(dryRun)
            .addOption(srcType)
            .addOption(srcLimit)
            .addOption(srcUri)
            .addOption(srcDriver)
            .addOption(srcConnStr)
            .addOption(srcUser)
            .addOption(srcPw)
            .addOption(srcDb)
            .addOption(srcTbl)
            .addOption(srcQueryStmt)
            .addOption(targetType)
            .addOption(targetUri)
            .addOption(targetDriver)
            .addOption(targetConnStr)
            .addOption(targetUser)
            .addOption(targetPw)
            .addOption(targetDb)
            .addOption(targetTbl)
            .addOption(srcDescFile)
            .addOption(targetDescFile)
            .addOption(ruleListFile);
  }
}
