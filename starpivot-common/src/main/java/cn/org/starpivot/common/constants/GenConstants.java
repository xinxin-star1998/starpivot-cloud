package cn.org.starpivot.common.constants;

/**
 * 代码生成模块通用常量。
 * <p>
 * 定义模板类型、树表字段、列类型映射、HTML 控件及 Java 类型等，供 generator 服务读取。
 * 关联实体字段见 {@link cn.org.starpivot.common.entity.BaseEntity}、
 * {@link cn.org.starpivot.common.entity.TreeEntity}。
 * </p>
 */
public class GenConstants
{
    /** CRUD 单表模板 */
    public static final String TPL_CRUD = "crud";
    /** 树形表模板 */
    public static final String TPL_TREE = "tree";
    /** 主子表模板 */
    public static final String TPL_SUB = "sub";
    /** 树编码字段名 */
    public static final String TREE_CODE = "treeCode";
    /** 树父编码字段名 */
    public static final String TREE_PARENT_CODE = "treeParentCode";
    /** 树名称字段名 */
    public static final String TREE_NAME = "treeName";
    /** 上级菜单 ID 字段名 */
    public static final String PARENT_MENU_ID = "parentMenuId";
    /** 上级菜单名称字段名 */
    public static final String PARENT_MENU_NAME = "parentMenuName";
    /** 字符串类型数据库列 */
    public static final String[] COLUMNTYPE_STR = { "char", "varchar", "nvarchar", "varchar2" };
    /** 大文本类型数据库列 */
    public static final String[] COLUMNTYPE_TEXT = { "tinytext", "text", "mediumtext", "longtext" };
    /** 日期时间类型数据库列 */
    public static final String[] COLUMNTYPE_TIME = { "datetime", "time", "date", "timestamp" };
    /** 数值类型数据库列 */
    public static final String[] COLUMNTYPE_NUMBER = { "tinyint", "smallint", "mediumint", "int", "number", "integer",
            "bit", "bigint", "float", "double", "decimal" };
    /** 编辑表单中排除的列 */
    public static final String[] COLUMNNAME_NOT_EDIT = { "id", "create_by", "create_time", "del_flag" };
    /** 列表页中排除的列 */
    public static final String[] COLUMNNAME_NOT_LIST = { "id", "create_by", "create_time", "del_flag", "update_by",
            "update_time" };
    /** 查询条件中排除的列 */
    public static final String[] COLUMNNAME_NOT_QUERY = { "id", "create_by", "create_time", "del_flag", "update_by",
            "update_time", "remark" };
    /** {@link cn.org.starpivot.common.entity.BaseEntity} 对应 Java 字段名 */
    public static final String[] BASE_ENTITY = { "createBy", "createTime", "updateBy", "updateTime", "remark" };
    /** {@link cn.org.starpivot.common.entity.TreeEntity} 扩展字段名 */
    public static final String[] TREE_ENTITY = { "parentName", "parentId", "orderNum", "ancestors", "children" };
    /** 文本输入框 */
    public static final String HTML_INPUT = "input";
    /** 多行文本 */
    public static final String HTML_TEXTAREA = "textarea";
    /** 下拉选择 */
    public static final String HTML_SELECT = "select";
    /** 单选框 */
    public static final String HTML_RADIO = "radio";
    /** 复选框 */
    public static final String HTML_CHECKBOX = "checkbox";
    /** 日期时间选择 */
    public static final String HTML_DATETIME = "datetime";
    /** 图片上传 */
    public static final String HTML_IMAGE_UPLOAD = "imageUpload";
    /** 文件上传 */
    public static final String HTML_FILE_UPLOAD = "fileUpload";
    /** 富文本编辑器 */
    public static final String HTML_EDITOR = "editor";
    /** Java {@link String} 类型 */
    public static final String TYPE_STRING = "String";
    /** Java {@link Integer} 类型 */
    public static final String TYPE_INTEGER = "Integer";
    /** Java {@link Long} 类型 */
    public static final String TYPE_LONG = "Long";
    /** Java {@link Double} 类型 */
    public static final String TYPE_DOUBLE = "Double";
    /** Java {@link java.math.BigDecimal} 类型 */
    public static final String TYPE_BIGDECIMAL = "BigDecimal";
    /** Java {@link java.util.Date} 类型 */
    public static final String TYPE_DATE = "Date";
    /** Java {@link java.time.LocalDateTime} 类型 */
    public static final String TYPE_LOCAL_DATE_TIME = "LocalDateTime";
    /** 模糊查询 */
    public static final String QUERY_LIKE = "LIKE";
    /** 等值查询 */
    public static final String QUERY_EQ = "EQ";
    /** 必填标记值 */
    public static final String REQUIRE = "1";
}
