package cn.org.starpivot.generator.path;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.utils.VelocityUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class GenPathResolverTest {

    @Test
    void builtinCrud_resolvesCorePaths() {
        GenTable table = baseTable(GenConstants.TPL_CRUD, "PmsBrand", "brand", "mall");

        assertEquals(
                "main/java/com/star/pivot/mall/domain/entity/PmsBrand.java",
                GenPathResolver.resolveZipEntryPath("vm/java/domain.java.vm", table, null));
        assertEquals(
                "main/java/com/star/pivot/mall/domain/bo/PmsBrandReqPageBo.java",
                GenPathResolver.resolveZipEntryPath("vm/java/bo/reqBo.java.vm", table, null));
        assertEquals(
                "vue/api/mall/brand.ts",
                GenPathResolver.resolveZipEntryPath("vm/ts/api.ts.vm", table, null));
        assertEquals(
                "vue/views/mall/brand/index.vue",
                GenPathResolver.resolveZipEntryPath("vm/vue/art-design-pro/index.vue.vm", table, null));
    }

    @Test
    void builtinSub_resolvesSubTablePathsWithoutMainTableCollision() {
        GenTable table = baseTable(GenConstants.TPL_SUB, "PmsSpu", "spu", "mall");
        GenTable subTable = baseTable(GenConstants.TPL_CRUD, "PmsSku", "sku", "mall");
        table.setSubTableName("pms_sku");
        table.setSubTable(subTable);

        assertEquals(
                "main/java/com/star/pivot/mall/domain/entity/PmsSku.java",
                GenPathResolver.resolveZipEntryPath("vm/java/sub-domain.java.vm", table, null));
        assertEquals(
                "main/java/com/star/pivot/mall/domain/bo/PmsSkuVO.java",
                GenPathResolver.resolveZipEntryPath("vm/java/bo/sub-vo.java.vm", table, null));
        assertEquals(
                "main/java/com/star/pivot/mall/domain/entity/PmsSpu.java",
                GenPathResolver.resolveZipEntryPath("vm/java/domain.java.vm", table, null));
    }

    @Test
    void externalProfile_usesConfiguredPackagesForWriteLayout() {
        GenTable table = baseTable(GenConstants.TPL_CRUD, "PmsBrand", "brand", "mall");
        GenPathProfile profile = new GenPathProfile();
        profile.setBasePackage("com.star.pivot.mall");
        profile.fillDefaults("mall");
        profile.setApiPath("star-pivot-ui/src/api/mall");
        profile.setVuePagePath("star-pivot-ui/src/views/mall/brand");

        assertEquals(
                "star-pivot-ui/src/api/mall/brand.ts",
                GenPathResolver.resolveZipEntryPath("vm/ts/api.ts.vm", table, profile));
        assertEquals(
                "star-pivot-ui/src/views/mall/brand/index.vue",
                GenPathResolver.resolveZipEntryPath("vm/vue/art-design-pro/index.vue.vm", table, profile));
        assertEquals(
                "star-pivot-ui/src/views/mall/brand/modules/brand-search.vue",
                GenPathResolver.resolveZipEntryPath("vm/vue/art-design-pro/modules/search.vue.vm", table, profile));
    }

    @Test
    void externalZipExport_usesBuiltinFrontendLayout() {
        GenTable table = baseTable(GenConstants.TPL_CRUD, "PmsBrand", "brand", "mall");
        GenPathProfile writeProfile = new GenPathProfile();
        writeProfile.setBasePackage("com.star.pivot.mall");
        writeProfile.fillDefaults("mall");
        writeProfile.setApiPath("star-pivot-ui/src/api/mall");
        writeProfile.setVuePagePath("star-pivot-ui/src/views/mall/brand");
        GenPathProfile zipProfile = GenPathProfile.forZipExport(table, writeProfile);

        assertEquals(
                "main/java/com/star/pivot/mall/domain/entity/PmsBrand.java",
                GenPathResolver.resolveZipEntryPath("vm/java/domain.java.vm", table, zipProfile));
        assertEquals(
                "vue/api/mall/brand.ts",
                GenPathResolver.resolveZipEntryPath("vm/ts/api.ts.vm", table, zipProfile));
        assertEquals(
                "vue/views/mall/brand/index.vue",
                GenPathResolver.resolveZipEntryPath("vm/vue/art-design-pro/index.vue.vm", table, zipProfile));
        assertEquals(
                "vue/views/mall/brand/modules/brand-search.vue",
                GenPathResolver.resolveZipEntryPath("vm/vue/art-design-pro/modules/search.vue.vm", table, zipProfile));

        var ctx = VelocityUtils.prepareContext(table, zipProfile);
        assertEquals("vue/api/mall", ctx.get("apiPath"));
    }

    @Test
    void externalWithoutVuePagePath_fallsBackToBuiltinVueLayout() {
        GenTable table = baseTable(GenConstants.TPL_CRUD, "PmsBrand", "brand", "mall");
        GenPathProfile profile = new GenPathProfile();
        profile.setBasePackage("com.star.pivot.mall");
        profile.fillDefaults("mall");
        profile.setVuePagePath("");

        assertEquals(
                "vue/views/mall/brand/index.vue",
                GenPathResolver.resolveZipEntryPath("vm/vue/v3/index.vue.vm", table, profile));
        assertEquals(
                "vue/views/mall/brand/modules/brand-dialog.vue",
                GenPathResolver.resolveZipEntryPath("vm/vue/v3/modules/dialog.vue.vm", table, profile));
    }

    @Test
    void treeTemplate_usesReqBoSuffix() {
        GenTable table = baseTable(GenConstants.TPL_TREE, "SysDept", "dept", "system");

        assertEquals(
                "main/java/com/star/pivot/system/domain/bo/SysDeptReqBo.java",
                GenPathResolver.resolveZipEntryPath("vm/java/bo/reqBo.java.vm", table, null));
    }

    @Test
    void vue2WebType_usesJsApiTemplate() {
        GenTable table = baseTable(GenConstants.TPL_CRUD, "PmsBrand", "brand", "mall");
        table.setTplWebType("element-ui");

        assertEquals(
                "vue/api/mall/brand.js",
                GenPathResolver.resolveZipEntryPath("vm/js/api.js.vm", table, null));
        assertEquals(
                "vue/views/mall/brand/index.vue",
                GenPathResolver.resolveZipEntryPath("vm/vue/v2/index.vue.vm", table, null));
        assertEquals("vm/js/api.js.vm", GenTemplateRegistry.resolveApiTemplate("element-ui"));
        assertEquals("vm/ts/api.ts.vm", GenTemplateRegistry.resolveApiTemplate("art-design-pro"));
        assertTrue(GenTemplateRegistry.getTemplateList(GenConstants.TPL_CRUD, "element-ui")
                .contains("vm/js/api.js.vm"));
        assertTrue(GenTemplateRegistry.getTemplateList(GenConstants.TPL_CRUD, "art-design-pro")
                .contains("vm/ts/api.ts.vm"));
    }

    @Test
    void allBuiltinTemplates_areRegistered() {
        GenTemplateRegistry.validateAllTemplateListCoverage();
    }

    @Test
    void unknownTemplate_throwsBizException() {
        GenTable table = baseTable(GenConstants.TPL_CRUD, "PmsBrand", "brand", "mall");
        assertThrows(Exception.class,
                () -> GenPathResolver.resolveZipEntryPath("vm/java/unknown.java.vm", table, null));
    }

    @Test
    void velocityContext_packagesAlignWithZipPathResolver() {
        GenTable table = baseTable(GenConstants.TPL_CRUD, "PmsBrand", "brand", "mall");
        table.setPackageName("com.legacy.wrong.package");
        GenPathProfile profile = new GenPathProfile();
        profile.setBasePackage("com.star.pivot.mall");
        profile.fillDefaults("mall");
        profile.setApiPath("star-pivot-ui/src/api/mall");

        var ctx = VelocityUtils.prepareContext(table, profile);
        assertEquals("com.star.pivot.mall.domain.entity", ctx.get("entityPackage"));
        assertEquals("com.star.pivot.mall.domain.dto", ctx.get("dtoPackage"));
        assertEquals("star-pivot-ui/src/api/mall", ctx.get("apiPath"));

        assertEquals(
                "main/java/com/star/pivot/mall/domain/entity/PmsBrand.java",
                GenPathResolver.resolveZipEntryPath("vm/java/domain.java.vm", table, profile));
        assertEquals(
                "star-pivot-ui/src/api/mall/brand.ts",
                GenPathResolver.resolveZipEntryPath("vm/ts/api.ts.vm", table, profile));
    }

    @Test
    void getFileName_delegatesToRegistry() {
        GenTable table = baseTable(GenConstants.TPL_CRUD, "PmsBrand", "brand", "mall");
        String template = "vm/java/mapper.java.vm";
        assertEquals(
                VelocityUtils.getFileName(template, table),
                GenPathResolver.resolveZipEntryPath(template, table, null));
    }

    private static GenTable baseTable(String tplCategory, String className, String businessName, String moduleName) {
        GenTable table = new GenTable();
        table.setTplCategory(tplCategory);
        table.setTplWebType("art-design-pro");
        table.setClassName(className);
        table.setBusinessName(businessName);
        table.setModuleName(moduleName);
        table.setPackageName("com.star.pivot." + moduleName);
        table.setTableName("demo_table");
        table.setColumns(Collections.<GenTableColumn>emptyList());
        return table;
    }
}
