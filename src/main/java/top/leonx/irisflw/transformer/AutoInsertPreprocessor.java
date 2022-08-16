package top.leonx.irisflw.transformer;

import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.source.FileResolution;
import top.leonx.irisflw.accessors.BufferBuilderAccessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoInsertPreprocessor extends PreprocessorBase{
    Pattern versionPattern = Pattern.compile("#version\\s+\\d+");
    Pattern ftransformAssignPattern = Pattern.compile("ftransform\\(\\)");
    Pattern textureMatrixPattern = Pattern.compile("gl_TextureMatrix\\[\\d\\]");
    Pattern boxCoordDetector = Pattern.compile("BoxCoord");
    Pattern definePattern = Pattern.compile("(?<=#define\\s)\\w+(?=\\s+)");

    public String mainFunctionRegex = "void\\s+main\\(\\s*\\)\\s*\\{";
    private Pattern mainFunctionPattern = Pattern.compile(mainFunctionRegex);

    public AutoInsertPreprocessor(Template<? extends VertexData> template, FileResolution header) {
        super(template, header);
    }

    @Override
    public String preprocess(String irisSource, TemplatePreprocessor.Context key) {
        StringBuilder builder = new StringBuilder(irisSource);
        VertexData appliedTemplate = template.apply(key.file);
        StringBuilder predefinedCodeBuilder = new StringBuilder();
        genPredefine(key, builder, appliedTemplate, predefinedCodeBuilder);
        renameFlwDefine(predefinedCodeBuilder);
        replaceOriginalDefine(builder, predefinedCodeBuilder);

        StringBuilder createVertexBuilder = new StringBuilder();
        createVertexBuilder.append('\n');
        generateCreateVertex(appliedTemplate,createVertexBuilder);
        createVertexBuilder.append("""
                                           vec4 _flw_patched_vertex_pos = FLWVertex(v);
                                           """);
        createVertexBuilder.append('\n');

        Matcher versionMatcher = versionPattern.matcher(builder);
        if (versionMatcher.find()) {
            builder.insert(versionMatcher.end(),predefinedCodeBuilder);
        }


        Matcher mainFuncMatcher = mainFunctionPattern.matcher(builder);

        if (mainFuncMatcher.find()) {
            builder.insert(mainFuncMatcher.end(),createVertexBuilder);
        }

        Matcher ftransformMatcher = ftransformAssignPattern.matcher(builder);
        while (ftransformMatcher.find()){
            builder.replace(ftransformMatcher.start(),ftransformMatcher.end(),
                            "_flw_patched_vertex_pos");

            ftransformMatcher = ftransformAssignPattern.matcher(builder);
        }


        Matcher textureMatrixMatcher = textureMatrixPattern.matcher(builder);
        while (textureMatrixMatcher.find()){
            builder.replace(textureMatrixMatcher.start(),textureMatrixMatcher.end(),
                            "1.0");

            textureMatrixMatcher = textureMatrixPattern.matcher(builder);
        }
        return builder.toString();
    }

    private void replaceOriginalDefine(StringBuilder builder, StringBuilder predefinedCodeBuilder) {
        predefinedCodeBuilder.append("""
                                             #undef gl_Vertex
                                             #undef gl_MultiTexCoord0
                                             #undef gl_Normal
                                             #undef gl_Color
                                             #define gl_Vertex (inverse(gl_ProjectionMatrix*gl_ModelViewMatrix)*_flw_patched_vertex_pos)
                                             #define gl_MultiTexCoord0 (vec4(v.texCoords,0,1))
                                             #define gl_Normal (v.normal)
                                             #define gl_Color (v.color)
                                             """);


        if (boxCoordDetector.matcher(builder).find()) {
            predefinedCodeBuilder.append("""
                                            #undef gl_MultiTexCoord1
                                            #define gl_MultiTexCoord1 (vec4(max(v.light,texture3D(uLightVolume,BoxCoord).rg),0,1))
                                                 """);
        }else {
            predefinedCodeBuilder.append("""
                                            #undef gl_MultiTexCoord1
                                            #define gl_MultiTexCoord1 (vec4(v.light,0,1))
                                                 """);
        }
        predefinedCodeBuilder.append('\n');
    }

    private void genPredefine(Context key, StringBuilder builder, VertexData appliedTemplate, StringBuilder predefinedCodeBuilder) {
        predefinedCodeBuilder.append('\n');
        genHeadSource(predefinedCodeBuilder, key);
        genCommonSource(predefinedCodeBuilder, key, appliedTemplate);
    }

    private void renameFlwDefine(StringBuilder predefinedCodeBuilder) {
        definePattern = Pattern.compile("(?<=#define\\s)\\w+(?<!_flw)(?=\\s+)");
        Matcher defineMatcher = definePattern.matcher(predefinedCodeBuilder);
        while (defineMatcher.find()){
            String name = defineMatcher.group();
            if (name.contains("_flw")) {
                continue;
            }
            Pattern thisIdentPattern = Pattern.compile(String.format("(?<=\\W)%s(?=\\W)", name));
            Matcher thisIdentMatcher = thisIdentPattern.matcher(predefinedCodeBuilder);
            String newName = name+"_flw";
            while (thisIdentMatcher.find()){
                predefinedCodeBuilder.replace(thisIdentMatcher.start(), thisIdentMatcher.end(),
                                              newName);
                thisIdentMatcher=thisIdentPattern.matcher(predefinedCodeBuilder);
            }
            defineMatcher = definePattern.matcher(predefinedCodeBuilder);
        }
    }
}
