package com.nuclearunicorn.negame.client.render.utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 23.06.12
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
public class VAVoxelRenderer {

    static final int vertexSize = (3 + 3 + 2) * 4;
    static final int faceSize =  vertexSize * 4;
    static final int voxelSize = faceSize * 6;
    //int maxBufferSize = voxelSize * (int)Math.pow(WorldChunk.CHUNK_SIZE,2) * (int)Math.pow(WorldCluster.CLUSTER_SIZE,2);
    int maxBufferSize = voxelSize * 32 * 32 * 9;

    int vertexCount = 0;

    FloatBuffer vertexBuffer;
    FloatBuffer textureBuffer;
    FloatBuffer normalBuffer;

    public VAVoxelRenderer(){
        //maxBufferSize = 1900000;

        System.out.println("creating VA buffer of size "+ maxBufferSize);
        vertexBuffer = BufferUtils.createFloatBuffer(maxBufferSize);
        textureBuffer = BufferUtils.createFloatBuffer(maxBufferSize);
        normalBuffer = BufferUtils.createFloatBuffer(maxBufferSize);
    }

    private void putVector3f(Vector3f source, FloatBuffer target){
        target.put(source.getX()).put(source.getY()).put(source.getZ());
    }

    public void addVoxedData(Vector3f vertex, Vector3f normal, Vector3f texture){
        putVector3f(vertex, vertexBuffer);
        putVector3f(normal, normalBuffer);
        putVector3f(texture, textureBuffer);

        vertexCount += 3;
    }

    public void clearBuffers(){
        vertexCount = 0;
        vertexBuffer.clear();
        textureBuffer.clear();
        normalBuffer.clear();
    }

    /**
     * Must be called after data is loaded before actual render cycle
     */
    public void flushBuffers(){
        vertexBuffer.flip();
        textureBuffer.flip();
        normalBuffer.flip();

        System.out.println("flushed " + vertexCount + "vertex data");
    }

    public void render(){

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        //int stride = (3+3+2) * 4;   //3 vertex + 2 texture

        glVertexPointer(3, 0, vertexBuffer); //block size = 3, e.g 3 float coord per vertex
        glNormalPointer(0, normalBuffer);
        glTexCoordPointer(3, 0, textureBuffer);

        glDrawArrays(GL_QUADS, 0, /* elements */vertexCount);

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    }
}