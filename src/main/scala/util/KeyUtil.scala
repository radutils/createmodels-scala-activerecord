package accelia.rad.util

import java.sql.ResultSet

object KeyUtil {

  def isConvention(rs: ResultSet): Boolean = {
    isPrimKey(rs, "id")
  }

  def isPrimKey(rs: ResultSet, column: String): Boolean = {
    var foundIt = false
    while(rs.next() && !foundIt) {
      if (rs.getString("COLUMN_NAME") == column) { foundIt = true }
    }
    rs.beforeFirst()
    foundIt
  }

  def isHasMultiPrimKey(rs: ResultSet): Boolean = {
    var count = 0
    while(rs.next()) { count=count+1 }
    if (count > 0) { rs.beforeFirst();true }
    else { false }
  }

  def isForigenKey(rs: ResultSet): Boolean = {
    val colname = rs.getString("COLUMN_NAME")
    if(colname.endsWith("id") && colname.length != 2) { true }
    else { false }
  }

  def printInfo(rs: ResultSet) = {
    while(rs.next()) {
      println( "COLUMN_NAME:%s [%s] PK_NAME[%s]".
                 format(rs.getString("COLUMN_NAME"), rs.getString("KEY_SEQ"), rs.getString("PK_NAME")))
    }
    rs.beforeFirst()
  }
}
