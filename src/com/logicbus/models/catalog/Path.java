package com.logicbus.models.catalog;


/**
 * 路径
 * 
 * <br>
 * 路径由两部分组成：<br>
 * - package <br>
 * - id <br>
 * 
 * 例如：现有路径/demo/logicbus/Helloworld，则package为/demo/logicbus,id为Helloworld.<br>
 * 
 * @author duanyy
 *
 */
public class Path {
	/**
	 * 包
	 */
	protected String pkg;
	/**
	 * ID
	 */
	protected String id;
	
	/**
	 * to get the package
	 * @return package
	 */
	public String getPackage(){return pkg;}
	
	/**
	 * to get the id
	 * @return
	 */
	public String getId(){return id;}
	
	public Path(String path){
		_setPath(normalize(path));
	}

	public Path(String _pkg,String _id){
		pkg = normalize(_pkg);
		id = _id;
	}
	/**
	 * 设置路径
	 * @param path 路径
	 */
	public void setPath(String path){
		_setPath(normalize(path));
	}
	/**
	 * 设置路径
	 * 
	 * <br>
	 * 将路径分解为package和id
	 * @param path 路径
	 */
	private void _setPath(String path) {
		String [] segments = splitPath(path);
		pkg = segments[0];
		id = segments[1];
	}

	/**
	 * 对路径进行规范化
	 * 
	 * <br>
	 * 规范化主要做几件事：<br>
	 * - 保证第一个字符为/
	 * - 保证路径中间/不重复出现
	 * - 保证不以/结尾
	 * 
	 * @param _path
	 * @return
	 */
	public static String normalize(String _path) {
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0 ; i < _path.length() ; i ++){
			if (i == 0 && _path.charAt(i) != '/'){
				buf.append('/');
			}
			if (_path.charAt(i) == '/'){
				if (i+1 < _path.length() && _path.charAt(i+1) != '/'){
					buf.append(_path.charAt(i));
				}
			}else{
				buf.append(_path.charAt(i));
			}
		}
		return buf.toString();
	}
	
	/**
	 * 将一个完整的路径拆分为package和id
	 * 
	 * <br>
	 * 例如：将完整路径/demo/logicbus/Helloworld拆分未/demo/logicbus和Helloworld
	 * 
	 * @param id 完整服务ID
	 * @return
	 */
	static protected String [] splitPath(String id){
		String [] result = new String[2];
		
		int i = id.length() -1;
		for (i = id.length() - 1 ; i >= 0 ; i --){
			if (id.charAt(i) == '/'){
				break;
			}
		}
		
		if (i == -1){
			result[0] = "";
			result[1] = id;
		}else{
			result[0] = id.substring(0,i);
			result[1] = id.substring(i + 1,id.length());
		}
		return result;
	}	
	/**
	 * 获取路径
	 * @return
	 */
	public String getPath(){
		return pkg + "/" + id;
	}
	
	@Override
	public String toString(){
		return pkg + "/" + id;
	}
	
	/**
	 * 在当前路径下,再增加一个层级
	 * @param id 新层级的id
	 * @return
	 */
	public Path append(String id){
		return new Path(getPath() + "/" + id);
	}
	
	public Path append(Path _path){
		return new Path(getPath() + "/" + _path.getPath());
	}
	
	/**
	 * 是否是根路径
	 * @return
	 */
	public boolean isRoot(){
		return id.length() <= 0;
	}
	
	public static void  main(String [] args){
		System.out.println(new Path("//dd//sdsdesd2/////ddd"));
		System.out.println(new Path("//dd//sdsdesd2/////ddd/"));
		System.out.println(new Path("dd//sdsdesd2/////ddd/"));
		System.out.println(new Path("dd"));
		System.out.println(new Path("dd/////ssss"));
		System.out.println(new Path("/"));
	}
}
