import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class Camera {
    Vector3f position;
    Vector3f front;
    Vector3f up;
    Vector3f right;
    Vector3f worldUp;

    float yaw;
    float pitch;
    float speed;
    float sensitivity;

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        this.position = position;
        this.front = new Vector3f(0.0f, 0.0f, -1.0f);
        this.worldUp = up;
        this.yaw = yaw;
        this.pitch = pitch;
        this.speed = 0.005f;
        this.sensitivity = 0.1f;
        updateCameraVectors();
    }

    private void updateCameraVectors() {
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.normalize();

        right = front.cross(worldUp, new Vector3f()).normalize();
        up = right.cross(front, new Vector3f()).normalize();
    }

    public Matrix4f getViewMatrix() {
        Vector3f center = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, center, up);
    }

    public void processKeyboard(int direction) {
        if (direction == GLFW_KEY_W)
            position.add(new Vector3f(front).mul(speed));
        if (direction == GLFW_KEY_S)
            position.sub(new Vector3f(front).mul(speed));
        if (direction == GLFW_KEY_A)
            position.sub(new Vector3f(right).mul(speed));
        if (direction == GLFW_KEY_D)
            position.add(new Vector3f(right).mul(speed));
    }

    public void processMouse(float xOffset, float yOffset) {
        xOffset *= sensitivity;
        yOffset *= sensitivity;

        yaw += xOffset;
        pitch += yOffset;

        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        updateCameraVectors();
    }
}
