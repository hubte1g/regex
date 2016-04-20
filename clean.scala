/***
title:-regex-_1_3_-1

***/

/***
Spark YARN configuration -- Runs over 214,692KB in two minutes, output file size 226,723KB
***/
spark-shell --master yarn-client --num-executors 10 --driver-memory 10g --executor-memory 5g --driver-cores 10 --executor-cores 20
--conf -Dspark.akka.retry.wait=30000 \
--conf -Dspark.akka.frameSize=10000 \

/***
//define case class - note generalInfo contains 'Severity|IP Address|NetBIOS Name|DNS Name|MAC Address|Repository'
***/
case class Host(plugName: String, GeneralInfo: String, Family: String, Plugin: String, Exploit: String, Port: String, 
  Solution: String, ExploitEase: String, ExploitFramework: String, FirstDiscovered: String, LastObserved: String, PluginPubDate: String, Tower: String)
 
/***
hdfs dir ...
***/
 val txt = "_no_hdr.csv"

//example of template format that is in OS files
val format1 = new java.text.SimpleDateFormat("yyyy/MM/dd hh:mm:ss")

val format2 = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss a")

//format to whatever I want:
implicit def str2date1(str: String) = {
	try {
  		val dateFormat = format1.parse(str)
		new java.text.SimpleDateFormat("yyyy-MM-dd").format(dateFormat)
	} catch {
  	case e:java.text.ParseException => "N/A".toString
	}	
}

//format to whatever I want:
/***
warning: there were 1 feature warning(s); re-run with -feature for details
str2date2: (str: String)String
***/

implicit def str2date2(str: String) = {
  try {
      val dateFormat = format2.parse(str)
    new java.text.SimpleDateFormat("yyyy-MM-dd").format(dateFormat)
  } catch {
    case e:java.text.ParseException => "N/A".toString
  } 
}

//method defines regex pattern for file.  File is "," delimited.
//fileName: pass name of file to run pattern matching and grouping
def parseLogFile(fileName: String): Iterator[Host] = {

  val p = """((?:"[^"]*"|[^,]*))\,([^,]*\,[^,]*\,[^,]*\,[^,]*\,[^,]*\,[^,]*\,)[^,]*\,([^,]*)\,([^,]*)\,[^,]*\,(?:"[^"]*"|[^,]*)\,[^,]*\,[^,]*\,([^,]*)\,([^,]*)\,((?:"[^"]*"|[^,]*))\,([^,]*)\,((?:"[^"]*"|[^,]*))\,[^,]*\,([^,]*)\,([^,]*)\,([^,]*)\,((?:"[^"]*"|[^,]*))\n""".r
  p.findAllMatchIn(fileName).map(
    m => Host(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6), 
      m.group(7), m.group(8), m.group(9), m.group(10), m.group(11), m.group(12), m.group(13))
    )
}

val rddTest = sc.wholeTextFiles("/local-dir/").flatMap{case (_, txt) => parseLogFile(txt)}

//this one works
/***

***/
val nestedArray = rddTest.collect.map { x => (x.plugName, x.GeneralInfo.replace(",","|"), x.Family, x.Plugin, x.Exploit, x.Port, 
  x.Solution.replace("\n"," "), x.ExploitEase, x.ExploitFramework, str2date2(x.FirstDiscovered), 
  str2date2(x.LastObserved), str2date2(x.PluginPubDate), x.Tower)}

//x.CPE.replace("\r\n",",") <-- that worked for aug2

import java.io._
def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
  val p = new java.io.PrintWriter(f)
  try { op(p) } finally { p.close() }}

/***
Why concat 2 & 3?
***/
printToFile(new File("/tmp/cleanedMar-1.txt"))(p => { 
	nestedArray.map{ x => x._1 + "|" + x._2 + x._3 + "|" + x._4 + "|" + x._5 + "|" + x._6 + "|" + x._7 + "|" + x._8 + "|" + x._9 + "|" + x._10 + "|" + x._11 + "|" + x._12 + "|" + x._13}.foreach(p.println);
	})

/*** does not work 
***/
//testRdd.toSchemaRDD.map(_.mkString("|")).saveAsTextFile("cleaned-Mar")
