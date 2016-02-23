/* SimpleApp.java */
package main.java;
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;

public class SimpleAPP {
	public static void main(String[] args) {
		String logFile = "/i3c/hpcl/huz123/spark/java_example_pras/README.md"; // Should be some file on your system
		SparkConf conf = new SparkConf().setAppName("Simple Application");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> logData = sc.textFile(logFile).cache();

		//***TODO:
		//1. load both process map and function pointer files into this cache above JavaRDD<String>
		//2. Write another to query this loaded file
		long numAs = logData.filter(new Function<String, Boolean>() {
				public Boolean call(String s) { return s.contains("a"); }
				}).count();

		long numBs = logData.filter(new Function<String, Boolean>() {
				public Boolean call(String s) { return s.contains("b"); }
				}).count();

		System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
	}
}
