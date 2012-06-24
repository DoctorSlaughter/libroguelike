package com.nuclearunicorn.negame.client.render;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 23.06.12
 * Time: 18:17
 * To change this template use File | Settings | File Templates.
 */
public class VAVoxel {

    private Vector3f origin = new Vector3f(0,0,0);
    Vector3f vertexVec = new Vector3f(0,0,0);
    Vector3f textureVec = new Vector3f(0,0,0);
    Vector3f normalVec = new Vector3f(0,0,0);

    //int textureTileId = 1;

    public float get_texture_size(){
        return 1.0f / 16;
        //return 1.0f;
    }

    public float get_texture_x(int textureTileId){
        return 1.0f / 16 * (textureTileId - 1);
        //return (1.0f / 16) * 3;
    }

    public float get_texture_y(int textureTileId){
        return 1.0f / 16 * ((int)((textureTileId-1)/16));
        //return 0.0f;
    }

    public void renderIntoVA(VAVoxelRenderer renderer){

        //implement some storage-retrival mechanism for voxel
        int topTileId = 1;
        int sideTileId = 4;

        float tx = get_texture_x(topTileId);
        float ty = get_texture_y(topTileId);
        float ts = get_texture_size();

        float vo = 0.5f;

        float  x = origin.x*vo*2;
        float  y = origin.y*vo*2;
        float  z = origin.z*vo*2;


        //TOP

        normalVec.set(0, 0.5f, 0);
        textureVec.set(tx, ty+ts);
        vertexVec.set(-vo+x, vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx, ty);
        vertexVec.set(-vo+x, vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx+ts, ty);
        vertexVec.set(vo+x, vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx+ts, ty+ts);
        vertexVec.set(vo+x, vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        tx = get_texture_x(sideTileId);
        ty = get_texture_y(sideTileId);


        //FRONT

        normalVec.set(0, 0, 0.5f);
        textureVec.set(tx, ty);
        vertexVec.set(-vo+x, -vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx+ts, ty);
        vertexVec.set(vo+x, -vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx+ts, ty+ts);
        vertexVec.set(vo+x, vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx, ty+ts);
        vertexVec.set(-vo+x, vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        //RIGHT

        normalVec.set(0.5f, 0, 0);
        textureVec.set(tx+ts, ty);
        vertexVec.set(vo+x, -vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx+ts, ty+ts);
        vertexVec.set(vo+x, vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx, ty+ts);
        vertexVec.set(vo+x, vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx, ty);
        vertexVec.set(vo+x, -vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        //BACK

        normalVec.set(0, 0, -0.5f);
        textureVec.set(tx+ts, ty);
        vertexVec.set(-vo+x, -vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx+ts, ty+ts);
        vertexVec.set(-vo+x, vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx, ty+ts);
        vertexVec.set(vo+x, vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx, ty);
        vertexVec.set(vo+x, -vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        //LEFT

        // Left Face

        normalVec.set(-0.5f, 0, 0);
        textureVec.set(tx, ty);
        vertexVec.set(-vo+x, -vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx+ts, ty);
        vertexVec.set(-vo+x, -vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx+ts, ty+ts);
        vertexVec.set(-vo+x, vo+y, vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        textureVec.set(tx, ty+ts);
        vertexVec.set(vo+x, -vo+y, -vo+z);
        renderer.addVoxedData(vertexVec, normalVec, textureVec);

        //We don't render bottom side of the tile.
        //For reference look at the Voxel.java#render method

    }

    public void setOrigin(float x, float y, float z) {
        origin.set(x, y, z);
    }
}
