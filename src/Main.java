import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Main {

    public static void main(String[] args) {
        float[] vertices = {
                0.5f,  0.5f, 0.0f,  // top right
                0.5f, -0.5f, 0.0f,  // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                -0.5f,  0.5f, 0.0f   // top left
        };
        int[] indices = {
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
        };

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Create a windowed mode window and its OpenGL context
        long window = glfwCreateWindow(800, 600, "My Game", 0, 0);
        if (window == 0) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        glfwShowWindow(window); // Ensure the window is visible
        GL.createCapabilities();

        // Set the viewport
        glViewport(0, 0, 800, 600);

        // Create a VAO
        int VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        // Create a VBO
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();

        int VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create an EBO
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(indices).flip();

        int EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Define the vertex attribute pointers
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Unbind the VAO (not strictly necessary here)
        glBindVertexArray(0);

        // Free the allocated memory
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);

        // Compile the shaders
        String vertexShaderSource = FileLoader.readFile("resources/basic.vert");
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        // Check for vertex shader compilation errors
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Vertex Shader Compilation Failed: " + glGetShaderInfoLog(vertexShader));
        }

        String fragmentShaderSource = FileLoader.readFile("resources/basic.frag");
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        // Check for fragment shader compilation errors
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Fragment Shader Compilation Failed: " + glGetShaderInfoLog(fragmentShader));
        }

        // Link the shaders into a program
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        // Check for linking errors
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Shader Program Linking Failed: " + glGetProgramInfoLog(shaderProgram));
        }

        // Delete the shaders as they are no longer needed
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Main loop
        while (!glfwWindowShouldClose(window)) {
            // Poll for window events
            glfwPollEvents();

            // Clear the screen
            glClear(GL_COLOR_BUFFER_BIT);

            // Render the rectangle
            glUseProgram(shaderProgram);
            glBindVertexArray(VAO);
            glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);

            // Swap the buffers
            glfwSwapBuffers(window);
        }

        // Clean up
        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
        glDeleteProgram(shaderProgram);

        // Terminate GLFW
        glfwTerminate();
    }
}