package top.leonx.irisflw.transformer;

import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.lib.material.LightShaders;
import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.ast.data.ChildNodeList;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.node.abstract_node.ASTNode;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.AssignmentExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.MemberAccessExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.statement.CompoundStatement;
import io.github.douira.glsl_transformer.ast.node.statement.Statement;
import io.github.douira.glsl_transformer.ast.node.statement.terminal.ExpressionStatement;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
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
import io.github.douira.glsl_transformer.ast.traversal.ASTBaseVisitor;
import io.github.douira.glsl_transformer.ast.traversal.ASTVisitor;
import io.github.douira.glsl_transformer.parser.ParseShape;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.flywheel.RenderLayerEventStateManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class GlslTransformerVertPatcher {

    final static String FLW_VERTEX_POS_DECL = "flw_vertexPos";
    final static String flw_vertexTexCoord = "flw_vertexTexCoord";
    final static String flw_vertexColor = "flw_vertexColor";
    final static String flw_vertexOverlay = "flw_vertexOverlay";
    final static String flw_vertexLight = "flw_vertexLight";
    final static String flw_vertexNormal = "flw_vertexNormal";

    private final SingleASTTransformer<ContextParameter> transformer;
    private final SingleASTTransformer<ContextParameter> flwTransformer;
    public static final AutoHintedMatcher<Expression> glTextureMatrix0 = new AutoHintedMatcher<>(
            "gl_TextureMatrix[0]", ParseShape.EXPRESSION);
    public static final AutoHintedMatcher<Expression> glTextureMatrix1 = new AutoHintedMatcher<>(
            "gl_TextureMatrix[1]", ParseShape.EXPRESSION);
    public static final AutoHintedMatcher<Expression> glTextureMatrix2 = new AutoHintedMatcher<>(
            "gl_TextureMatrix[2]", ParseShape.EXPRESSION);


    public static final Set<String> toRemoveAttributesSet = Set.of(
            "at_tangent",
            "at_midBlock",
            "mc_Entity",
            "mc_midTexCoord"
    );


    public static final AutoHintedMatcher<Expression> ftransformExpr = new AutoHintedMatcher<>("ftransform()", ParseShape.EXPRESSION);

    public static final ASTVisitor<Boolean> vNormalMemberReassignMatchVisitor = new ReassignMatcherVisitor("v", "normal");

    public static final ASTVisitor<Boolean> vTexCoordsMemberReassignMatchVisitor = new ReassignMatcherVisitor("v", "texCoords");


    private static final ParseShape<GLSLParser.CompoundStatementContext, CompoundStatement> CompoundStatementShape = new ParseShape<>(
            GLSLParser.CompoundStatementContext.class,
            GLSLParser::compoundStatement,
            ASTBuilder::visitCompoundStatement);

    private static final Pattern boxCoordDetector = Pattern.compile("BoxCoord");

    private static final Pattern versionPattern = Pattern.compile("^.*#version\\s+(\\d+)", Pattern.DOTALL);
//    private static final boolean useLightSector = true;

    public GlslTransformerVertPatcher() {
        transformer = new SingleASTTransformer<>() {
            {
                setRootSupplier(RootSupplier.PREFIX_UNORDERED_ED_EXACT);
            }

            @Override
            public TranslationUnit parseTranslationUnit(Root rootInstance, String input) {
                // parse #version directive using an efficient regex before parsing so that the
                // parser can be set to the correct version
                java.util.regex.Matcher matcher = versionPattern.matcher(input);
                if (!matcher.find()) {
                    throw new IllegalArgumentException(
                            "No #version directive found in source code! See debugging.md for more information.");
                }
                var versionNum = Integer.parseInt(matcher.group(1));
                if (versionNum < 330) {
                    versionNum = 330;
                    var ignored = matcher.replaceAll("#version 330");
                    IrisFlw.LOGGER.warn("GLSL version is lower than 330, set to 330");
                }
                transformer.getLexer().version = Version.fromNumber(versionNum);

                return super.parseTranslationUnit(rootInstance, input);
            }
        };

        transformer.setTransformation(this::transform);

        flwTransformer = new SingleASTTransformer<>();
        flwTransformer.setRootSupplier(new RootSupplier(SuperclassNodeIndex::withOrdered, IdentifierIndex::withOnlyExact, ExternalDeclarationIndex::withOnlyExactOrdered));
        flwTransformer.getLexer().version = Version.GLSL33;
    }

    private void transform(TranslationUnit tree, Root root, ContextParameter parameter) {
        var vertexTemplate = parameter.flwVertexTemplate;
        var processedFlwSource = JcppProcessor.glslPreprocessSource(vertexTemplate, List.of(new StringPair("VERTEX_SHADER", "1")));
        parameter.flwTree = flwTransformer.parseSeparateTranslationUnit(processedFlwSource);

        var predefinesStats = ProcessFlywheelPredefine(tree, parameter);
        var prependMainStats = ProcessFlywheelCreateVertex(tree, parameter);

        tree.injectNodes(ASTInjectionPoint.BEFORE_DECLARATIONS, predefinesStats);
        tree.prependMainFunctionBody(prependMainStats);

        root.replaceReferenceExpressions(transformer, "gl_Vertex", String.format("inverse(gl_ProjectionMatrix*gl_ModelViewMatrix)* flw_viewProjection * %s", FLW_VERTEX_POS_DECL));
        root.replaceReferenceExpressions(transformer, "gl_MultiTexCoord0", String.format("vec4(%s,0,1)", flw_vertexTexCoord));
        root.replaceReferenceExpressions(transformer, "gl_Normal", flw_vertexNormal);
        if(parameter.getUseLightLut())
            root.replaceReferenceExpressions(transformer, "gl_Color", String.format("%s * vec4(_flw_ao, _flw_ao, _flw_ao, 1)", flw_vertexColor));
        else
            root.replaceReferenceExpressions(transformer, "gl_Color", flw_vertexColor);

        root.replaceExpressionMatches(transformer, ftransformExpr, String.format("flw_viewProjection * %s", FLW_VERTEX_POS_DECL));

        Map<String, Integer> originalAttrVecDims = new HashMap<>();
        RemoveOriginalAttributes(root, originalAttrVecDims);

        var atTangentDim = originalAttrVecDims.getOrDefault("at_tangent", 4);
        var atMidBlockDim = originalAttrVecDims.getOrDefault("at_midBlock", 4);
        var mcMidTexCoordDim = originalAttrVecDims.getOrDefault("mc_midTexCoord", 4);
        var mcEntityDim = originalAttrVecDims.getOrDefault("mc_Entity", 2);

        if (IrisFlw.isUsingExtendedVertexFormat()) {
            replaceReferenceExpressionsWithCorrectSwizzle(root, transformer, "at_tangent", "_flw_at_tangent", atTangentDim);
            replaceReferenceExpressionsWithCorrectSwizzle(root, transformer, "mc_Entity", "_flw_v_mc_Entity", atTangentDim);
            replaceReferenceExpressionsWithCorrectSwizzle(root, transformer, "mc_midTexCoord", "_flw_mc_midTexCoord", mcMidTexCoordDim);
            replaceReferenceExpressionsWithCorrectSwizzle(root, transformer, "at_midBlock", "_flw_at_midBlock", atMidBlockDim);
        } else {
            root.replaceReferenceExpressions(transformer, "at_tangent", getSwizzleFromDimension("_flw_fake_tangent", atTangentDim));
            root.replaceReferenceExpressions(transformer, "mc_Entity", getZeroFromDimension(mcEntityDim));
            root.replaceReferenceExpressions(transformer, "mc_midTexCoord", getZeroFromDimension(mcMidTexCoordDim));
            root.replaceReferenceExpressions(transformer, "at_midBlock", getZeroFromDimension(atMidBlockDim));
        }

        // TODO do not use RenderLayerEventStateManager.isRenderingShadow()
        if(!RenderLayerEventStateManager.isRenderingShadow())
        {
            root.replaceReferenceExpressionsReport(transformer, "gl_MultiTexCoord1", String.format("(vec4(%s*256.0,0,1))", flw_vertexLight));
        }
    }

    private static void RemoveOriginalAttributes(Root root, Map<String, Integer> attrVectorDims) {
        root.process(root.nodeIndex.getStream(DeclarationExternalDeclaration.class).distinct(),
                node -> {
                    if (node.getDeclaration() instanceof TypeAndInitDeclaration typeAndInitDeclaration) {
                        var foundMember = typeAndInitDeclaration.getMembers().stream().filter(member -> toRemoveAttributesSet.contains(member.getName().getName())).findAny();
                        if (foundMember.isPresent()) {
                            if (typeAndInitDeclaration.getType().getTypeSpecifier() instanceof BuiltinNumericTypeSpecifier numericTypeSpecifier) {
                                var name = foundMember.get().getName().getName();
                                var dimensions = numericTypeSpecifier.type.getDimensions();
                                var dim = dimensions.length > 0 ? dimensions[0] : 1;
                                attrVectorDims.put(name, dim);
                            }
                            node.detachAndDelete();
                        }
                    }
                }
        );
    }

    private ChildNodeList<ExternalDeclaration> ProcessFlywheelPredefine(TranslationUnit tree, ContextParameter parameter) {
        var beforeDeclarationContent = new StringBuilder();

        if (!IrisFlw.isUsingExtendedVertexFormat()) {
            beforeDeclarationContent.append("vec4 _flw_fake_tangent;");
        }

        // TODO do not use RenderLayerEventStateManager.isRenderingShadow()
        if(parameter.getUseLightLut())
        {
            beforeDeclarationContent.append("float _flw_ao;");
        }

        var flwTree = parameter.flwTree;
        var flwSource = parameter.flwVertexTemplate;

        if(!beforeDeclarationContent.isEmpty())
        {
            var additionDeclarations = flwTransformer.parseNodeSeparate(flwTransformer.getRootSupplier(), ParseShape.EXTERNAL_DECLARATION, beforeDeclarationContent.toString());
            flwTree.injectNode(ASTInjectionPoint.BEFORE_DECLARATIONS, additionDeclarations);
        }

        var flwPredefineRoot = flwTree.getRoot();

        //This ensures that the shader code correctly transforms the _flw_at_tangent and the _flw_mc_midTexCoord.
        //We find the assignments of v.normal such as v.normal = m * v.normal and inject a new statement like _flw_at_tangent.xyz = m * _flw_at_tangent.xyz
        flwPredefineRoot.process(flwTree.getRoot().nodeIndex.getStream(ExpressionStatement.class),
                statement -> {
                    if (vNormalMemberReassignMatchVisitor.startVisit(statement)) {
                        var assignStatStr = ASTPrinter.printSimple(statement);
                        assignStatStr = assignStatStr.replace("v.normal", "_flw_at_tangent.xyz");
                        var statements = statement.getAncestor(CompoundStatement.class);
                        var index = statements.getStatements().indexOf(statement);
                        statements.getStatements().add(index + 1, flwTransformer.parseStatement(flwTree.getRoot(), assignStatStr));
                    }
                }
        );
        flwPredefineRoot.process(flwTree.getRoot().nodeIndex.getStream(ExpressionStatement.class),
                statement -> {
                    if (vTexCoordsMemberReassignMatchVisitor.startVisit(statement)) {
                        var assignStatStr = ASTPrinter.printSimple(statement);
                        assignStatStr = assignStatStr.replace("v.texCoords", "_flw_mc_midTexCoord.xy");
                        var statements = statement.getAncestor(CompoundStatement.class);
                        var index = statements.getStatements().indexOf(statement);
                        statements.getStatements().add(index + 1, flwTransformer.parseStatement(flwTree.getRoot(), assignStatStr));
                    }
                }
        );

        var mainFunc = flwTree.getOneMainDefinitionBody().getAncestor(ExternalDeclaration.class);
        return ChildNodeList.collect(flwTree.getChildren().stream().filter(x->x!=mainFunc), flwTree);
    }

    private ChildNodeList<Statement> ProcessFlywheelCreateVertex(TranslationUnit irisTree, ContextParameter context) {
        StringBuilder createVertexBuilder = new StringBuilder();
        createVertexBuilder.append("{\n");

        var flwTree = context.flwTree;
        var flwSource = context.flwVertexTemplate;

        var flwMainBody = flwTree.getOneMainDefinitionBody();

        createVertexBuilder.append(ASTPrinter.printSimple(flwMainBody));

        if (!IrisFlw.isUsingExtendedVertexFormat()) {
            //fake tangent
            createVertexBuilder.append(String.format("""
                    vec3 skewedNormal = %s+vec3(0.5,0.5,0.5);
                    _flw_fake_tangent = vec4(normalize(skewedNormal - %s*dot(skewedNormal, %s)).xyz,1.0);
                    """,flw_vertexNormal, flw_vertexNormal, flw_vertexNormal));
        }

        if(context.getUseLightLut()) {
            createVertexBuilder.append("""
                    FlwLightAo _flw_light;
                    flw_light(flw_vertexPos.xyz, flw_vertexNormal, _flw_light);
                    flw_vertexLight = _flw_light.light;
                    _flw_ao = _flw_light.ao;
                    """);
        }

        createVertexBuilder.append("\n}");


        var compoundStatement = flwTransformer.parseNodeSeparate(flwTransformer.getRootSupplier(), CompoundStatementShape, createVertexBuilder.toString());

        return compoundStatement.getStatements();
    }

    /**
     * Attributes may be defined in different type in different shader, so we need to replace the original attributes with the correct swizzle
     * We define all attributes as vec4. If a shader defines an attribute as vec3, we need to add .xyz when referencing it.
     */
    private void replaceReferenceExpressionsWithCorrectSwizzle(Root root, SingleASTTransformer<ContextParameter> transformer, String name, String expression, int dimension) {
        root.process(root.identifierIndex.getStream(name), identifier -> {
            var parent = identifier.getParent();
            if (!(parent instanceof ReferenceExpression referenceExpression)) {
                return;
            }

            if (referenceExpression.getParent() instanceof MemberAccessExpression) {
                // if the parent is a member access expression, means it's already a swizzle
                parent.replaceByAndDelete(
                        transformer.parseExpression(identifier.getRoot(), expression));
            } else {
                parent.replaceByAndDelete(
                        transformer.parseExpression(identifier.getRoot(), getSwizzleFromDimension(expression, dimension)));
            }
        });
    }

    private String getSwizzleFromDimension(String identifierName, int dimension) {
        return identifierName + switch (dimension) {
            case 1 -> ".x";
            case 2 -> ".xy";
            case 3 -> ".xyz";
            case 4 -> ".xyzw";
            default -> "";
        };
    }

    private String getZeroFromDimension(int dimension) {
        return switch (dimension) {
            case 1 -> "0.0";
            case 2 -> "vec2(0.0)";
            case 3 -> "vec3(0.0)";
            case 4 -> "vec4(0.0)";
            default -> "";
        };
    }

    public String patch(String irisSource, String flwSource, boolean isShadow, boolean isEmbedded, LightShader lightShader, boolean isExtendedVertexFormat){
        return transformer.transform(irisSource, new ContextParameter(flwSource, isShadow, isEmbedded, lightShader, isExtendedVertexFormat));
    }


    public static class ContextParameter implements JobParameters {
//        public Context ctx;

        public boolean isShadow;

        public boolean isEmbedded;

        public LightShader lightShader;

        public boolean isExtendedVertexFormat;

        public String flwVertexTemplate;

        public TranslationUnit flwTree;

        public boolean getUseLightLut()
        {
            return !isShadow && (isEmbedded && lightShader == LightShaders.SMOOTH_WHEN_EMBEDDED
                    || lightShader == LightShaders.FLAT || lightShader == LightShaders.SMOOTH);
        }

        public ContextParameter(String flwVertexTemplate, boolean isShadow, boolean isEmbedded, LightShader lightShader, boolean isExtendedVertexFormat) {
            this.flwVertexTemplate = flwVertexTemplate;
            this.isShadow = isShadow;
            this.isEmbedded = isEmbedded;
            this.lightShader = lightShader;
            this.isExtendedVertexFormat = isExtendedVertexFormat;
        }
    }

    private static class ReassignMatcherVisitor extends ASTBaseVisitor<Boolean> {
        private final String targetName;
        private final String targetMember;

        private boolean isTargetMemberAccess = false;

        public ReassignMatcherVisitor(String targetName, String targetMember) {
            this.targetName = targetName;
            this.targetMember = targetMember;
        }

        private boolean isVNormalMemberAccess(ASTNode node) {
            if (node instanceof MemberAccessExpression memberAccessExpression) {
                return memberAccessExpression.getMember().getName().equals(targetMember)
                        && memberAccessExpression.getOperand() instanceof ReferenceExpression referenceExpression
                        && referenceExpression.getIdentifier().getName().equals(targetName);
            }
            return false;
        }

        @Override
        public Boolean startVisit(ASTNode node) {
            isTargetMemberAccess = false;
            return super.startVisit(node);
        }

        @Override
        public Boolean visitRaw(ASTNode node) {

            if (node instanceof AssignmentExpression assignmentExpression) {
                // If contains v.normal, return true
                var left = assignmentExpression.getLeft();
                if (left instanceof MemberAccessExpression memberAccessExpression) {
                    var leftIsVNormal = isVNormalMemberAccess(memberAccessExpression);

                    if (leftIsVNormal) {
                        Expression right = assignmentExpression.getRight();
                        isTargetMemberAccess = true;
                        return right.accept(this);
                    }
                }
            } else if (isTargetMemberAccess && node instanceof MemberAccessExpression memberAccessExpression
                    && isVNormalMemberAccess(memberAccessExpression)) {
                return true;
            }
            return node.accept(this);
        }

        @Override
        public Boolean defaultResult() {
            return false;
        }

        @Override
        public Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
            return aggregate || nextResult;
        }
    }
}
