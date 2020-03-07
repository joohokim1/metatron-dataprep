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

package app.metatron.dataprep.connector;

import app.metatron.dataprep.SourceDesc;
import app.metatron.dataprep.file.PrepCsvUtil;
import app.metatron.dataprep.file.PrepJsonUtil;
import app.metatron.dataprep.teddy.DataFrame;
import java.io.File;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

public class FileConnector {

  private static boolean exists(String path) {
    File f = new File(path);
    return f.exists();
  }

  public static Configuration getHadoopConf(String hadoopConfDir) {
    if (hadoopConfDir == null) {
      return null;
    }

    String coreSite = hadoopConfDir + File.separator + "core-site.xml";
    String hdfsSite = hadoopConfDir + File.separator + "hdfs-site.xml";

    if (!exists(coreSite)) {
      throw new IllegalArgumentException("MSG_DP_ALERT_HADOOP_CORE_SITE_NOT_FOUND");
    }
    if (!exists(hdfsSite)) {
      throw new IllegalArgumentException("MSG_DP_ALERT_HADOOP_HDFS_SITE_NOT_FOUND");
    }

    Configuration hadoopConf = new Configuration();
    hadoopConf.addResource(new Path(coreSite));
    hadoopConf.addResource(new Path(hdfsSite));

    return hadoopConf;
  }

  public static DataFrame load(SourceDesc src, String dsName) {
    String strUri = src.getStrUri();
    String delim = src.getDelim();
    String quoteChar = src.getQuoteChar();
    Integer colCnt = src.getColCnt();
    String hadoopConfDir = src.getHadoopConfDir();
    Integer limitRows = src.getLimitRows();

    DataFrame df = new DataFrame(dsName);   // to provide dataset name in join, union
    Configuration hadoopConf = getHadoopConf(hadoopConfDir);

    String extensionType = FilenameUtils.getExtension(strUri);
    switch (extensionType) {
      case "json":
        df.setByGrid(PrepJsonUtil.parse(strUri, limitRows, colCnt, hadoopConf));
        break;
      default: // csv
        PrepCsvUtil csvUtil = PrepCsvUtil.DEFAULT
                .withDelim(delim)
                .withQuoteChar(quoteChar)
                .withLimitRows(limitRows)
                .withManualColCnt(colCnt)
                .withHadoopConf(hadoopConf);
        df.setByGrid(csvUtil.parse(strUri));
    }
    return df;
  }
}
