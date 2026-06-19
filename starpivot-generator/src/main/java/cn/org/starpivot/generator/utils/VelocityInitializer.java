package cn.org.starpivot.generator.utils;

import org.apache.velocity.app.Velocity;

import java.io.File;
import java.util.Properties;

/**
 * VelocityEngine工厂
 * 
 * @author StarPivot
 */
public class VelocityInitializer
{
    /**
     * 初始化 Velocity（classpath 模板）
     */
    public static void initVelocity()
    {
        initVelocity(null);
    }

    /**
     * 初始化 Velocity；若指定目录则优先从文件系统加载同名模板，否则回退 classpath
     *
     * @param fileTemplateDir 自定义模板根目录（目录内结构需与 classpath vm/ 一致）
     */
    public static void initVelocity(String fileTemplateDir)
    {
        Properties p = new Properties();
        try
        {
            p.setProperty(Velocity.INPUT_ENCODING, Constants.UTF8);
            if (StringUtils.isNotEmpty(fileTemplateDir))
            {
                File dir = new File(fileTemplateDir.trim());
                p.setProperty("resource.loaders", "file,class");
                p.setProperty("resource.loader.file.class",
                        "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
                p.setProperty("resource.loader.file.path", dir.getAbsolutePath());
                p.setProperty("resource.loader.file.cache", "false");
                p.setProperty("resource.loader.class.class",
                        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            }
            else
            {
                p.setProperty("resource.loader.file.class",
                        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            }
            Velocity.init(p);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}

