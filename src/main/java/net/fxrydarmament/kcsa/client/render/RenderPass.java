package net.fxrydarmament.kcsa.client.render;

public final class RenderPass {

    private RenderPass() {
    }

    public enum Type {
        MAIN,
        HANDS
    }

    private static final ThreadLocal<Type> CURRENT =
            ThreadLocal.withInitial(() -> Type.MAIN);

    public static void push(Type pass) {
        CURRENT.set(pass);
    }

    public static void pop() {
        CURRENT.set(Type.MAIN);
    }

    public static Type current() {
        return CURRENT.get();
    }

    public static boolean isHands() {
        return CURRENT.get() == Type.HANDS;
    }

    public static boolean isMain() {
        return CURRENT.get() == Type.MAIN;
    }
}
