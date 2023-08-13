package org.foxesworld.newgame.engine.material;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jme3.asset.AssetManager;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.texture.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MaterialCreator extends MaterialAbstract {

    private static final String matIndexFile = "materialOptions.json";
    private Map<String, Object> matData;

    public MaterialCreator(AssetManager assetManager) {
        setAssetManager(assetManager);
    }

    @Override
    public Material createMat(String path) {
        System.out.println("Creating material from '" + path + "' dir");
        matData = readMatConfig(MaterialCreator.class.getClassLoader().getResourceAsStream(path + "/" + matIndexFile));
        String matDef = String.valueOf(matData.get("matDef"));
        initMaterial(matDef);
        System.out.println("    - Setting " + matDef + " matDef");

        handleTextures(path, (mapName, textureInstanceMap) -> {
            TextureWrap wrapType = TextureWrap.valueOf((String) textureInstanceMap.get("wrap"));
            Texture thisTexture = getAssetManager().loadTexture(path + "/" + textureInstanceMap.get("texture"));

            switch (wrapType) {
                case REPEAT:
                    thisTexture.setWrap(Texture.WrapMode.Repeat);
                    break;
                case MIRRORED_REPEAT:
                    thisTexture.setWrap(Texture.WrapMode.MirroredRepeat);
                    break;
                case EDGE_CLAMP:
                    thisTexture.setWrap(Texture.WrapMode.EdgeClamp);
                    break;
                case NONE:
                    break;
            }
            getMaterial().setTexture(mapName, thisTexture);
        });

        handleVars((cfgTitle, value) -> {
            VarType inputType = VarType.valueOf(((String) value.get("type")).toUpperCase());
            switch (inputType) {
                case FLOAT:
                    setMaterialFloat(cfgTitle, (Float) value.get("value"));
                    break;
                case BOOLEAN:
                    setMaterialBoolean(cfgTitle, (Boolean) value.get("value"));
                    break;
                case COLOR:
                    setMaterialColor(cfgTitle, parseColor((String) value.get("value")));
                    break;
            }
        });

        return getMaterial();
    }

    private void handleTextures(String path, BiConsumer<String, Map<String, Object>> consumer) {
        LinkedHashMap<String, Map<String, Object>> texturesMap = (LinkedHashMap<String, Map<String, Object>>) matData.get("textures");
        texturesMap.forEach((mapName, textureInstanceMap) -> {
            TextureWrap wrapType = TextureWrap.valueOf((String) textureInstanceMap.get("wrap"));

            Texture thisTexture = getAssetManager().loadTexture(path + "/" + textureInstanceMap.get("texture"));

            switch (wrapType) {
                case REPEAT:
                    thisTexture.setWrap(Texture.WrapMode.Repeat);
                    break;
                case MIRRORED_REPEAT:
                    thisTexture.setWrap(Texture.WrapMode.MirroredRepeat);
                    break;
                case EDGE_CLAMP:
                    thisTexture.setWrap(Texture.WrapMode.EdgeClamp);
                    break;
                case NONE:
                    break;
            }
            consumer.accept(mapName, textureInstanceMap);
        });
    }

    private void handleVars(BiConsumer<String, Map<String, Object>> consumer) {
        LinkedHashMap<String, Map<String, Object>> varsMap = (LinkedHashMap<String, Map<String, Object>>) matData.get("vars");
        varsMap.forEach((cfgTitle, value) -> {
            VarType inputType = VarType.valueOf(((String) value.get("type")).toUpperCase());
            switch (inputType) {
                case FLOAT:
                    setMaterialFloat(cfgTitle, (Float) value.get("value"));
                    break;
                case BOOLEAN:
                    setMaterialBoolean(cfgTitle, (Boolean) value.get("value"));
                    break;
                case COLOR:
                    setMaterialColor(cfgTitle, parseColor((String) value.get("value")));
                    break;
            }
            consumer.accept(cfgTitle, value);
        });
    }

    private ColorRGBA parseColor(String colorStr) {
        String[] rgba = colorStr.split(",");
        float r = Float.parseFloat(rgba[0]);
        float g = Float.parseFloat(rgba[1]);
        float b = Float.parseFloat(rgba[2]);
        float a = Float.parseFloat(rgba[3]);
        return new ColorRGBA(r, g, b, a);
    }

    private enum VarType {
        FLOAT,
        BOOLEAN,
        COLOR
    }

    private Vector2f parseVector2f(String input) {
        String[] parts = input.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid vector2f format: " + input);
        }

        float x = Float.parseFloat(parts[0].trim());
        float y = Float.parseFloat(parts[1].trim());

        return new Vector2f(x, y);
    }

    private Map<String, Object> readMatConfig(InputStream is) {
        Map<String, Object> map = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
            };
            map = mapper.readValue(is, typeRef);
        } catch (IOException ignored) {
        }
        return map;
    }

    @FunctionalInterface
    private interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}
