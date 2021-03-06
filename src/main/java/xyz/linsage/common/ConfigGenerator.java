package xyz.linsage.common;

import com.jfinal.kit.PathKit;
import xyz.linsage.kit.StringKit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * 常量类生成器
 *
 * @author linsage
 * @create 2017-06-26  下午3:31
 */
public class ConfigGenerator {
	/**
	 * 包名
	 */
	protected String packageName;

	/**
	 * 包模板
	 */
	protected String packageTemplate =
			"package %s;%n%n";

	/**
	 * 类名
	 */
	protected String className;

	/**
	 * 类模板
	 */
	protected String classDefineTemplate =
			"/**%n" +
					" * Generated by linsage, do not modify this file.%n" +
					" */%n" +
					"@SuppressWarnings(\"serial\")%n" +
					"public  class %s {%n";


	/**
	 * 输出文件目录
	 */
	protected String outputDir;

	/**
	 * 内部类模板
	 */
	protected String internalClassDefineTemplate =
			"%s public static class %s {%n";

	/**
	 * 属性模板
	 */
	protected String propertyDefineTemplate =
			"%s public static %s %s;%n";

	/**
	 * @param packageName 包名
	 * @param className   类名
	 */
	public ConfigGenerator(String packageName, String className, String outputDir) {
		this.packageName = packageName;
		this.className = className;
		this.outputDir = outputDir;
	}

	/**
	 * 生成结果
	 *
	 * @param properties
	 */
	public void generate(Properties properties) {
		StringBuilder ret = new StringBuilder();
		try {
			generatePackage(ret);            //包名
			generateClassDefine(ret);        //类定义
			generateContent(properties, ret);        //内部属性+类
			ret.append("}\n");            //结尾
			writeToFile(ret.toString());
			System.out.println(ret.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 生成类定义
	 *
	 * @param ret
	 */
	protected void generateClassDefine(StringBuilder ret) {
		String template = classDefineTemplate;
		ret.append(String.format(template, className));
	}

	/**
	 * 生成包定义
	 *
	 * @param ret
	 */
	protected void generatePackage(StringBuilder ret) {
		ret.append(String.format(packageTemplate, packageName));
	}

	/**
	 * 生成内部类
	 *
	 * @param className
	 * @param tab
	 * @param ret
	 */
	protected void generateInternalClassDefine(String className, String tab, StringBuilder ret) {
		String template = internalClassDefineTemplate;
		ret.append(String.format(template, tab, className));
	}


	/**
	 * 生成属性
	 *
	 * @param propertyName
	 * @param type
	 * @param tab
	 * @param ret
	 */
	protected void generatePropertyDefine(String propertyName, String type, String tab, StringBuilder ret) {
		String template = propertyDefineTemplate;
		ret.append(String.format(template, tab, type, propertyName));
	}


	/**
	 * 生成内容体（内部类+属性）
	 *
	 * @param properties 配置文件
	 * @param ret        字符串引用
	 */
	protected void generateContent(Properties properties, StringBuilder ret) {
		Tree tree = Tree.build(properties);
		recursiveTree(tree, ret);
	}

	/**
	 * 递归内容树
	 *
	 * @param tree
	 * @param ret
	 */
	protected void recursiveTree(Tree tree, StringBuilder ret) {
		int i = 1;
		for (Tree item : tree.getChildren()) {
			if (item.isClass()) {
				ret.append("\n");
				generateInternalClassDefine(item.getName(), StringKit.multiply("\t", tree.getLevel() + 1), ret);
			} else {
				generatePropertyDefine(item.getName(), item.getType(), StringKit.multiply("\t", tree.getLevel() + 1), ret);
				// end
				if (i++ == tree.getChildren().size()) {
					for (int j = item.getLevel(); j > 0; j--) {
						String tab = StringKit.multiply("\t", j);
						ret.append(tab + "}\n");
					}
				}
			}
			recursiveTree(item, ret);
		}
	}


	/**
	 * 若 model 文件存在，则不生成，以免覆盖用户手写的代码
	 */
	protected void writeToFile(String ret) throws IOException {
		File dir = new File(outputDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		String target = outputDir + File.separator + className + ".java";

		File file = new File(target);

		FileWriter fw = new FileWriter(file);
		try {
			fw.write(ret);
		} finally {
			fw.close();
		}
	}


	public static void main(String[] args) {
		/**
		 * 生成常量类
		 */
		// base model 文件保存路径
		String outputDir = PathKit.getWebRootPath() + "/../src/main/java/xyz/linsage/model";
		//包名
		String packageName = "xyz.linsage.model";
		//类名
		String className = "Constant";
		ConfigGenerator g = new ConfigGenerator(packageName, className, outputDir);
		//带顺序，为了和配置文件一致
		LinkedProp prop = LinkedPropKit.use("config.properties");
		g.generate(prop.getProperties());
	}


}
