package accelia.rad

import accelia.rad.model._
import accelia.rad.util._

import java.sql.{DriverManager, Connection, ResultSet, DatabaseMetaData}
import java.io.PrintWriter
import java.io.File

//import scala.util.control.NonFatal

case class Args(var user: String, var pass: String, 
                var host: String, var port: String, var db: String, var outdir: String, var help:Boolean) {
  def isParmValidate:Boolean = {
    if (user.length == 0 || host.length == 0 || port.length == 0 || 
        db.length == 0 || outdir.length == 0) { false } else { true }
  }
}

object Parse {

  def main(args:Array[String]) = {

    var host   = "127.0.0.1"
    var port   = "3306"
    var db     = ""
    var user   = ""
    var pass   = ""
    var outdir = ""
    var help   = false

    //def parseArgs(args:List[String]): Option[Args] = {
    def parseArgs(args:List[String]): Args = {
      def _parseArgs(args:List[String]): Args = {
        args match {
          //case "-s" :: arg  => { host = arg.toString; Args(user, pass, host, port, db, outdir) }
          case "-H" :: arg :: rest => { host   = arg; _parseArgs(rest) }
          case "-P" :: arg :: rest => { port   = arg; _parseArgs(rest) }
          case "-D" :: arg :: rest => { db     = arg; _parseArgs(rest) }
          case "-u" :: arg :: rest => { user   = arg; _parseArgs(rest) }
          case "-p" :: arg :: rest => { pass   = arg; _parseArgs(rest) }
          case "-o" :: arg :: rest => { outdir = arg; _parseArgs(rest) }
          case "--help" :: rest    => { help   = true; _parseArgs(rest) }
          case _ => Args(user, pass, host, port, db, outdir, help)
        }
      }
      _parseArgs(args)
      //try { Some(_parseArgs(args)) } catch { case NonFatal(_) => None }
    }

    var arg = parseArgs(args.toList)

    if (arg.help || !arg.isParmValidate || !hasOutPutDir(arg.outdir)) {
      printArgs
    } else {
      println("host[%s], port[%s], db[%s], user[%s], pass[%s], outdir[%s]".
              format(arg.host, arg.port, arg.db, arg.user, arg.pass, arg.outdir))

      val constr:String = "jdbc:mysql://%s:%s/%s?user=%s%s".
                          format(arg.host, arg.port, arg.db, arg.user, 
                                 if (arg.pass.length != 0) { "&password=" + arg.pass } else { "" })

      try {
        Class.forName("com.mysql.jdbc.Driver").newInstance
        val con:Connection = DriverManager.getConnection(constr)

        val md:DatabaseMetaData  = con.getMetaData()
        val rsT = md.getTables(con.getCatalog, null, null, null)

        val list:Map[TableInfo, List[ColumnInfo]] = parseTable(con, md, rsT)
        if (!list.isEmpty) {
          printModelFile(outdir, list)
        } else {
          println("WARN: No Table information was found.")
        }
      } catch {
        case e: Exception  => { println("ERROR: No connection: " + e.getMessage); /*throw e*/ }
      }
    }
  }

  /**
   * 使い方の表示
   */
  def printArgs():Unit = {
    val msg = """
Usage:
       -H host     : [Option]   defalut value 127.0.0.1 
       -P port     : [Option]   default value 3306
       -D database : [Required] default value testdb
       -u user     : [Required] value is not set.
       -p pass     : [Option]   value is not set.
       -o outdir   : [Required] value is not set.
       --help      : show this message.
"""
    println("%s".format(msg))
  }

  /**
   * 出力先のディレクトリが存在するか否か
   * @param dir 確認するディレクトリ名
   * @return ディレクトリが存在すればtrueを、存在しなければfalseを返す
   */
  def hasOutPutDir(dir:String): Boolean = {
    val fl:File = new File(dir)
    if (fl.exists) { true } else { println("Error: No Such Output Directory."); false }
  }

  /**
   * テーブル情報の解析
   */
  def parseTable(con:Connection, md:DatabaseMetaData, rsT:ResultSet):Map[TableInfo, List[ColumnInfo]] = {
    var list:Map[TableInfo, List[ColumnInfo]] = Map.empty[TableInfo, List[ColumnInfo]]

    while(rsT.next()){

      val tinfo:TableInfo = new TableInfo(rsT)

      val rsP = md.getPrimaryKeys(con.getCatalog, null, tinfo.tname)
      val rsC = md.getColumns(con.getCatalog, null, tinfo.tname, null)

      var clist:List[ColumnInfo] = List.empty[ColumnInfo]
      while(rsC.next()) {
        val cinfo:ColumnInfo = new ColumnInfo(tinfo, rsP, rsC)
        clist = cinfo :: clist
      }
      if (!clist.isEmpty) { clist.reverse }
      list = list + (tinfo->clist)
    }

    list

  }

  /**
   * 取得したテーブルとカラムのリストから、outdirで指定したディレクトリにファイルを書き込む。
   * 書き込むファイル名はテーブル名.scalaとする
   *
   * @param outdir 書き込み先ディレクトリ
   * @param list   テーブルとテーブルに紐づくカラム群リスト
   */
  def printModelFile(outdir:String, list:Map[TableInfo, List[ColumnInfo]]) = {
    val mheader = """
package models

import com.github.aselab.activerecord._
import com.github.aselab.activerecord.annotations._
import org.squeryl.annotations._

"""

    var tfile = new PrintWriter(outdir + "/Tables.scala")
    tfile.println("package models")
    tfile.println("")
    tfile.println("import com.github.aselab.activerecord._")
    tfile.println("import com.github.aselab.activerecord.dsl._")
    tfile.println("")
    tfile.println("object Tables extends ActiveRecordTables {")

    list.foreach { case(t,c) =>
      {
        tfile.println("    val %s = table[%s]".format(t.vname, t.mname))

        var alist:List[String] = List.empty[String]  // コンストラクタの引数として
        var clist:List[String] = List.empty[String]  // クラス内の変数宣言として
        var llist:List[String] = List.empty[String]  // クラス内のコメント宣言として

        for (cinfo <- c) {
          var tmp:String = cinfo.getArgsParam
          if (tmp.length != 0) { alist = tmp :: alist; tmp = "" }
          tmp = cinfo.getInClassParam
          if (tmp.length != 0) { clist = tmp :: clist; tmp = "" }
          tmp = cinfo.getCommentParam
          if (tmp.length != 0) { llist = tmp :: llist; tmp = "" }
        }

        var ofile = new PrintWriter(outdir + "/" + t.mname + ".scala")
        ofile.println(mheader)
        ofile.println("case class %s(%s) extends ActiveRecord%s{\n".
                       format(t.mname, 
                              if (!alist.isEmpty) { alist.reduceLeft((x,y) => x + ",\n" + setSpace("case class %s".format(t.mname)) + y) } else { "" } , 
                              if (t.hasTimeColumn) { " with Datestamps " } else { "" }))
        if (!llist.isEmpty) {
          ofile.println("    val labelName:Map[String,String] = Map(%s)\n".format(llist.reduceLeft((x,y) => x + ", " + y)))
        }

        if (!clist.isEmpty) {
          ofile.println("    %s".format(clist.reduceLeft((x,y) => x + "\n    " + y)))
        }
        
        ofile.println("}")
        ofile.println("object %s extends ActiveRecordCompanion[%s]\n".format(t.mname, t.mname))
        ofile.close
      }
    }
    tfile.println("}")
    tfile.close
  }

  def setSpace(cdef:String):String = {
    var rtval = ""
    for ( _ <- 0 to cdef.length) rtval = rtval + " "
    rtval 
  }
}


