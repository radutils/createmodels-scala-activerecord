package accelia.rad.model

import accelia.rad.util._

import java.sql.ResultSet

class TableInfo(rs:ResultSet) {

  val tname:String   = TableUtil.getTableName(rs) //テーブル名称 (DBに登録されているテーブル名)
  val mname:String   = TableUtil.getModelName(rs) //モデル名として使用するテーブル名
  val vname:String   = getTableVal(mname)         //Tables.scala内で使用するテーブル変数名
  val isconv:Boolean = TableUtil.isConvention(rs) //テーブル名が規約に則したものか否か(true:則している)

  var hasTimeColumn:Boolean  = false              //テーブルのカラムにcreated_at/updated_atカラムが存在するか(true:存在する)
  var anum:Int = 0                                //テーブルに含まれるカラム数


  /**
   * Tables.scala object Tables extends ActiveRecordTables 内で記述する変数名の取得
   */
  private def getTableVal(rs:String):String = {
    val r = "([A-Z])([a-zA-Z0-9_]*)".r
    rs match {
      case r(a, n) => a.toLowerCase() + n
      case _ => rs
    }
  }
}
