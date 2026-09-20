package io.github.elebras1.flecs;

import io.github.elebras1.flecs.callback.CopyMoveCallback;
import io.github.elebras1.flecs.callback.CtorCallback;
import io.github.elebras1.flecs.callback.DtorCallback;
import io.github.elebras1.flecs.callback.IterHookCallback;
import io.github.elebras1.flecs.callback.IterHookContextCallback;
import io.github.elebras1.flecs.callback.ReplaceHookCallback;
import io.github.elebras1.flecs.callback.ReplaceHookContextCallback;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class ComponentHooks<T> {
    private final Linker linker;
    private final World world;
    private final Component<T> component;
    private IterHookCallback<T> onAddCallback;
    private IterHookContextCallback<T> onAddContextCallback;
    private IterHookCallback<T> onSetCallback;
    private IterHookContextCallback<T> onSetContextCallback;
    private IterHookCallback<T> onRemoveCallback;
    private IterHookContextCallback<T> onRemoveContextCallback;
    private ReplaceHookCallback<T> onReplaceCallback;
    private ReplaceHookContextCallback<T> onReplaceContextCallback;
    private CtorCallback<T> ctorCallback;
    private DtorCallback<T> dtorCallback;
    private CopyMoveCallback<T> copyCallback;
    private CopyMoveCallback<T> moveCallback;
    private CopyMoveCallback<T> copyCtorCallback;
    private CopyMoveCallback<T> moveCtorCallback;

    public ComponentHooks(World world, Component<T> component) {
        this.linker = Linker.nativeLinker();
        this.world = world;
        this.component = component;
    }

    public ComponentHooks<T> onAdd(IterHookCallback<T> callback) {
        this.onAddCallback = callback;
        this.onAddContextCallback = null;
        return this;
    }

    public ComponentHooks<T> onAdd(IterHookContextCallback<T> callback) {
        this.onAddContextCallback = callback;
        this.onAddCallback = null;
        return this;
    }

    public ComponentHooks<T> onSet(IterHookCallback<T> callback) {
        this.onSetCallback = callback;
        this.onSetContextCallback = null;
        return this;
    }

    public ComponentHooks<T> onSet(IterHookContextCallback<T> callback) {
        this.onSetContextCallback = callback;
        this.onSetCallback = null;
        return this;
    }

    public ComponentHooks<T> onRemove(IterHookCallback<T> callback) {
        this.onRemoveCallback = callback;
        this.onRemoveContextCallback = null;
        return this;
    }

    public ComponentHooks<T> onRemove(IterHookContextCallback<T> callback) {
        this.onRemoveContextCallback = callback;
        this.onRemoveCallback = null;
        return this;
    }

    public ComponentHooks<T> onReplace(ReplaceHookCallback<T> callback) {
        this.onReplaceCallback = callback;
        this.onReplaceContextCallback = null;
        return this;
    }

    public ComponentHooks<T> onReplace(ReplaceHookContextCallback<T> callback) {
        this.onReplaceContextCallback = callback;
        this.onReplaceCallback = null;
        return this;
    }

    public ComponentHooks<T> ctor(CtorCallback<T> callback) {
        this.ctorCallback = callback;
        return this;
    }

    public ComponentHooks<T> dtor(DtorCallback<T> callback) {
        this.dtorCallback = callback;
        return this;
    }

    public ComponentHooks<T> copy(CopyMoveCallback<T> callback) {
        this.copyCallback = callback;
        return this;
    }

    public ComponentHooks<T> move(CopyMoveCallback<T> callback) {
        this.moveCallback = callback;
        return this;
    }

    public ComponentHooks<T> copyCtor(CopyMoveCallback<T> callback) {
        this.copyCtorCallback = callback;
        return this;
    }

    public ComponentHooks<T> moveCtor(CopyMoveCallback<T> callback) {
        this.moveCtorCallback = callback;
        return this;
    }

    public void install(MemorySegment worldHandle, long componentId) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment hooks = ecs_type_hooks_t.allocate(tempArena);

            if (this.ctorCallback != null) {
                ecs_type_hooks_t.ctor(hooks, this.createCtorStub(this.ctorCallback));
            }
            if (this.dtorCallback != null) {
                ecs_type_hooks_t.dtor(hooks, this.createDtorStub(this.dtorCallback));
            }
            if (this.copyCallback != null) {
                ecs_type_hooks_t.copy(hooks, this.createCopyMoveStub(this.copyCallback, "copy"));
            }
            if (this.moveCallback != null) {
                ecs_type_hooks_t.move(hooks, this.createCopyMoveStub(this.moveCallback, "move"));
            }
            if (this.copyCtorCallback != null) {
                ecs_type_hooks_t.copy_ctor(hooks, this.createCopyMoveStub(this.copyCtorCallback, "copy_ctor"));
            }
            if (this.moveCtorCallback != null) {
                ecs_type_hooks_t.move_ctor(hooks, this.createCopyMoveStub(this.moveCtorCallback, "move_ctor"));
            }
            if (this.onAddContextCallback != null) {
                ecs_type_hooks_t.on_add(hooks, this.createIterContextHookStub(this.onAddContextCallback, "on_add", true));
            } else if (this.onAddCallback != null) {
                ecs_type_hooks_t.on_add(hooks, this.createIterHookStub(this.onAddCallback, "on_add", true));
            }
            if (this.onSetContextCallback != null) {
                ecs_type_hooks_t.on_set(hooks, this.createIterContextHookStub(this.onSetContextCallback, "on_set", true));
            } else if (this.onSetCallback != null) {
                ecs_type_hooks_t.on_set(hooks, this.createIterHookStub(this.onSetCallback, "on_set", true));
            }
            if (this.onRemoveContextCallback != null) {
                ecs_type_hooks_t.on_remove(hooks, this.createIterContextHookStub(this.onRemoveContextCallback, "on_remove", false));
            } else if (this.onRemoveCallback != null) {
                ecs_type_hooks_t.on_remove(hooks, this.createIterHookStub(this.onRemoveCallback, "on_remove", false));
            }
            if (this.onReplaceContextCallback != null) {
                ecs_type_hooks_t.on_replace(hooks, this.createReplaceContextHookStub(this.onReplaceContextCallback, "on_replace"));
            } else if (this.onReplaceCallback != null) {
                ecs_type_hooks_t.on_replace(hooks, this.createReplaceHookStub(this.onReplaceCallback, "on_replace"));
            }

            flecs_h.ecs_set_hooks_id(worldHandle, componentId, hooks);
        }
    }

    private MethodHandle bind(String name, MethodType type, Object... args) {
        try {
            MethodHandle target = MethodHandles.lookup().bind(this, name, type);
            return MethodHandles.insertArguments(target, 0, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to bind component hook", e);
        }
    }

    private MemorySegment upcall(MethodHandle target, FunctionDescriptor descriptor) {
        try {
            return this.linker.upcallStub(target, descriptor, this.world.arena());
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to create component hook upcall stub", e);
        }
    }

    private void report(String hook, Throwable e) {
        this.world.reportHookFailure(hook, e);
    }

    private MemorySegment createIterHookStub(IterHookCallback<T> callback, String hookName, boolean writeBack) {
        MethodHandle target = this.bind("invokeIterHook",
                MethodType.methodType(void.class, IterHookCallback.class, String.class, boolean.class, MemorySegment.class),
                callback, hookName, writeBack);
        return this.upcall(target, FunctionDescriptor.ofVoid(ADDRESS));
    }

    private MemorySegment createIterContextHookStub(IterHookContextCallback<T> callback, String hookName, boolean writeBack) {
        MethodHandle target = this.bind("invokeIterContextHook",
                MethodType.methodType(void.class, IterHookContextCallback.class, String.class, boolean.class, MemorySegment.class),
                callback, hookName, writeBack);
        return this.upcall(target, FunctionDescriptor.ofVoid(ADDRESS));
    }

    private MemorySegment createReplaceHookStub(ReplaceHookCallback<T> callback, String hookName) {
        MethodHandle target = this.bind("invokeReplaceHook",
                MethodType.methodType(void.class, ReplaceHookCallback.class, String.class, MemorySegment.class),
                callback, hookName);
        return this.upcall(target, FunctionDescriptor.ofVoid(ADDRESS));
    }

    private MemorySegment createReplaceContextHookStub(ReplaceHookContextCallback<T> callback, String hookName) {
        MethodHandle target = this.bind("invokeReplaceContextHook",
                MethodType.methodType(void.class, ReplaceHookContextCallback.class, String.class, MemorySegment.class),
                callback, hookName);
        return this.upcall(target, FunctionDescriptor.ofVoid(ADDRESS));
    }

    private MemorySegment createCtorStub(CtorCallback<T> callback) {
        MethodHandle target = this.bind("invokeCtor",
                MethodType.methodType(void.class, CtorCallback.class, String.class, MemorySegment.class, int.class, MemorySegment.class),
                callback, "ctor");
        return this.upcall(target, FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS));
    }

    private MemorySegment createDtorStub(DtorCallback<T> callback) {
        MethodHandle target = this.bind("invokeDtor",
                MethodType.methodType(void.class, DtorCallback.class, String.class, MemorySegment.class, int.class, MemorySegment.class),
                callback, "dtor");
        return this.upcall(target, FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS));
    }

    private MemorySegment createCopyMoveStub(CopyMoveCallback<T> callback, String hookName) {
        MethodHandle target = this.bind("invokeCopyMove",
                MethodType.methodType(void.class, CopyMoveCallback.class, String.class, MemorySegment.class, MemorySegment.class, int.class, MemorySegment.class),
                callback, hookName);
        return this.upcall(target, FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, JAVA_INT, ADDRESS));
    }

    private void invokeIterHook(IterHookCallback<T> callback, String hookName, boolean writeBack, MemorySegment iterSeg) {
        try {
            MemorySegment iter = iterSeg.reinterpret(ecs_iter_t.sizeof());
            int count = ecs_iter_t.count(iter);
            if (count <= 0) {
                return;
            }

            T[] components = this.readFieldComponents(iter, 0, count);

            callback.invoke(components);

            if (writeBack) {
                this.writeFieldComponents(iter, 0, components, count);
            }
        } catch (Throwable e) {
            report(hookName, e);
        }
    }

    private void invokeIterContextHook(IterHookContextCallback<T> callback, String hookName, boolean writeBack, MemorySegment iterSeg) {
        try {
            MemorySegment iter = iterSeg.reinterpret(ecs_iter_t.sizeof());
            int count = ecs_iter_t.count(iter);
            if (count <= 0) {
                return;
            }

            T[] components = this.readFieldComponents(iter, 0, count);

            callback.invoke(this.instantiateIter(iter), components);

            if (writeBack) {
                this.writeFieldComponents(iter, 0, components, count);
            }
        } catch (Throwable e) {
            report(hookName, e);
        }
    }

    private void invokeReplaceHook(ReplaceHookCallback<T> callback, String hookName, MemorySegment iterSeg) {
        try {
            MemorySegment iter = iterSeg.reinterpret(ecs_iter_t.sizeof());
            int count = ecs_iter_t.count(iter);
            if (count <= 0) {
                return;
            }

            T[] oldComponents = this.readFieldComponents(iter, 0, count);
            T[] newComponents = this.readFieldComponents(iter, 1, count);

            callback.invoke(oldComponents, newComponents);

            this.writeFieldComponents(iter, 1, newComponents, count);
        } catch (Throwable e) {
            report(hookName, e);
        }
    }

    private void invokeReplaceContextHook(ReplaceHookContextCallback<T> callback, String hookName, MemorySegment iterSeg) {
        try {
            MemorySegment iter = iterSeg.reinterpret(ecs_iter_t.sizeof());
            int count = ecs_iter_t.count(iter);
            if (count <= 0) {
                return;
            }

            T[] oldComponents = this.readFieldComponents(iter, 0, count);
            T[] newComponents = this.readFieldComponents(iter, 1, count);

            callback.invoke(this.instantiateIter(iter), oldComponents, newComponents);

            this.writeFieldComponents(iter, 1, newComponents, count);
        } catch (Throwable e) {
            report(hookName, e);
        }
    }

    private void invokeCtor(CtorCallback<T> callback, String hookName, MemorySegment segment, int count, MemorySegment typeInfo) {
        try {
            if (segment.address() == 0 || count <= 0) {
                return;
            }

            T[] components = callback.invoke(count);

            if (components != null) {
                this.writeComponentArray(segment, components, count);
            }
        } catch (Throwable e) {
            report(hookName, e);
        }
    }

    private void invokeDtor(DtorCallback<T> callback, String hookName, MemorySegment segment, int count, MemorySegment typeInfo) {
        try {
            if (segment.address() == 0 || count <= 0) {
                return;
            }

            callback.invoke(this.readComponentArray(segment, count));
        } catch (Throwable e) {
            report(hookName, e);
        }
    }

    private void invokeCopyMove(CopyMoveCallback<T> callback, String hookName, MemorySegment dstSeg, MemorySegment srcSeg, int count, MemorySegment typeInfo) {
        try {
            if (srcSeg.address() == 0 || count <= 0) {
                return;
            }

            T[] src = this.readComponentArray(srcSeg, count);
            T[] dst = callback.invoke(src);

            if (dst != null) {
                this.writeComponentArray(dstSeg, dst, count);
            } else {
                this.copyBytes(dstSeg, srcSeg, count);
            }
        } catch (Throwable e) {
            report(hookName, e);
        }
    }

    private Iter instantiateIter(MemorySegment iterSeg) {
        Iter iter = new Iter(iterSeg, this.world);
        iter.setIterSeg(iterSeg);
        return iter;
    }

    private boolean isRowField(MemorySegment iterSeg, int fieldIndex) {
        return (ecs_iter_t.row_fields(iterSeg) & (1 << fieldIndex)) != 0;
    }

    private T[] readFieldComponents(MemorySegment iterSeg, int fieldIndex, int count) {
        T[] array = this.component.createArray(Math.max(count, 0));
        if (count <= 0 || this.component.size() == 0) {
            return array;
        }

        if (this.isRowField(iterSeg, fieldIndex)) {
            for (int i = 0; i < count; i++) {
                MemorySegment rowSeg = flecs_h.ecs_field_at_w_size(iterSeg, this.component.size(), (byte) fieldIndex, i);
                if (rowSeg.address() != 0) {
                    array[i] = this.component.read(rowSeg, 0);
                }
            }
            return array;
        }

        return this.readComponentArray(this.fieldSegment(iterSeg, fieldIndex), count);
    }

    private void writeFieldComponents(MemorySegment iterSeg, int fieldIndex, T[] components, int count) {
        if (count <= 0 || this.component.size() == 0) {
            return;
        }

        if (this.isRowField(iterSeg, fieldIndex)) {
            for (int i = 0; i < count && i < components.length; i++) {
                if (components[i] == null) {
                    continue;
                }
                MemorySegment rowSeg = flecs_h.ecs_field_at_w_size(iterSeg, this.component.size(), (byte) fieldIndex, i);
                if (rowSeg.address() != 0) {
                    this.component.write(rowSeg, 0, components[i]);
                }
            }
            return;
        }

        this.writeComponentArray(this.fieldSegment(iterSeg, fieldIndex), components, count);
    }

    private MemorySegment fieldSegment(MemorySegment iterSeg, int fieldIndex) {
        if (this.component.size() == 0) {
            return MemorySegment.NULL;
        }
        return flecs_h.ecs_field_w_size(iterSeg, this.component.size(), (byte) fieldIndex);
    }

    private T[] readComponentArray(MemorySegment segment, int count) {
        long size = this.component.size();
        T[] array = this.component.createArray(Math.max(count, 0));

        if (segment.address() == 0 || count <= 0 || size == 0) {
            return array;
        }

        MemorySegment buffer = segment.reinterpret(size * count);
        for (int i = 0; i < count; i++) {
            array[i] = this.component.read(buffer, (long) i * size);
        }

        return array;
    }

    private void writeComponentArray(MemorySegment segment, T[] components, int count) {
        long size = this.component.size();
        if (segment.address() == 0 || count <= 0 || size == 0) {
            return;
        }

        MemorySegment buffer = segment.reinterpret(size * count);
        int limit = Math.min(count, components.length);
        for (int i = 0; i < limit; i++) {
            if (components[i] != null) {
                this.component.write(buffer, (long) i * size, components[i]);
            }
        }
    }

    private void copyBytes(MemorySegment dstSeg, MemorySegment srcSeg, int count) {
        long bytes = this.component.size() * count;
        if (bytes == 0 || dstSeg.address() == 0 || srcSeg.address() == 0) {
            return;
        }
        MemorySegment.copy(srcSeg.reinterpret(bytes), 0, dstSeg.reinterpret(bytes), 0, bytes);
    }
}
