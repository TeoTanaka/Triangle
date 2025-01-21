import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Main {



    public static void main(String[] args) {

        // Setup GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        long window = glfwCreateWindow(800, 600, "Perspective Projection with Camera", 0, 0);
        if (window == 0) {
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        GL.createCapabilities();

        // Viewport and depth
        glViewport(0, 0, 800, 600);
        glEnable(GL_DEPTH_TEST);

        // Camera setup
        Camera camera = new Camera(new Vector3f(0.0f, 0.0f, 3.0f), new Vector3f(0.0f, 1.0f, 0.0f), -90.0f, 0.0f);

        float[] vertices = {
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 1.0f
        };

        int[] indices = {
                0, 1, 2, 2, 3, 0, // Front
                4, 5, 6, 6, 7, 4, // Back
                0, 1, 5, 5, 4, 0, // Bottom
                2, 3, 7, 7, 6, 2, // Top
                0, 3, 7, 7, 4, 0, // Left
                1, 2, 6, 6, 5, 1  // Right
        };

        // VAO, VBO, EBO setup
        int VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        int VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(indices).flip();

        int EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);

        // Shader setup
        int shaderProgram = setupShaders();
        glUseProgram(shaderProgram);

        int viewLoc = glGetUniformLocation(shaderProgram, "view");
        int projLoc = glGetUniformLocation(shaderProgram, "projection");

        Matrix4f proj = new Matrix4f().perspective((float) Math.toRadians(45f), 800f / 600f, 0.1f, 100f);
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

        // Mouse and keyboard input handling
        float[] lastMousePos = {400, 300};
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            float xOffset = (float) xpos - lastMousePos[0];
            float yOffset = lastMousePos[1] - (float) ypos;
            lastMousePos[0] = (float) xpos;
            lastMousePos[1] = (float) ypos;
            camera.processMouse(xOffset, yOffset);
        });

        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();

            // WASD movement
            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) camera.processKeyboard(GLFW_KEY_W);
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) camera.processKeyboard(GLFW_KEY_S);
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) camera.processKeyboard(GLFW_KEY_A);
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) camera.processKeyboard(GLFW_KEY_D);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUniformMatrix4fv(viewLoc, false, camera.getViewMatrix().get(matrixBuffer));
            glUniformMatrix4fv(projLoc, false, proj.get(matrixBuffer));

            glBindVertexArray(VAO);
            glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);

            glfwSwapBuffers(window);
        }

        glfwTerminate();
    }

    private static int setupShaders() {
        String vertexShaderSource = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            uniform mat4 view;
            uniform mat4 projection;
            void main() {
                gl_Position = projection * view * vec4(aPos, 1.0);
            }
        """;

        String fragmentShaderSource = """
            #version 330 core
            out vec4 FragColor;
            void main() {
                FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
            }
        """;

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return shaderProgram;
    }
}