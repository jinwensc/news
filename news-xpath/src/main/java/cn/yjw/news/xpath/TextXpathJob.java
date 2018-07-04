/**
 * TextXpathJob.java
 * cn.yjw.news.xpath.mapreduce
 * Copyright (c) 2018, 海牛学院版权所有.
*/

package cn.yjw.news.xpath;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import cn.yjw.news.common.bean.ResultBean;
import cn.yjw.news.common.utils.JobRunUtils;
import cn.yjw.news.xpath.mapreduce.TextXpath;

/**
 * 获取html的正文xpath存入mysql
 * 
 * @author    杨锦文
 * @Date	 2018年1月21日 	 
 */
public class TextXpathJob extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		System.exit(ToolRunner.run(new TextXpathJob(), args));

	}

	public int run(String[] args) throws Exception {
		//获得configuration
		Configuration conf = getConf();
		//创建任务对象
		TextXpath textXpath = new TextXpath();
		//设置configuration
		textXpath.setConfiguration(conf);
		//创建工作任务链
		ControlledJob controlledJob = textXpath.getControlledJob();
		JobControl jobC = new JobControl(textXpath.getJobName());
		jobC.addJob(controlledJob);
		//运行任务,获得结果
		ResultBean resultBean = JobRunUtils.run(jobC);
		//打印运行结果
		JobRunUtils.printJobInfo(resultBean);

		return 0;

	}

}
