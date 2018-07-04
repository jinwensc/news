package cn.yjw.news.common.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.io.orc.OrcSerde;
import org.apache.hadoop.hive.ql.io.orc.OrcStruct;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

/**
 * orc工具类
 * 
 * @author   yangjinwen
 * @Date	 2017年9月15日 	 
 */
public class ORCUtil {

	private StructObjectInspector soi = null;
	
	private OrcStruct orc = null;
	
	private ObjectInspector oi = null;
	
	private List<Object> list = null;
	
	private OrcSerde serde = null;
	
	/**
	 * 根基hive表描述设置读取ORC文件时使用的typeInfo
	 * 
	 * @param type 			orc格式的hive表描述
	 */
	public void setORCtype(String type){
		TypeInfo typeInfo = TypeInfoUtils.getTypeInfoFromTypeString(type);
		soi = (StructObjectInspector) OrcStruct.createObjectInspector(typeInfo);
	}
	
	/**
	 * orc格式文件中的数据，相当于hive中的一行记录，是由orcNewInputFormat读取的
	 * 
	 * @param orcStruct 			hive中的一行记录
	 */
	public void setOrcStruct(OrcStruct orcStruct){
		this.orc = orcStruct;
	}
	
	/**
	 * 拿取一orc格式文件一行记录中某个字段的值
	 * 
	 * @param key				hive表中的某个字段
	 * @return 					字段对应的值(字符串)
	 */
	public String getData(String key){
		StructField structFieldRef = soi.getStructFieldRef(key);
		String values = String.valueOf(soi.getStructFieldData(this.orc, structFieldRef));
		return StringUtils.isEmpty(values) || values.toLowerCase().equals("null") ? null : values;
	}
	
	/**
	 * 初始化ORC配置用于写出ORC格式文件
	 */
	public void setORCWriteType(String type){
		TypeInfo typeInfo = TypeInfoUtils.getTypeInfoFromTypeString(type);
		oi = TypeInfoUtils.getStandardJavaObjectInspectorFromTypeInfo(typeInfo);
	}
	
	/**
	 * 添加用于输出的字段,是一个一个相应类型的对象
	 * 
	 * @param objs				按ORC配置排序好的数据，也可以一个一个的添加
	 */
	public ORCUtil addAttr(Object... objs){
		if(Utils.isEmpty(list)){
			list = new ArrayList<Object>();
		}
		for(Object o:objs){
			list.add(o);
		}
		return this;
	}
	
	/**
	 * 将添加的输出字段转换成用于MR输出的ORC序列化对象
	 * 注意：每输出一次就要清空一次缓存的数据
	 * 
	 * @return				ORC数据的序列化对象
	 */
	public Writable serialize(){
		if(Utils.isEmpty(serde)){
			serde = new OrcSerde();
		}
		Writable line = serde.serialize(list, oi);
		list = new ArrayList<Object>();
		return line;
	}
	
	/**
	 * 将添加的输出字段转换成用于MR输出的ORC序列化对象
	 * 注意：每输出一次就要清空一次缓存的数据
	 * 
	 * @param context					map或reducer的context
	 */
	public void serialize(TaskInputOutputContext<?,?,NullWritable,Writable> context) throws IOException, InterruptedException{
		if(Utils.isEmpty(serde)){
			serde = new OrcSerde();
		}
		Writable line = serde.serialize(list, oi);
		context.write(NullWritable.get(),line);
		list.clear();
	}
	
	
}