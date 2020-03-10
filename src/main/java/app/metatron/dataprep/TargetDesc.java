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

import static app.metatron.dataprep.TargetDesc.Type.URI;

public class TargetDesc {

  enum Type {
    URI,
    DATABASE,
    STAGING_DB
  }

  // Common properties
  private Type type;
  private Integer limitRows;

  // File kinds
  private String strUri;

  // Hadoop special
  private String hadoopConfDir;

  // DB kinds
  private String driver;
  private String connStr;
  private String dbName;
  private String tblName;
  private String queryStmt;

  // All extra information, like dsId, dsName, and so on, in a JSON form.
  private String custom;

  public TargetDesc() {
    this(URI);
  }

  public TargetDesc(Type type) {
    this.type = type;
    this.limitRows = 1000;

    switch (type) {
      case URI:
        strUri = "/tmp/prep.csv";
        break;
      case DATABASE:
        break;
      case STAGING_DB:
        break;
    }
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Integer getLimitRows() {
    return limitRows;
  }

  public void setLimitRows(Integer limitRows) {
    this.limitRows = limitRows;
  }

  public String getStrUri() {
    return strUri;
  }

  public void setStrUri(String strUri) {
    this.strUri = strUri;
  }

  public String getHadoopConfDir() {
    return hadoopConfDir;
  }

  public void setHadoopConfDir(String hadoopConfDir) {
    this.hadoopConfDir = hadoopConfDir;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getConnStr() {
    return connStr;
  }

  public void setConnStr(String connStr) {
    this.connStr = connStr;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getTblName() {
    return tblName;
  }

  public void setTblName(String tblName) {
    this.tblName = tblName;
  }

  public String getQueryStmt() {
    return queryStmt;
  }

  public void setQueryStmt(String queryStmt) {
    this.queryStmt = queryStmt;
  }

  public String getCustom() {
    return custom;
  }

  public void setCustom(String custom) {
    this.custom = custom;
  }
}
