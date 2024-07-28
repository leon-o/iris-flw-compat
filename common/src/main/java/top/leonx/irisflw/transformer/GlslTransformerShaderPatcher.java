package top.leonx.irisflw.transformer;

import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.source.FileResolution;
import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.node.declaration.DeclarationMember;
import io.github.douira.glsl_transformer.ast.node.declaration.FunctionDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.statement.CompoundStatement;
import io.github.douira.glsl_transformer.ast.node.statement.Statement;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.RootSupplier;
import io.github.douira.glsl_transformer.ast.query.index.ExternalDeclarationIndex;
import io.github.douira.glsl_transformer.ast.query.index.IdentifierIndex;
import io.github.douira.glsl_transformer.ast.query.index.SuperclassNodeIndex;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.transform.ASTBuilder;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.JobParameters;
import io.github.douira.glsl_transformer.ast.transform.SingleASTTransformer;
import io.github.douira.glsl_transformer.parser.ParseShape;
import net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class GlslTransformerShaderPatcher extends ShaderPatcherBase {
    private SingleASTTransformer<ContextParameter> transformer;
    private SingleASTTransformer<ContextParameter> flwTransformer;
    public static final AutoHintedMatcher<Expression> glTextureMatrix0 = new AutoHintedMatcher<>(
            "gl_TextureMatrix[0]", ParseShape.EXPRESSION);
    public static final AutoHintedMatcher<Expression> glTextureMatrix1 = new AutoHintedMatcher<>(
            "gl_TextureMatrix[1]", ParseShape.EXPRESSION);
    public static final AutoHintedMatcher<Expression> glTextureMatrix2 = new AutoHintedMatcher<>(
            "gl_TextureMatrix[2]", ParseShape.EXPRESSION);

    private static final ParseShape<GLSLParser.CompoundStatementContext, CompoundStatement> CompoundStatementShape = new ParseShape<>(
            GLSLParser.CompoundStatementContext.class,
            GLSLParser::compoundStatement,
            ASTBuilder::visitCompoundStatement);

    private static final Pattern boxCoordDetector = Pattern.compile("BoxCoord");

    private static final Pattern versionPattern = Pattern.compile("^.*#version\\s+(\\d+)", Pattern.DOTALL);

    public GlslTransformerShaderPatcher(Template<? extends VertexData> template, FileResolution header) {
        super(template, header);
        transformer = new SingleASTTransformer<>() {
            {
                setRootSupplier(RootSupplier.PREFIX_UNORDERED_ED_EXACT);
            }

            @Override
            public TranslationUnit parseTranslationUnit(Root rootInstance, String input) {
                // parse #version directive using an efficient regex before parsing so that the
                // parser can be set to the correct version
                Matcher matcher = versionPattern.matcher(input);
                if (!matcher.find()) {
                    throw new IllegalArgumentException(
                            "No #version directive found in source code! See debugging.md for more information.");
                }
                Version version = Version.fromNumber(Integer.parseInt(matcher.group(1)));
                if (version.number >= 200) {
                    version = Version.GLSL33;
                }
                transformer.getLexer().version = version;

                return super.parseTranslationUnit(rootInstance, input);
            }
        };

        transformer.setTransformation(this::transform);

        flwTransformer = new SingleASTTransformer<>();
        flwTransformer.setRootSupplier(new RootSupplier(SuperclassNodeIndex::withOrdered, IdentifierIndex::withOnlyExact, ExternalDeclarationIndex::withOnlyExactOrdered));
        flwTransformer.getLexer().version = Version.GLSL33;
    }

    private void transform(TranslationUnit tree, Root root, ContextParameter parameter) {
        var beforeDeclarationContent = new StringBuilder();
        var vertexTemplate = template.get(parameter.ctx.file);
        genHeadSource(beforeDeclarationContent, parameter.ctx);
        genCommonSource(beforeDeclarationContent, parameter.ctx, vertexTemplate);

        var predefineContent = JcppProcessor.glslPreprocessSource(beforeDeclarationContent.toString(), List.of(new StringPair("VERTEX_SHADER", "1")));

        var declarationMembers = flwTransformer.parseSeparateTranslationUnit(predefineContent).getChildren();

        tree.injectNodes(ASTInjectionPoint.BEFORE_DECLARATIONS, declarationMembers);


        StringBuilder createVertexBuilder = new StringBuilder();
        createVertexBuilder.append("{\n");
        generateCreateVertex(vertexTemplate, createVertexBuilder);

        //fake tangent
        createVertexBuilder.append("""
                vec3 skewedNormal = v.normal+vec3(0.5,0.5,0.5);
                _flw_tangent = vec4(normalize(skewedNormal - v.normal*dot(skewedNormal,v.normal)).xyz,1.0);
                """);

        createVertexBuilder.append("""
                _flw_patched_vertex_pos = FLWVertex(v);
                """);

        createVertexBuilder.append("\n}");

        var prependMainFuncContent = JcppProcessor.glslPreprocessSource(createVertexBuilder.toString(), List.of(new StringPair("VERTEX_SHADER", "1")));

        var compoundStatement = flwTransformer.parseNodeSeparate(flwTransformer.getRootSupplier(), CompoundStatementShape, prependMainFuncContent);
        compoundStatement.getRoot().rename("i","_flw_instance");

        var prependMainFuncStatements = compoundStatement.getStatements();
        tree.prependMainFunctionBody(prependMainFuncStatements);

        root.replaceReferenceExpressions(transformer, "gl_Vertex", "inverse(gl_ProjectionMatrix*gl_ModelViewMatrix)*_flw_patched_vertex_pos");
        root.replaceReferenceExpressions(transformer, "gl_MultiTexCoord0", "vec4(v.texCoords,0,1)");
        root.replaceReferenceExpressions(transformer, "gl_Normal", "v.normal");
        root.replaceReferenceExpressions(transformer, "gl_Color", "v.color");

        root.replaceExpressionMatches(transformer, new AutoHintedMatcher<>("ftransform()", ParseShape.EXPRESSION), "_flw_patched_vertex_pos");

        root.replaceReferenceExpressions(transformer, "at_tangent", "_flw_tangent");

        //root.replaceExpressionMatches(transformer, glTextureMatrix0, "mat4(1.0)");

        if (boxCoordDetector.matcher(predefineContent).find()) {
            root.replaceReferenceExpressionsReport(transformer, "gl_MultiTexCoord1", "(vec4(max(v.light,texture3D(uLightVolume,BoxCoord).rg)*255.0,0,1))");
        } else {
            root.replaceReferenceExpressionsReport(transformer, "gl_MultiTexCoord1", "(vec4(v.light*255.0,0,1))");
        }

        //root.replaceExpressionMatches(transformer, CommonTransformer.glTextureMatrix1, "1.0");
        //root.replaceExpressionMatches(transformer, CommonTransformer.glTextureMatrix2, "1.0");
    }

    @Override
    public String patch(String irisSource, Context key) {
        return transformer.transform(irisSource, new ContextParameter(key));
    }


    public static class ContextParameter implements JobParameters {
        public Context ctx;

        public ContextParameter(Context ctx) {
            this.ctx = ctx;
        }
    }
}
