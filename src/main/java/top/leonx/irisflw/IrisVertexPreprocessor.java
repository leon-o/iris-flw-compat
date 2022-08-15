package top.leonx.irisflw;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.Templates;
import com.jozufozu.flywheel.core.compile.*;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileIndexImpl;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.parse.StructField;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrisVertexPreprocessor {
    private final FileResolution header;
    private final Template<? extends VertexData> template;

    public IrisVertexPreprocessor(Template<? extends VertexData> template, FileResolution header) {
        this.header = header;
        this.template = template;
    }

    public String headerPlaceholder = "/\\*\\s*FLW_HEADER\\s*\\*/";
    public String structPlaceholder = "/\\*\\s*FLW_STRUCT\\s*\\*/";
    public String createVertexPlaceholder = "/\\*\\s*FLW_CREATE_VERTEX\\s*\\*/";
    public String assignPlaceholder = "/\\*\\s*.*\\s?=\\s?<FLW_\\w+>\\s*\\*/";
    public String leftValue = "(?<=/\\*).*\\s?(?=\\=)";
    public String rightValue = "(?<=\\=)\\s*<.*>";


    public String preprocess(String irisSource, Context key) {
        StringBuilder headerBuilder = new StringBuilder();

        //key.ctx.getShaderConstants().writeInto(finalSource);

        headerBuilder.append("""
                                     struct Vertex {
                                     	vec3 pos;
                                     	vec4 color;
                                     	vec2 texCoords;
                                     	vec2 light;
                                     	vec3 normal;
                                     };
                                     """);
        headerBuilder.append(key.vertexType.getShaderHeader());
        Pattern headerPattern = Pattern.compile(headerPlaceholder);
        Matcher matcher = headerPattern.matcher(irisSource);
        irisSource = matcher.replaceAll(headerBuilder.toString());

        StringBuilder structBuilder = new StringBuilder();
        structBuilder.append("#define VERTEX_SHADER\n");

        FileIndexImpl index = new FileIndexImpl();

        //Templates.DIFFUSE_FILE.getFile().generateFinalSource(index, finalSource);
        //headerFile
        header.getFile().generateFinalSource(index, structBuilder);

        key.file.generateFinalSource(index, structBuilder);

        VertexData appliedTemplate = template.apply(key.file);
        generateInputAttribute(appliedTemplate, key.vertexType, structBuilder);

        Pattern structPattern = Pattern.compile(structPlaceholder);
        matcher = structPattern.matcher(irisSource);
        irisSource = matcher.replaceAll(structBuilder.toString());

        StringBuilder createVertexBuilder = new StringBuilder();
        generateCreateVertex(appliedTemplate, createVertexBuilder);

        Pattern createVertexPattern = Pattern.compile(createVertexPlaceholder);
        matcher = createVertexPattern.matcher(irisSource);
        irisSource = matcher.replaceAll(createVertexBuilder.toString());

        Pattern assignPattern = Pattern.compile(assignPlaceholder);
        matcher = assignPattern.matcher(irisSource);
        Pattern leftValuePattern = Pattern.compile(leftValue);
        Pattern rightValuePattern = Pattern.compile(rightValue);
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

    public void generateCreateVertex(VertexData template, StringBuilder createVertexBuilder) {
        if (template instanceof InstancingTemplateData instancingTemplate) {

            createVertexBuilder.append(String.format("""
                                                                  Vertex v = FLWCreateVertex();
                                                                  %s i;
                                                                  %s
                                                                  vertex(v, i);
                                                             """, instancingTemplate.instanceName,
                                                     InstancingTemplateData.assignFields(instancingTemplate.instance,
                                                                                         "i.", "a_i_")));

        } else if (template instanceof OneShotTemplateData oneShotTemplateData) {
            createVertexBuilder.append("""
                                                    Vertex v = FLWCreateVertex();
                                                    vertex(v);
                                                    v.light = vec2(0,0.85);
                                               """);
        }
    }

    public void generateInputAttribute(VertexData template, VertexType vertexType, StringBuilder builder) {
        if (template instanceof InstancingTemplateData instancingTemplate) {
            int attributeBinding = vertexType.getLayout().getAttributeCount();
            for (StructField field : instancingTemplate.instance.getFields()) {
                builder.append("layout(location = ").append(attributeBinding).append(") in").append(' ').append(
                        field.type).append(' ').append("a_i_").append(field.name).append(";\n");
                attributeBinding += CompileUtil.getAttributeCount(field.type);
            }
        }
    }

    public static class Context {
        /**
         * The file to compile.
         */
        private final SourceFile file;

        /**
         * The shader constants to apply.
         */
        private final StateSnapshot ctx;

        /**
         * The vertex type to use.
         */
        private final VertexType vertexType;

        public Context(SourceFile file, StateSnapshot ctx, VertexType vertexType) {
            this.file = file;
            this.ctx = ctx;
            this.vertexType = vertexType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var that = (Context) o;
            return file == that.file && vertexType == that.vertexType && ctx.equals(that.ctx);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, ctx, vertexType);
        }
    }
}
