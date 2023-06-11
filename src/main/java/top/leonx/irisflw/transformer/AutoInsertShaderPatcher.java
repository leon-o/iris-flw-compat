package top.leonx.irisflw.transformer;

import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.source.FileResolution;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoInsertShaderPatcher extends ShaderPatcherBase {
    Pattern versionPattern = Pattern.compile("#version\\s+\\d+(\\s+compatibility)?");
    Pattern ftransformAssignPattern = Pattern.compile("ftransform\\(\\)");
    Pattern textureMatrixPattern = Pattern.compile("gl_TextureMatrix\\[\\d\\]");
    Pattern atTangentPattern = Pattern.compile("(?<!(in\\svec4\\s|attribute\\svec4\\s))at_tangent"); // usage of at_tangent attribute
    Pattern boxCoordDetector = Pattern.compile("BoxCoord");
    Pattern definePattern = Pattern.compile("(?<=#define\\s)\\w+(?=\\s+)");

    public String mainFunctionRegex = "void\\s+main\\(\\s*\\)\\s*\\{";
    private final Pattern mainFunctionPattern = Pattern.compile(mainFunctionRegex);

    public AutoInsertShaderPatcher(Template<? extends VertexData> template, FileResolution header) {
        super(template, header);
    }

    @Override
    public String patch(String irisSource, TemplateShaderPatcher.Context key) {
        StringBuilder irisSourceBuilder = new StringBuilder(irisSource);
        VertexData appliedTemplate = template.apply(key.file);
        StringBuilder predefinedCodeBuilder = new StringBuilder();
        genPredefine(key, appliedTemplate, predefinedCodeBuilder);
        renameFlwDefine(predefinedCodeBuilder);
        replaceOriginalDefine(predefinedCodeBuilder);

        StringBuilder createVertexBuilder = new StringBuilder();
        createVertexBuilder.append('\n');
        generateCreateVertex(appliedTemplate,createVertexBuilder);
        createVertexBuilder.append("""
                                           _flw_patched_vertex_pos = FLWVertex(v);
                                           """);
        createVertexBuilder.append("""
                vec3 skewedNormal = v.normal+vec3(0.5,0.5,0.5);
                _flw_tangent = vec4(normalize(skewedNormal - v.normal*dot(skewedNormal,v.normal)).xyz,1.0);
                """);
        //This tangent is not correct. Just make the tangent not equal to (0,0,0).
        createVertexBuilder.append('\n');

        // insert the code after the #version.
        Matcher versionMatcher = versionPattern.matcher(irisSourceBuilder);
        if (versionMatcher.find()) {
            irisSourceBuilder.insert(versionMatcher.end(),predefinedCodeBuilder);
        }


        // insert the code before the main() function
        Matcher mainFuncMatcher = mainFunctionPattern.matcher(irisSourceBuilder);

        if (mainFuncMatcher.find()) {
            irisSourceBuilder.insert(mainFuncMatcher.end(),createVertexBuilder);
        }

        // Replace all ftransform() with _flw_patched_vertex_pos
        Matcher ftransformMatcher = ftransformAssignPattern.matcher(irisSourceBuilder);
        var afterReplaced = ftransformMatcher.replaceAll("_flw_patched_vertex_pos");

        // Replace all at_tangent with _flw_tangent
        Matcher atangentMatcher = atTangentPattern.matcher(afterReplaced);
        afterReplaced = atangentMatcher.replaceAll("_flw_tangent");

        // Replace textureMatrix with "1.0"
        Matcher textureMatrixMatcher = textureMatrixPattern.matcher(afterReplaced);
        afterReplaced = textureMatrixMatcher.replaceAll("1.0");

        return afterReplaced;
    }

    private void replaceOriginalDefine(StringBuilder predefinedCodeBuilder) {

        // Use #define to replace the original vertex data.
        // Jcpp will preprocess the code before compiling.
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


        if (boxCoordDetector.matcher(predefinedCodeBuilder).find()) {
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

    private void genPredefine(Context key, VertexData appliedTemplate, StringBuilder predefinedCodeBuilder) {
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
