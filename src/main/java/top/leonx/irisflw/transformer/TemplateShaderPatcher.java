package top.leonx.irisflw.transformer;

import com.jozufozu.flywheel.core.compile.*;
import com.jozufozu.flywheel.core.source.FileResolution;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateShaderPatcher extends ShaderPatcherBase {

    public TemplateShaderPatcher(Template<? extends VertexData> template, FileResolution header) {
        super(template,header);
    }

    public String headerPlaceholder = "/\\*\\s*FLW_HEADER\\s*\\*/";
    public String structPlaceholder = "/\\*\\s*FLW_STRUCT\\s*\\*/";
    public String createVertexPlaceholder = "/\\*\\s*FLW_CREATE_VERTEX\\s*\\*/";
    public String assignPlaceholder = "/\\*\\s*.*\\s?=\\s?<FLW_\\w+>\\s*\\*/";
    public String leftValue = "(?<=/\\*).*\\s?(?=\\=)";
    public String rightValue = "(?<=\\=)\\s*<.*>";

    private final Pattern structPattern = Pattern.compile(structPlaceholder);
    private final Pattern createVertexPattern = Pattern.compile(createVertexPlaceholder);

    private final Pattern assignPattern = Pattern.compile(assignPlaceholder);

    private final Pattern leftValuePattern = Pattern.compile(leftValue);
    private final Pattern rightValuePattern = Pattern.compile(rightValue);
    private final Pattern headerPattern = Pattern.compile(headerPlaceholder);
    public String patch(String irisSource, Context key) {
        irisSource = patchHead(irisSource, key);
        Matcher matcher;
        VertexData appliedTemplate = template.apply(key.file);
        StringBuilder structBuilder = new StringBuilder();
        genCommonSource(structBuilder, key, appliedTemplate);


        matcher = structPattern.matcher(irisSource);
        irisSource = matcher.replaceAll(structBuilder.toString());

        StringBuilder createVertexBuilder = new StringBuilder();
        generateCreateVertex(appliedTemplate, createVertexBuilder);


        matcher = createVertexPattern.matcher(irisSource);
        irisSource = matcher.replaceAll(createVertexBuilder.toString());


        matcher = assignPattern.matcher(irisSource);

        StringBuilder buffer = new StringBuilder(irisSource);
        while (matcher.find()) {
            String assignMark = matcher.group();
            Matcher lValMatcher = leftValuePattern.matcher(assignMark);
            Matcher rValMatcher = rightValuePattern.matcher(assignMark);
            if (lValMatcher.find() && rValMatcher.find()) {
                String lValue = lValMatcher.group();
                String rValueMark = rValMatcher.group().strip();
                String rValue;
                switch (rValueMark) {
                    case "<FLW_POSITION>":
                        rValue = "FLWVertex(v)";
                        break;
                    case "<FLW_TEXCOORDS>":
                        rValue = "v.texCoords";
                        break;
                    case "<FLW_LIGHT>":
                        rValue = "v.light";
                        break;
                    case "<FLW_NORMAL>":
                        rValue = "v.normal";
                        break;
                    case "<FLW_COLOR>":
                        rValue = "v.color";
                        break;
                    default:
                        continue;
                }
                String assignStat = String.format("%s = %s;", lValue, rValue);

                buffer.replace(matcher.start(), matcher.end(), assignStat);
                matcher = assignPattern.matcher(buffer);
            }
        }
        //finalSource.append(appliedTemplate.generateFooter(index, key.vertexType));

        return buffer.toString();
    }

    private String patchHead(String irisSource, Context key) {
        StringBuilder headerBuilder = new StringBuilder();
        genHeadSource(headerBuilder,key);

        Matcher matcher = headerPattern.matcher(irisSource);
        irisSource = matcher.replaceAll(headerBuilder.toString());
        return irisSource;
    }

}
