package cn.org.starpivot.ai.rag.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentLoaderService {

    private final Map<String, DocumentParser> parsers;

    public DocumentLoaderService(List<DocumentParser> parserList) {
        this.parsers = parserList.stream()
                .collect(Collectors.toMap(p -> p.supportedType().toUpperCase(), Function.identity()));
        log.info("已加载文档解析器：{}", parsers.keySet());
    }

    public ParseResult load(InputStream inputStream, String fileName) {
        String fileType = detectFileType(fileName).toUpperCase();
        DocumentParser parser = parsers.get(fileType);
        if (parser == null) {
            return ParseResult.failure("不支持的文件类型：" + fileType + "，目前支持：PDF / DOCX / MD / TXT");
        }
        log.info("[文档加载] 开始解析：fileName={}，type={}", fileName, fileType);
        return parser.parse(inputStream, fileName);
    }

    public static String detectFileType(String fileName) {
        if (fileName == null) {
            return "UNKNOWN";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "UNKNOWN";
        }
        String ext = fileName.substring(dotIndex + 1).toLowerCase();
        return switch (ext) {
            case "pdf" -> "PDF";
            case "docx" -> "DOCX";
            case "md", "markdown" -> "MD";
            case "txt" -> "TXT";
            default -> ext.toUpperCase();
        };
    }
}
