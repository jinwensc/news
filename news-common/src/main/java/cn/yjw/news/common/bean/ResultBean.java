/**
 * ResultBean.java
 * hadoop.mapreduce.bean
 * Copyright (c) 2017, 海牛学院版权所有.
*/

package cn.yjw.news.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;

/**
 * 存放任务的结果
 * @author    杨锦文
 * @Date	 2017年11月14日 	 
 */
public class ResultBean {
	private long time;
	private boolean isSuccesss;
	private List<String> failedJobList = new ArrayList<String>();
	private Map<String, Counters> counterMap = new HashMap<String, Counters>();

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	
	
	public boolean isSuccesss() {
		return isSuccesss;
	}

	public void setSuccesss(boolean isSuccesss) {
		this.isSuccesss = isSuccesss;
	}

	
	
	public List<String> getFailedJobList() {
		return failedJobList;
	}

	public void addFailedJob(String failedJob) {
		this.failedJobList.add(failedJob);
	}

	public void setFailedJobList(List<String> failedJobList) {
		this.failedJobList = failedJobList;
	}

	
	
	public void setCounterMap(Map<String, Counters> counterMap) {
		this.counterMap = counterMap;
	}

	public Map<String, Counters> getCounters() {
		return counterMap;
	}

	public void addCounter(String jobName, Counters counters) {
		this.counterMap.put(jobName, counters);
	}

}
