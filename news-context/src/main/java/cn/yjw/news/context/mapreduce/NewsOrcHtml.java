/**
 * AVRO2ORC.java
 * hadoop.hive.accesslogprocess.format
 * Copyright (c) 2017, 海牛学院版权所有.
*/

package cn.yjw.news.context.mapreduce;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.ql.io.orc.OrcNewOutputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import cn.yjw.news.common.base.MrBase;
import cn.yjw.news.common.utils.ORCUtil;
import cn.yjw.news.context.util.Constant;
import cn.yjw.news.xpath.mapreduce.TextXpath;

/**
 * avro转orc格式类
 * 
 * @author    杨锦文
 * @Date	 2017年12月14日 	 
 */
public class NewsOrcHtml extends MrBase {

	public static class NewsOrcHtmlMapper extends Mapper<LongWritable, Text, NullWritable, Writable> {
		
		private ORCUtil orc = new ORCUtil();

		@Override
		protected void setup(Mapper<LongWritable, Text, NullWritable, Writable>.Context context)
				throws IOException, InterruptedException {
			

			orc.setORCWriteType(Constant.NEWS_ORC_HTML);

		}

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] split = value.toString().split(Constant.SEQ3);
			if (split.length != 2) {
				context.getCounter("content counter", "bad webpage").increment(1L);
				return;
			}
			String url = split[0];
			String html = split[1];
			String host = TextXpath.getHost(url);

			orc.addAttr(url, host, html);
			Writable outValue = orc.serialize();
			context.write(NullWritable.get(), outValue);
		}

	}

	@Override
	public Job getJob(Configuration configuration) throws Exception {

		Job job = Job.getInstance(configuration, getJobName());
		job.setJarByClass(NewsOrcHtml.class);
		job.setMapperClass(NewsOrcHtmlMapper.class);
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Writable.class);
		job.setOutputFormatClass(OrcNewOutputFormat.class);

//		job.addCacheFile(uri);
		FileInputFormat.addInputPath(job, getInputPath());
		FileOutputFormat.setOutputCompressorClass(job, SnappyCodec.class);
		FileOutputFormat.setOutputPath(job, getOutputPath());
		return job;

	}

	@Override
	public String getJobName() {

		return "newsorchtml";

	}
}
