/**
 * Avro2OrcJob.java
 * hadoop.hive.accesslogprocess
 * Copyright (c) 2017, 海牛学院版权所有.
*/

package cn.yjw.news.context;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import cn.yjw.news.common.bean.ResultBean;
import cn.yjw.news.common.utils.JobRunUtils;
import cn.yjw.news.context.mapreduce.NewsOrcHtml;

/**
 * 将网页内容转换成orc表
 * 
 * @author    杨锦文
 * @Date	 2017年1月22日 	 
 */
public class NewsOrcHtmlJob extends Configured implements Tool {

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		NewsOrcHtml newsOrcHtml = new NewsOrcHtml();
		newsOrcHtml.setConfiguration(conf);
		JobControl jobC = new JobControl(newsOrcHtml.getJobName());
		ControlledJob cJob = newsOrcHtml.getControlledJob();
		jobC.addJob(cJob);

		ResultBean run = JobRunUtils.run(jobC);
		JobRunUtils.printJobInfo(run);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		System.exit(ToolRunner.run(new NewsOrcHtmlJob(), args));
	}
}
