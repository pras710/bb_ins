/* SimpleApp.java */
package main.java;
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;



import java.util.zip.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class SimpleAPP {
	public static void main(String[] args) throws Exception{
//		String bionicPath = "/i3c/hpcl/huz123/bb_acc/bionic/"; // Should be some file on your system
//		String inputVerboses = "/i3c/hpcl/huz123/bb_acc/traces/verboses/"; // Should be some file on your system
//		SparkConf conf = new SparkConf().setAppName("Bionic Reader");
//		JavaSparkContext sc = new JavaSparkContext(conf);
//		sc.addJar("/i3c/hpcl/huz123/spark/java_example_pras/target/simple-project-1.0.jar");
//		JavaPairRDD<String, String> allBionicFiles = sc.wholeTextFiles(bionicPath);
//		JavaPairRDD<String, String> allVerboseFiles = sc.wholeTextFiles(inputVerboses);
//		//sc.cache();
		LightStrander.main1(args);
//		long sfiles = allBionicFiles.filter(new Function<scala.Tuple2<String, String>, Boolean>(){
//					public Boolean call(Tuple2<String, String> onefile)//for each verbose file
//					{
//						System.out.println(onefile._1);
//						//System.out.println(onefile._2);
//						if(onefile._1.endsWith(".S"))
//						{
//							return true;
//						}
//						return false;
//						//for each chain in the file:
//						
//						//for each .S file in bionic
//					//	allBionicFiles.filter(new Function<scala.Tuple2<String, String>, Double>(){
//					//			}).max();
//					}
//				}).count();
//
//		long summer = allVerboseFiles.filter(new Function<scala.Tuple2<String, String>, Boolean>(){
//					public Boolean call(Tuple2<String, String> onefile)//for each verbose file
//					{
//						//System.out.println(onefile._2);
//						if(onefile._2.contains("verb"))
//						{
//							return true;
//						}
//						return false;
//						//for each chain in the file:
//						
//						//for each .S file in bionic
//					//	allBionicFiles.filter(new Function<scala.Tuple2<String, String>, Double>(){
//					//			}).max();
//					}
//				}).count();
//
//		System.out.println("outs are good?: "+sfiles+" "+summer);
		//***TODO:
		//1. load both process map and function pointer files into this cache above JavaRDD<String>
		//2. Write another to query this loaded file





//old codes
//		long numAs = logData.filter(new Function<String, Boolean>() {
//				public Boolean call(String s) { return s.contains("a"); }
//				}).count();
//
//		long numBs = logData.filter(new Function<String, Boolean>() {
//				public Boolean call(String s) { return s.contains("b"); }
//				}).count();
//
//		System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
	}
}


