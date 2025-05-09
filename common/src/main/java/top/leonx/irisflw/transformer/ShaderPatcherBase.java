/*
package top.leonx.irisflw.transformer;


public abstract class ShaderPatcherBase {
    protected final FileResolution header;
    protected final Template<? extends VertexData> template;

    public ShaderPatcherBase(Template<? extends VertexData> template, FileResolution header) {
        this.header = header;
        this.template = template;
    }

    public abstract String patch(String irisSource, Context key);

    public void generateCreateVertex(VertexData template, StringBuilder createVertexBuilder) {
        if (template instanceof InstancingTemplateData instancingTemplate) {

            createVertexBuilder.append(String.format("""
                                                                  v = FLWCreateVertex();
                                                                  %s i;
                                                                  %s
                                                                  vertex(v, i);
                                                             """, instancingTemplate.instanceName,
                                                     InstancingTemplateData.assignFields(instancingTemplate.instance,
                                                                                         "i.", "a_i_")));

        } else if (template instanceof OneShotTemplateData) {
            createVertexBuilder.append("""
                                                    v = FLWCreateVertex();
                                                    vertex(v);
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

    protected void genCommonSource(StringBuilder structBuilder, Context key, VertexData appliedTemplate) {
        structBuilder.append("#define VERTEX_SHADER\n");

        FileIndexImpl index = new FileIndexImpl();

        //Templates.DIFFUSE_FILE.getFile().generateFinalSource(index, finalSource);
        //headerFile
        header.getFile().generateFinalSource(index, structBuilder);

        key.file.generateFinalSource(index, structBuilder);


        generateInputAttribute(appliedTemplate, key.vertexType, structBuilder);
    }

    protected void genHeadSource(StringBuilder headerBuilder, Context key) {

        //key.ctx.getShaderConstants().writeInto(finalSource);

        headerBuilder.append("""
                                     struct Vertex {
                                     	vec3 pos;
                                     	vec4 color;
                                     	vec2 texCoords;
                                     	vec2 light;
                                     	vec3 normal;
                                     };
                                     
                                     Vertex v; //define outside of main() so all function can use it.
                                     vec4 _flw_patched_vertex_pos;
                                     """);
        headerBuilder.append(key.vertexType.getShaderHeader());
    }
}
*/
