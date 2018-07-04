/**
 * JobRunUtils.java
 * hadoop.mapreduce.util
 * Copyright (c) 2017, 海牛学院版权所有.
*/

package cn.yjw.news.common.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;

import cn.yjw.news.common.bean.ResultBean;



/**
 * 工具类
 * @author    杨锦文
 * @Date	 2017年11月14日 	 
 */
public class JobRunUtils {

	public static ResultBean run(JobControl jobControl) throws InterruptedException, ExecutionException {
		FutureTask<ResultBean> futureTask = new FutureTask<>(new JobMonitor(jobControl));
		Thread monitorThread = new Thread(futureTask);
		monitorThread.start();
		new Thread(jobControl).start(); //obControl.run();
		ResultBean resultBean = futureTask.get();

		return resultBean;
	}

	public static void printJobInfo(ResultBean resultBean) {
		String timeFormat = timeFormat(resultBean.getTime());
		System.out.println("Time for job:" + timeFormat);

		boolean successs = resultBean.isSuccesss();
		System.out.println("Is successed:" + successs);

		if (!successs) {
			System.out.println("Failed job:");
			for (String jobName : resultBean.getFailedJobList()) {
				System.out.println("\t" + jobName);
			}
		}
		Map<String, Counters> counters = resultBean.getCounters();
		for (Map.Entry<String, Counters> entry : counters.entrySet()) {
			System.out.println(entry.getKey() + " Counter----------------------");
			Counters value = entry.getValue();
			Iterable<String> groupNames = value.getGroupNames();

			for (String string : groupNames) {
				CounterGroup group = value.getGroup(string);
				System.out.println("\t" + string);
				for (Counter counter : group) {
					System.out.println("\t\t" + counter.getName() + "  :  " + counter.getValue());
				}
			}

		}

	}

	private static String timeFormat(long time) {
		StringBuilder sb = new StringBuilder();
		long num = time;
		if ((num = time / (1000 * 60 * 60 * 24)) > 0) {
			sb.append(num).append("天");
		}
		if ((num = time % (1000 * 60 * 60 * 24) / (1000 * 60 * 60)) > 0) {
			sb.append(num).append("小时");
		}
		if ((num = time % (1000 * 60 * 60) / (1000 * 60)) > 0) {
			sb.append(num).append("分");
		}
		if ((num = time % (1000 * 60) / (1000)) > 0) {
			sb.append(num).append("秒");
		}
		return sb.toString();
	}

	public static class JobMonitor implements Callable<ResultBean> {
		private JobControl jobControl;

		public JobMonitor(JobControl jobControl) {
			this.jobControl = jobControl;
		}

		@Override
		public ResultBean call() throws Exception {
			long startTime = System.currentTimeMillis();
			while (!jobControl.allFinished()) {
				Thread.sleep(500);
			}
			long endTime = System.currentTimeMillis();
			ResultBean resultBean = new ResultBean();
			List<ControlledJob> failedJobList = jobControl.getFailedJobList();
			for (ControlledJob controlledJob : failedJobList) {
				resultBean.addFailedJob(controlledJob.getJobName());
			}

			resultBean.setSuccesss((failedJobList.size() == 0) ? true : false);

			List<ControlledJob> successfulJobList = jobControl.getSuccessfulJobList();
			for (ControlledJob controlledJob : successfulJobList) {
				resultBean.addCounter(controlledJob.getJobName(), controlledJob.getJob().getCounters());
			}

			resultBean.setTime(endTime - startTime);

			return resultBean;
		}

	}

}
