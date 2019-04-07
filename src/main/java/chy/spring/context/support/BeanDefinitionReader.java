package chy.spring.context.support;

import chy.spring.bean.BeanDefinition;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//用对配置文件进行查找，读取、解析
public class BeanDefinitionReader {
    Properties properties = null;
    //拿到所有类,包括没有注解的
    private List<String> scanAllClass = new ArrayList<String>();

    private final static String scanPackage = "package";

    public BeanDefinitionReader(String path)  {
        //读取配置文件
        setProperties(path);

        //开始扫描
        String packagePath = (String) properties.get(scanPackage);
        doScan(packagePath);
    }


    public void setProperties(String path){
        InputStream is =this.getClass().getClassLoader().getResourceAsStream(path);
        try {
            properties  = new Properties();
            properties.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //扫描路径下的包
    private void doScan(String packagePath)  {


        String url = null;
        try {
            url = this.getClass().getClassLoader().getResource( packagePath.replaceAll("\\.","/")).toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        File classDir = new File(url);
        String[] files = classDir.list();
        for (File file : classDir.listFiles()) {
            String fileName = file.getName();
            String newName = packagePath + "." + fileName;
            if(file.isDirectory()){
                doScan(newName);
            }else{
                scanAllClass.add(newName.replace(".class",""));
            }
        }
    }

    public List<String> loadBeanDefinitions() {
        return scanAllClass;
    }


    public BeanDefinition getDefinition(String beanPath) {
        if(!scanAllClass.contains(beanPath)){
            return null;
        }
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClassName(beanPath);
        beanDefinition.setFactoryBeanName(beanPath);
        return beanDefinition;
    }



}
