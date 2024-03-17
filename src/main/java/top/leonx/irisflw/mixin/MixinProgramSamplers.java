package top.leonx.irisflw.mixin;

import net.irisshaders.iris.gl.program.ProgramSamplers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import java.util.Set;
import java.util.HashSet;

@Mixin(value = ProgramSamplers.class,remap = false)
public class MixinProgramSamplers {
    @ModifyVariable(method = "builder", at = @At("LOAD"), argsOnly = true)
    private static Set<Integer> modifyReservedTextureUnits(Set<Integer> var1) {
        // Add the lighting volume texture from Create to the reserved textures.
        //
        // The set from Iris is immutable, so duplicate before modifying.
        Set<Integer> reservedTextureUnits = new HashSet<Integer>();
        reservedTextureUnits.addAll(var1);
        reservedTextureUnits.add(4);
        return reservedTextureUnits;
    }
}
