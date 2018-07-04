/**
 * MrBase.java
 * hadoop.mapreduce.base
 * Copyright (c) 2017, 海牛学院版权所有.
*/

package cn.yjw.news.common.base;

import java.io.IOException;
import java.util.Map.Entry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;

import cn.yjw.news.common.utils.Constants;



/**
 * mapreduce任务基类
 * @author    杨锦文
 * @Date	 2017年11月14日 	 
 */
public abstract class MrBase {
	public static Configuration conf;

	/**
	 * 获取工作链任务对象
	 * @param conf 配置对象
	 * @param args 参数
	 * @return 工作链任务对象
	 * @throws Exception 
	*/
	public ControlledJob getControlledJob() throws Exception {
		FileSystem fs = FileSystem.get(MrBase.conf);
		Path getoutputDir = getOutputPath();
		if (fs.exists(getoutputDir)) {
			fs.delete(getoutputDir, true);
		}

		Configuration configuration = new Configuration();
		for (Entry<String, String> entry : conf) {
			configuration.set(entry.getKey(), entry.getValue());
		}

		Job job = getJob(configuration);

		ControlledJob cJob = new ControlledJob(conf);
		cJob.setJob(job);

		return cJob;
	}

	public abstract Job getJob(Configuration configuration) throws Exception;

	/**
	 * 给配置赋值
	 * @param configuration
	 * @throws IOException
	*/
	public  void setConfiguration(Configuration configuration) {
		MrBase.conf = configuration;
	}

	public Configuration getConfiguration() {
		return conf;
	}

	/**
	 * 获取输出的目录path
	 * @return 输出的目录path
	*/
	public Path getOutputPath() {
		return new Path(Constants.BASE_DIR + MrBase.conf.get(Constants.JOB_OUTPUT_DIR_ATTR));
	}

	/**
	 * 获取任务的名称
	 * @return   任务的名称
	*/
	public abstract String getJobName();

	/**
	 * 获取带ID的任务名称
	 * @return 带ID的任务名称
	*/
	public String getJobNameWithID() {
		return getJobName() + "_" + MrBase.conf.get(Constants.JOB_ID_ATTR);
	}

	/**
	 * 获取输入目录path
	 * @return 输入目录path
	*/
	public Path getInputPath() {
		return new Path(Constants.BASE_DIR + MrBase.conf.get(Constants.JOB_INPUT_DIR_ATTR));
	}
}
