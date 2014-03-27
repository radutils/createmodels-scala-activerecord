package accelia.rad.util

import java.sql.{ResultSet, Types}

object ColumnUtil {

 
  def isNotNull(rs: ResultSet): Boolean = {
    if (rs.getString("IS_NULLABLE") == "NO") true
    else false
  }

  def isTimeColumn(rs: ResultSet): Boolean = {
    if (rs.getString("COLUMN_NAME") == "created_at" || 
        rs.getString("COLUMN_NAME") == "updated_at") {
      true
    } else {
      false
    }
  }

  def isVarChar(rs: ResultSet): Boolean = {
    rs.getString("TYPE_NAME") match {
      case "CHAR"    => { true }
      case "VARCHAR" => { true }
      case _         => { false }
    }
  }

  def isUnsigned(rs: ResultSet): Boolean = {
    rs.getString("TYPE_NAME") match {
      //case "SMALLINT UNSIGNED"  => { true }
      case "MEDIUMINT UNSIGNED" => { true }
      case "INT UNSIGNED"       => { true }
      case "BIGINT UNSIGNED"    => { true }
      case _                    => { false }
    }
  }

  def getSize(rs: ResultSet): String       = { rs.getString("COLUMN_SIZE") }
  def getColumnName(rs: ResultSet): String = { rs.getString("COLUMN_NAME") }
  def getComment(rs: ResultSet): String    = { rs.getString("REMARKS") }


  def getTypeName(rs: ResultSet): String = {
    if ( isNotNull(rs) ) { getType(rs) + "," }
    else { "Option[%s]".format(getType(rs)) + "," }
  }


  /**
   * @see http://dev.mysql.com/doc/refman/5.1/ja/connector-j-reference-type-conversions.html
   */
  def getType(rs: ResultSet): String = {
    //println("Type:%s".format(rs.getString("TYPE_NAME")))
    rs.getString("TYPE_NAME") match {
      case "BIT"                => { "Boolean" }
      case "TINYINT"            => { "Int" }
      case "SMALLINT"           => { "Int" }
      case "SMALLINT UNSIGNED"  => { "Int" }
      case "MEDIUMINT"          => { "Int" }
      case "MEDIUMINT UNSIGNED" => { "Long" }
      case "INT"                => { "Int" }
      case "INT UNSIGNED"       => { "Long" }
      case "BIGINT"             => { "Long" }
      case "BIGINT UNSIGNED"    => { "Long" }
      case "FLOAT"              => { "Float" }
      case "DOUBLE"             => { "Double" }
      case "DECIMAL"            => { "BigDecimal" }
      case "DATE"               => { "Java.sql.Date" }
      case "DATETIME"           => { "Java.sql.Timestamp" }
      case "TIMESTAMP"          => { "Java.sql.Timestamp" }
      case "TIME"               => { "Java.sql.Time" }
      case "YEAR"               => { "NoType" }
      case "CHAR"               => { "String" }
      case "VARCHAR"            => { "String" }
      case "BINARY"             => { "Byte" }
      case "VARBINARY"          => { "Byte" }
      case "TINYBLOB"           => { "Byte" }
      case "BLOB"               => { "Byte" }
      case "MEDIUMBLOB"         => { "Byte"}
      case "LONGBLOB"           => { "Byte" }
      case _                    => { "NoType"}
    }
  }
    
}

