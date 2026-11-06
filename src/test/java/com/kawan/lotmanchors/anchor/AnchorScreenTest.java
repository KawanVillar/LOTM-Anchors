package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.client.AnchorPanelLayout;
import org.objectweb.asm.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Regression for double blur and GUI-scaled layout, without initializing Minecraft. */
public final class AnchorScreenTest {
    private static int checks;
    private record Call(String owner, String method) {}
    public static void main(String[] args) throws Exception {
        String base = "com/kawan/lotmanchors/client/AnchorScreen";
        List<Call> frame = calls(base, "render");
        check(frame.stream().filter(call -> call.method().equals("renderBackground")).count() == 1,
                "background drawn once per frame");
        check(frame.stream().noneMatch(call -> call.owner().equals("net/minecraft/client/gui/screens/Screen")
                && call.method().equals("render")), "no Screen.render after content: it would blur the panel");
        int background = index(frame, "renderBackground"), content = index(frame, "renderContent");
        int widgets = index(frame, "render"), overlay = index(frame, "renderOverlay");
        check(background >= 0 && background < content && content < widgets && widgets < overlay,
                "background, content, widgets, tooltips in correct order");
        boolean[] finalRender = {false};
        reader(base).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (name.equals("render")) finalRender[0] = (access & Opcodes.ACC_FINAL) != 0;
                return null;
            }
        }, ClassReader.SKIP_DEBUG);
        check(finalRender[0], "subclasses cannot bypass shared render ordering");
        String history = "com/kawan/lotmanchors/client/AnchorHistoryScreen";
        check(calls(history, "backToSources").stream().anyMatch(call -> call.method().equals("setScreen")),
                "history returns directly to saved source screen");
        check(calls(history, "backToSources").stream().noneMatch(call -> call.method().equals("sendToServer")),
                "back navigation does not wait for paused server or request cooldown");
        for (String screen : List.of("AnchorInfoScreen", "AnchorSourcesScreen", "AnchorHistoryScreen")) {
            String name = "com/kawan/lotmanchors/client/" + screen;
            check(reader(name).getSuperName().equals(base), screen + " uses shared renderer");
            check(calls(name, "render").isEmpty(), screen + " does not override frame rendering");
            check(calls(name, "renderContent").stream().noneMatch(call -> call.method().equals("renderBackground")
                    || call.method().equals("renderBlurredBackground") || call.method().equals("render")),
                    screen + " content cannot trigger a second blur pass");
        }
        for (int[] size : new int[][]{{320, 240}, {427, 240}, {480, 270}, {640, 360}, {854, 480}, {1920, 1080}}) {
            for (int maxHeight : new int[]{360, 370}) {
                var layout = AnchorPanelLayout.fit(size[0], size[1], 640, maxHeight);
                check(layout.left() >= 8 && layout.top() >= 8
                        && layout.left() + layout.width() <= size[0] - 8
                        && layout.top() + layout.height() <= size[1] - 8, "panel inside GUI " + size[0] + "x" + size[1]);
                check(layout.width() <= 640 && layout.height() <= maxHeight, "maximum panel size");
                check(124 + layout.sourcePageSize() * 32 <= layout.height() - 47, "rows do not overlap notes or footer");
                check(layout.scrollLimit(500, layout.height() - 104) > 0, "long lists scroll at this size");
                check(layout.scrollLimit(0, layout.height() - 104) == 0, "empty lists do not overscroll");
            }
        }
        System.out.println("AnchorScreens: " + checks + " checks passed");
    }
    private static int index(List<Call> calls, String method) {
        for (int i = 0; i < calls.size(); i++) if (calls.get(i).method().equals(method)) return i;
        return -1;
    }
    private static ClassReader reader(String name) throws Exception {
        try (InputStream input = AnchorScreenTest.class.getResourceAsStream("/" + name + ".class")) {
            if (input == null) throw new IllegalStateException(name);
            return new ClassReader(input);
        }
    }
    private static List<Call> calls(String className, String methodName) throws Exception {
        List<Call> calls = new ArrayList<>();
        reader(className).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (!name.equals(methodName)) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        calls.add(new Call(owner, name));
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG);
        return calls;
    }
    private static void check(boolean condition, String scenario) {
        if (!condition) throw new AssertionError(scenario);
        checks++;
    }
}
