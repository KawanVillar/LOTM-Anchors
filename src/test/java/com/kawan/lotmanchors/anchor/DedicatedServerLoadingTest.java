package com.kawan.lotmanchors.anchor;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.objectweb.asm.*;

/** Resolves packet handlers while all addon/Minecraft client classes are unavailable. */
public final class DedicatedServerLoadingTest {
    private static int checks;

    private static final class ServerLoader extends ClassLoader {
        ServerLoader() { super(DedicatedServerLoadingTest.class.getClassLoader()); }
        @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                if (name.startsWith("net.minecraft.client.") || name.startsWith("com.kawan.lotmanchors.client.")) {
                    throw new ClassNotFoundException("Client class forbidden on server: " + name);
                }
                if (!name.startsWith("com.kawan.lotmanchors.")) return super.loadClass(name, resolve);
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try (InputStream input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        if (input == null) throw new ClassNotFoundException(name);
                        byte[] bytes = input.readAllBytes();
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    } catch (java.io.IOException e) { throw new ClassNotFoundException(name, e); }
                }
                if (resolve) resolveClass(loaded);
                return loaded;
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        var loader = new ServerLoader();
        boolean blocked = false;
        try { loader.loadClass("com.kawan.lotmanchors.client.AnchorNetworkClient"); }
        catch (ClassNotFoundException expected) { blocked = true; }
        check(blocked, "test loader actually excludes the old registration target");
        Class<?> bridge = loader.loadClass("com.kawan.lotmanchors.network.AnchorClientHandlers");
        for (String factory : new String[]{"overview", "sources", "history"}) {
            // Resolves the lambda bootstrap, which previously resolved a client method directly.
            Object handler = MethodHandles.publicLookup().findStatic(bridge, factory,
                    MethodType.methodType(IPayloadHandler.class)).invoke();
            check(handler instanceof IPayloadHandler<?>, factory + " handler constructs without client classes");
        }
        for (String name : new String[]{"AnchorNetwork", "AnchorSourcesNetwork", "AnchorHistoryNetwork"}) {
            String resource = "com/kawan/lotmanchors/network/" + name + ".class";
            try (InputStream input = DedicatedServerLoadingTest.class.getClassLoader().getResourceAsStream(resource)) {
                if (input == null) throw new AssertionError(resource);
                new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override public MethodVisitor visitMethod(int access, String method, String desc, String sig, String[] exceptions) {
                        if (!method.equals("register")) return null;
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override public void visitInvokeDynamicInsn(String n, String d, Handle bootstrap, Object... arguments) {
                                for (Object argument : arguments) if (argument instanceof Handle target) {
                                    check(!target.getOwner().startsWith("com/kawan/lotmanchors/client/"),
                                            name + " registration must not resolve client method references");
                                }
                            }
                            @Override public void visitMethodInsn(int opcode, String owner, String n, String d, boolean itf) {
                                check(!owner.startsWith("com/kawan/lotmanchors/client/"),
                                        name + " registration must not invoke client code");
                            }
                        };
                    }
                }, ClassReader.SKIP_DEBUG);
            }
        }
        System.out.println("DedicatedServerLoading: " + checks + " checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
