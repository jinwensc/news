/**
 * NewsOrcContext.java
 * cn.yjw.news.context.mapreduce
 * Copyright (c) 2018, 海牛学院版权所有.
*/

package cn.yjw.news.context.mapreduce;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import cn.yjw.news.common.base.MrBase;
import cn.yjw.news.common.utils.C3P0Util;

/**
 * 获取html正文，
 * 
 * @author    杨锦文
 * @Date	 2018年1月22日 	 
 */
public class NewsOrcContext extends MrBase {
//	TreeMap<String, String> hostMap = new TreeMap<>();
	
//	URI[] cacheFiles = context.getCacheFiles();
//	if (cacheFiles.length != 1) {
//		throw new IOException("cache file error!!!");
//	}
//
//	InputStream inputStream = new FileInputStream(cacheFiles[0].toString());
//
//	String line = null;
//	try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
//		while ((line = br.readLine()) != null) {
//			String[] split = line.split("\\s+");
//			if (split.length != 2) {
//				continue;
//			}
//			String host = split[0];
//			String xpath = split[1];
//			hostMap.put(host, xpath);
//		}
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
	
	
	/**
	 * 读取mysql写到本地文件
	 * @param path 本地文件名
	 */
	public void writeXpathFile(String path) {
		//获取连接
		Connection connection = C3P0Util.getConnection();
		String sql = "select host ,IFNULL(manual_set_xpath,xpath) context_xpath from news_host_xpath WHERE status = 0;";
		Statement createStatement = null;
		ResultSet executeQuery = null;
		HashMap<String, String> xpathMap = new HashMap<String, String>();
		try {
			//查询结果
			createStatement = connection.createStatement();
			executeQuery = createStatement.executeQuery(sql);
			while (executeQuery.next()) {
				String host = executeQuery.getString(1);
				String xpath = executeQuery.getString(2);
				//将结果存到map
				xpathMap.put(host, xpath);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			//关闭资源
			C3P0Util.close(connection, null, executeQuery);
			if (createStatement != null) {
				try {
					createStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		BufferedWriter bw = null;
		//写出map的内容到文件
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
			for (Iterator<Entry<String, String>> iterator = xpathMap.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, String> kv = iterator.next();
				bw.write(kv.getKey() + "\001" + kv.getValue() + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//关闭资源
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * 上传xpath文件到集群
	 * @path 本地目录,hdfs目录由conf的xpath.cachedir传递
	 * 
	 */
	public void putXpathFile(String path, Configuration conf) {
		try {
			FileSystem fs = FileSystem.get(conf);
			Path f = new Path(path);
			if (!fs.isDirectory(f.getParent())) {
				fs.mkdirs(f.getParent());
			}
			if (fs.exists(f)) {
				fs.delete(f, false);
			}
			fs.copyFromLocalFile(f, new Path(conf.get("xpath.cachedir")));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Job getJob(Configuration configuration) throws Exception {
		Job job = Job.getInstance(configuration, getJobName());
		job.setJarByClass(NewsOrcContext.class);

		
		return job;

	}

	@Override
	public String getJobName() {

		return "newsorccontext";

	}

}
