package accelia.rad.util

import java.sql.ResultSet

object TableUtil {

  /**
   * DBに登録されているテーブル名を返す
   */
  def getTableName(rs:ResultSet): String = { rs.getString("TABLE_NAME") }

  /**
   * 
   */
  def getModelName(rs:ResultSet): String = {
    def _getModelName(rs:String): String = {
      val r = "([a-zA-Z0-9]*)_([a-zA-Z0-9_]*)".r
      rs match {
        case r(a, n) => a + _getModelName(n.capitalize)
        case _ => rs
      }
    }
    _getModelName(rs.getString("TABLE_NAME").capitalize.dropRight(1)) //最後がsで終わると決め打ち
  }

  def _getModelName(rs:String): String = {
    def __getModelName(rs:String): String = {
      val r = "([a-zA-Z0-9]*)_([a-zA-Z0-9_]*)".r
      rs match {
        case r(a, n) => a + _getModelName(n.capitalize)
        case _ => rs
      }
    }
    __getModelName(rs.capitalize.dropRight(4)) //hogehoge[s_id]と決め打ち...
  }

  /**
   *
   */
  def isConvention(rs: java.sql.ResultSet): Boolean = {
    if(rs.getString("TABLE_NAME").endsWith("s")) { true }
    else { false }
  }

}
