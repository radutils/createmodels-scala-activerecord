package accelia.rad.model

//import accelia.rad.model.TableInfo
import accelia.rad.util.{Util, TableUtil, ColumnUtil, KeyUtil}

import java.sql.ResultSet

class ColumnInfo(table:TableInfo, rsP:ResultSet, rsC:ResultSet) {

  val pname:String       = ColumnUtil.getColumnName(rsC)      // カラム名称
  val tname:String       = ColumnUtil.getType(rsC)            // 型名称
  val psize:String       = ColumnUtil.getSize(rsC)            // カラムサイズ
  val comment:String     = ColumnUtil.getComment(rsC)         // カラムコメント
  val isNotNull:Boolean  = ColumnUtil.isNotNull(rsC)          // NULLを許容しないか否か
  val isVarChar:Boolean  = ColumnUtil.isVarChar(rsC)          // CHAR/VARCHARの型のカラムか否か
  val isUnsigned:Boolean = ColumnUtil.isUnsigned(rsC)         // UNSIGNED hogehoge の型のカラムか否か
  val isTimeColumn:Boolean = ColumnUtil.isTimeColumn(rsC)     // created_at,updated_atの名称のカラムか否か
  val isForigen:Boolean  = KeyUtil.isForigenKey(rsC)          // 外部キーか(xxxxx_idの文字列のカラムか)否か
  val isPrim:Boolean     = KeyUtil.isPrimKey(rsP, this.pname) // 主キーか否か
  val isPrimConv:Boolean = KeyUtil.isConvention(rsP)          // 主キーの規約に則しているか否か
  val isVal:Boolean      = true                               // 変数はval かvarか(1:val, 0:varで宣言)

  //コンストラクタ処理　以下2つ実行
  //created_at/updated_atのカラムであれば、クラス宣言時に(with Datestamps）を付与するパラメータをセットする
  if (isTimeColumn) {
    table.hasTimeColumn = true
  }

  //規約に沿った主キー名称、外部キー、created_at/updated_atでないカラムであれば、
  //引数確認で使用する(15以上のコンストラクタ引数であればコンストラクタ引数として宣言しない)フラグを1加算
  if (isForigen || (isPrim && isPrimConv) || isTimeColumn) {
    /* do nothing. */
  } else {
    table.anum = table.anum + 1
  }

  //コンストラクタ引数宣言文字列取得
  def getArgsParam: String = {
    var param:String = ""
    if (isPrim) {
      if (!isPrimConv) {
        param = getPrimParam
      }
    } else if ((table.anum <= Util.MAX_ARGE_SIZE) && !isForigen && !isTimeColumn) { 
        param = "%s%s %s: %s".format(getAnnotation, getVarVal, getParam, getType)
    }
    param
  }

  //クラス内変数宣言文字列取得
  def getInClassParam: String = {
    var param:String = ""

    if (!isPrim) {
      if (isForigen) {
        param = "lazy %s %s = belongsTo[%s]".format(getVarVal, pname, TableUtil._getModelName(pname))
      } else {
        if ((table.anum > Util.MAX_ARGE_SIZE) && !isTimeColumn){
          //param = "%s%s %s: %s = _".format(getAnnotation, getVarVal, getParam, getType)
          param = "%s%s %s: %s = _".format(getAnnotation, "var", getParam, getType)
        }
      }
    }
    param
  }

  //コメント用変数宣言文字列取得
  def getCommentParam: String = {
    var param:String = ""
    if (comment.length != 0) { param = "(\"%s\" -> \"%s\")".format(pname, comment)} 
    param
  }

  //パラメータの型名取得
  private def getType: String   = { if (isNotNull) tname else "Option[" + tname + "]" }
  //変数宣言(var/val)名取得
  private def getVarVal: String = { if (isVal) "val" else "var" }

  //主キー名の取得
  private def getPrimParam: String = {
    "@Column(\"%s\") override val id: %s".format(pname, getType)
  }

  //アノテーション文字列の取得
  private def getAnnotation: String = {
    "%s%s%s%s".format(if (isNotNull)  { "@Required " } else { "" },
                      if (isUnsigned) { "@Range(min=0, max=Long.MaxValue) " } else { "" },
                      if (isVarChar)  { "@Length(min=0, max=%s) ".format(psize) } else { "" },
                      if (isUpperCaseColum || isReservedWord) { "@Column(\"%s\") ".format(pname) } else { "" })
  }

  //パラメータ名の取得。
  private def getParam: String = {
    if (isUpperCaseColum || isReservedWord) { getConvertParam } else { pname }
  }

  //先頭が大文字なら小文字に変換して先頭にアンダースコアを付加した文字列の取得
  private def getConvertParam: String = {
    val r = "([A-Z])([a-zA-Z0-9_]*)".r
    pname match {
      case r(a, n) => "_" + a.toLowerCase() + n
      case _ => "_" + pname
    }
  }

  //カラム名の先頭が大文字で始まっているか確認
  private def isUpperCaseColum = {
     val r = "([A-Z])([a-zA-Z0-9_]*)".r
     pname match {
        case r(a, n) => true
        case _       => false
     }
  }

  //カラム名がScala予約語に該当するか確認
  private def isReservedWord: Boolean = {
    val list:List[String] = List("abstract", "case", "catch", "class", "def", "do", "else", "extends", "false", "final",
                                 "finally", "for", "forSome", "if", "implicit", "import", "lazy", "match", "new", "null",
                                 "object", "override", "package", "private", "protected", "requires", "return", "sealed", "super", 
                                 "this", "throw", "trait", "try", "true", "type", "val", "var", "while", "with yield")
    list.filter( _ == pname).length match {
      case 1 => true
      case _ => false
    }
  }
}
