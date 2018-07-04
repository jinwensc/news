/**
 * HFileInputFormat.java
 * hadoop.hbase.mapreduce
 * Copyright (c) 2018, 海牛学院版权所有.
*/

package cn.yjw.news.common.inputformat;

import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.io.hfile.HFile.FileInfo;
import org.apache.hadoop.hbase.io.hfile.HFile.Reader;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFile;
import org.apache.hadoop.hbase.io.hfile.HFileScanner;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * 自定义读取Hfile文件的FileInputFormat
 * @author    杨锦文
 * @Date	 2018年1月17日 	 
 */
public class HBaseInputFormat extends FileInputFormat<ImmutableBytesWritable, KeyValue> {

	/**
	 * 返回HbaseInputFormat要使用的Reader
	 */
	@Override
	public RecordReader<ImmutableBytesWritable, KeyValue> createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException, InterruptedException {

		return new HFileRecordReader(split, context);
	}

	/**
	 * 文件是否拆分,文件的拆分方法
	 */
	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		return false;
	}

	/**
	 * 自定义读取Hfile文件的Reader
	 * @author   杨锦文
	 * @Date	 2018年1月17日
	 */
	private class HFileRecordReader extends RecordReader<ImmutableBytesWritable, KeyValue> {

		private Reader reader;
		private HFileScanner scan;
		private long entryNumber = 0L;

		/**
		 * 创建 HFileRecordReader对象.
		 */
		public HFileRecordReader(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {

			initialize(split, context);
		}

		/**
		 * 初始化scanner和reader
		 */
		@Override
		public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {

			FileSplit fSplit = (FileSplit) split;
			Path path = fSplit.getPath();
			this.reader = HFile.createReader(FileSystem.get(context.getConfiguration()), path,
					new CacheConfig(context.getConfiguration()), context.getConfiguration());
			FileInfo fileInfo = (FileInfo) reader.loadFileInfo();
			System.out.println(fileInfo.size());
			//是否使用缓存,是否随机读取
			this.scan = this.reader.getScanner(false, false);
			//读下一个entry
			this.scan.seekTo();

		}

		/**
		 * 是否有下一个entry,计数来计算进度
		 */
		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			this.entryNumber++;
			return this.scan.next();

		}

		/**
		 * 读hfile物理行的rowKey,不是逻辑行
		 */
		@SuppressWarnings("deprecation")
		@Override
		public ImmutableBytesWritable getCurrentKey() throws IOException, InterruptedException {

			return new ImmutableBytesWritable(scan.getKeyValue().getRow());

		}

		/**
		 * 读取Hfile物理行的value,包括字段名字,版本和值,已经该行所有信息
		 */
		@Override
		public KeyValue getCurrentValue() throws IOException, InterruptedException {

			return (KeyValue) scan.getKeyValue();

		}

		/**
		 * 获得整体进度
		 */
		@Override
		public float getProgress() throws IOException, InterruptedException {
			if (this.reader != null) {
				return this.entryNumber / reader.getEntries();
			} else {
				return 1;
			}
		}

		/**
		 * 关闭reader
		 */
		@Override
		public void close() throws IOException {

			if (this.reader != null) {
				this.reader.close();
			}
		}
	}
}
