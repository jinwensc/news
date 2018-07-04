/**
 * TextXpath.java
 * cn.yjw.news.xpath.mapreduce
 * Copyright (c) 2018, 海牛学院版权所有.
*/

package cn.yjw.news.xpath.mapreduce;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import cn.yjw.news.common.base.MrBase;
import cn.yjw.news.common.utils.C3P0Util;
import cn.yjw.news.common.utils.Utils;
import cn.yjw.news.xpath.util.Constant;
import cn.yjw.news.xpath.util.HtmlContentExtractor;


/**
 * 获取网页正文内容的xpath
 * 
 * @author    杨锦文
 * @Date	 2018年1月20日 	 
 */
public class TextXpath extends MrBase {
	public static class TextXpathMapper extends Mapper<LongWritable, Text, Text, Text> {
		private Text keyOut = new Text();
		private Text valueOut = new Text();

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] split = value.toString().split(Constant.SEQ3);
			if (split.length != 2) {
				context.getCounter("content counter", "bad webpage").increment(1L);
				return;
			}
			String url = split[0];
			String html = split[1];
			String contentXpath = "";

			StringBuilder negative = new StringBuilder();
			Map<String, String> generateXpath = HtmlContentExtractor.generateXpath(html);
			if (generateXpath == null) {
				context.getCounter("content counter", "word number is less than 50 ").increment(1L);
				return;
			}

			try {
				for (Iterator<Entry<String, String>> iterator = generateXpath.entrySet().iterator(); iterator
						.hasNext();) {
					Entry<String, String> kv = iterator.next();

					if (kv.getValue().equals(HtmlContentExtractor.CONTENT)) {
						contentXpath = kv.getKey();
					} else {
						//记录反规则,没用上
						negative.append(kv.getKey());
						negative.append(":");
						negative.append(kv.getValue());
						if (iterator.hasNext()) {
							negative.append(",");
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (Utils.isEmpty(contentXpath)) {
				context.getCounter("content counter", "find no xpath").increment(1L);
				return;
			}
			String host = getHost(url);
			if (Utils.isEmpty(host)) {
				context.getCounter("content counter", "find no host").increment(1L);
				return;
			}
			keyOut.set(host);
			valueOut.set(contentXpath);
			context.write(keyOut, valueOut);
		}

	}

	public static class TextXpathReducer extends Reducer<Text, Text, NullWritable, NullWritable> {
		private HashMap<String, Integer> map = new HashMap<String, Integer>();
		private Connection connection;
		String sql = "insert into news_host_xpaths (host,xpath,count,total) values (?,?,?,?);";

		@Override
		protected void setup(Reducer<Text, Text, NullWritable, NullWritable>.Context context)
				throws IOException, InterruptedException {
			connection = C3P0Util.getConnection();
		}

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			String xpath = "";
			map.clear();

			String host = key.toString();

			//该host的总网页数,不包括无xpath和无正文的
			int total = 0;

			//将每个xpath的数量存入map
			for (Text text : values) {
				xpath = text.toString();
				Integer count = map.get(xpath);
				if (count == null || count == 0) {
					map.put(xpath, 1);
				} else {
					map.put(xpath, count + 1);
				}
				total++;
			}

			/**
			 * 输出到mysql,需要配置c3p0.properties
			 */
			PreparedStatement prepareStatement = null;
			try {
				int temp = 0;
				prepareStatement = connection.prepareStatement(sql);
				for (Iterator<Entry<String, Integer>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
					Entry<String, Integer> next = iterator.next();
					prepareStatement.setString(1, host);
					prepareStatement.setString(2, next.getKey());
					prepareStatement.setInt(3, next.getValue());
					prepareStatement.setInt(4, total);
					prepareStatement.addBatch();
					temp++;
				}
				if (temp > 0) {
					prepareStatement.executeBatch();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				C3P0Util.close(null, prepareStatement, null);
			}
		}

		@Override
		protected void cleanup(Reducer<Text, Text, NullWritable, NullWritable>.Context context)
				throws IOException, InterruptedException {
			C3P0Util.close(connection, null, null);
		}
	}

	/**
	 * 获取url的xpath
	*/
	public static String getHost(String url) {
		if (!(StringUtils.startsWithIgnoreCase(url, "http://") || StringUtils.startsWithIgnoreCase(url, "https://"))) {
			url = "http://" + url;
		}
		String returnVal = StringUtils.EMPTY;
		try {
			URI uri = new URI(url);
			returnVal = uri.getHost();
		} catch (Exception e) {
		}
		if ((StringUtils.endsWithIgnoreCase(returnVal, ".html") || StringUtils.endsWithIgnoreCase(returnVal, ".htm"))) {
			returnVal = StringUtils.EMPTY;
		}
		return returnVal;
	}

	

	@Override
	public Job getJob(Configuration configuration) throws Exception {
		//创建工作对象
		Job job = Job.getInstance(configuration, getJobName());
		job.setJarByClass(TextXpath.class);
		//设置map
		job.setMapperClass(TextXpathMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		//设置reduce
		job.setReducerClass(TextXpathReducer.class);
		job.setNumReduceTasks(1);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(NullWritable.class);
		job.setInputFormatClass(TextInputFormat.class);
		//设置输入目录
		FileInputFormat.addInputPath(job, getInputPath());
		//输出到mysql
		FileOutputFormat.setOutputPath(job, getOutputPath());
		return job;
	}

	@Override
	public String getJobName() {
		return "textxpath";
	}

}
