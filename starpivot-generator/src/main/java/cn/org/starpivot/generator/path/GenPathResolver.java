package cn.org.starpivot.generator.path;


import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.external.GenPathProfile;



/**

 * 根据 {@link GenPathProfile} 解析 ZIP 内文件路径

 */

public final class GenPathResolver {



    private GenPathResolver() {

    }



    public static String packageToPath(String javaPackage) {
        return GenPathContext.toJavaZipDir(javaPackage);
    }



    /**

     * 解析 ZIP 条目路径；内置与外部生成统一走 {@link GenTemplateRegistry}

     */

    public static String resolveZipEntryPath(String template, GenTable table, GenPathProfile profile) {

        return GenTemplateRegistry.resolveZipEntryPath(template, table, profile);

    }

}


