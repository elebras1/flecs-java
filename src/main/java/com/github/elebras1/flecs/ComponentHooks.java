package com.github.elebras1.flecs;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.foreign.ValueLayout.*;

public class ComponentHooks<T> {
    private final Linker linker;
    private final World world;
    private final Component<T> component;
    private IterHookCallback<T> onAddCallback;
    private IterHookCallback<T> onSetCallback;
    private IterHookCallback<T> onRemoveCallback;
    private ReplaceHookCallback<T> onReplaceCallback;
    private XtorCallback<T> ctorCallback;
    private XtorCallback<T> dtorCallback;
    private CopyMoveCallback<T> copyCallback;
    private CopyMoveCallback<T> moveCallback;
    private CopyMoveCallback<T> copyCtorCallback;
    private CopyMoveCallback<T> moveCtorCallback;

    public ComponentHooks(World world, Component<T> component) {
        this.linker = Linker.nativeLinker();
        this.world = world;
        this.component = component;
    }

    @FunctionalInterface
    public interface IterHookCallback<T> {
        void invoke(MemorySegment it, T[] components);
    }

    @FunctionalInterface
    public interface ReplaceHookCallback<T> {
        void invoke(MemorySegment it, T[] oldComponents, T[] newComponents);
    }

    @FunctionalInterface
    public interface XtorCallback<T> {
        void invoke(T[] components, int count);
    }

    @FunctionalInterface
    public interface CopyMoveCallback<T> {
        void invoke(T[] dst, T[] src, int count);
    }

    public ComponentHooks<T> onAdd(IterHookCallback<T> callback) {
        this.onAddCallback = callback;
        return this;
    }

    public ComponentHooks<T> onSet(IterHookCallback<T> callback) {
        this.onSetCallback = callback;
        return this;
    }

    public ComponentHooks<T> onRemove(IterHookCallback<T> callback) {
        this.onRemoveCallback = callback;
        return this;
    }

    public ComponentHooks<T> onReplace(ReplaceHookCallback<T> callback) {
        this.onReplaceCallback = callback;
        return this;
    }

    public ComponentHooks<T> ctor(XtorCallback<T> callback) {
        this.ctorCallback = callback;
        return this;
    }

    public ComponentHooks<T> dtor(XtorCallback<T> callback) {
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


    private MemorySegment createIterHookStub(IterHookCallback<T> callback) {
        if (callback == null) {
            return MemorySegment.NULL;
        }
        try {
            FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);

            MethodHandle target = MethodHandles.lookup().bind(this, "invokeIterHook", MethodType.methodType(void.class, IterHookCallback.class, MemorySegment.class)).bindTo(callback);

            return this.linker.upcallStub(target, descriptor, this.world.arena());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create iter hook stub", e);
        }
    }

    private MemorySegment createReplaceHookStub(ReplaceHookCallback<T> callback) {
        if (callback == null) {
            return MemorySegment.NULL;
        }
        try {
            FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS);

            MethodHandle target = MethodHandles.lookup().bind(this, "invokeReplaceHook", MethodType.methodType(void.class, ReplaceHookCallback.class, MemorySegment.class)).bindTo(callback);

            return this.linker.upcallStub(target, descriptor, this.world.arena());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create replace hook stub", e);
        }
    }

    private MemorySegment createXtorStub(XtorCallback<T> callback) {
        if (callback == null) {
            return MemorySegment.NULL;
        }
        try {
            FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS);

            MethodHandle target = MethodHandles.lookup().bind(this, "invokeXtorHook", MethodType.methodType(void.class, XtorCallback.class, MemorySegment.class, int.class, MemorySegment.class)).bindTo(callback);

            return this.linker.upcallStub(target, descriptor, this.world.arena());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create xtor hook stub", e);
        }
    }

    private MemorySegment createCopyMoveStub(CopyMoveCallback<T> callback) {
        if (callback == null) {
            return MemorySegment.NULL;
        }
        try {
            FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, JAVA_INT, ADDRESS);

            MethodHandle target = MethodHandles.lookup().bind(this, "invokeCopyMoveHook", MethodType.methodType(void.class, CopyMoveCallback.class, MemorySegment.class, MemorySegment.class, int.class, MemorySegment.class)).bindTo(callback);

            return this.linker.upcallStub(target, descriptor, this.world.arena());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create copy/move hook stub", e);
        }
    }

    private void invokeIterHook(IterHookCallback<T> callback, MemorySegment iterPtr) {
        try {
            MemorySegment iter = iterPtr.reinterpret(ecs_iter_t.sizeof());

            int count = ecs_iter_t.count(iter);
            if (count == 0) {
                return;
            }

            MemorySegment fieldPtr = flecs_h.ecs_field_w_size(iter, component.size(), (byte) 0);

            if (fieldPtr == null || fieldPtr.address() == 0) return;

            T[] components = this.readComponentArray(fieldPtr, count);

            callback.invoke(iter, components);

            this.writeComponentArray(fieldPtr, components, count);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void invokeReplaceHook(ReplaceHookCallback<T> callback, MemorySegment iterPtr) {
        try {
            MemorySegment iter = iterPtr.reinterpret(ecs_iter_t.sizeof());
            int count = ecs_iter_t.count(iter);
            if (count == 0) return;

            MemorySegment oldPtr = flecs_h.ecs_field_w_size(iter, component.size(), (byte) 0);
            MemorySegment newPtr = flecs_h.ecs_field_w_size(iter, component.size(), (byte) 1);

            if (oldPtr == null || newPtr == null) return;

            T[] oldComponents = this.readComponentArray(oldPtr, count);
            T[] newComponents = this.readComponentArray(newPtr, count);

            callback.invoke(iter, oldComponents, newComponents);

            this.writeComponentArray(newPtr, newComponents, count);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void invokeXtorHook(XtorCallback<T> callback, MemorySegment ptr, int count, MemorySegment typeInfo) {
        try {
            if (ptr == null || ptr.address() == 0 || count == 0) {
                return;
            }

            T[] components = this.readComponentArray(ptr, count);

            callback.invoke(components, count);

            if (callback == this.ctorCallback) {
                this.writeComponentArray(ptr, components, count);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void invokeCopyMoveHook(CopyMoveCallback<T> callback, MemorySegment dstPtr, MemorySegment srcPtr, int count, MemorySegment typeInfo) {
        try {
            if (count == 0) {
                return;
            }

            T[] dst = this.readComponentArray(dstPtr, count);
            T[] src = this.readComponentArray(srcPtr, count);

            callback.invoke(dst, src, count);

            this.writeComponentArray(dstPtr, dst, count);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    private T[] readComponentArray(MemorySegment ptr, int count) {
        long size = this.component.size();
        MemorySegment buffer = ptr.reinterpret(size * count);

        T[] array = this.component.createArray(count);

        for (int i = 0; i < count; i++) {
            MemorySegment componentSegment = buffer.asSlice(i * size, size);
            array[i] = this.component.read(componentSegment);
        }

        return array;
    }

    private void writeComponentArray(MemorySegment ptr, T[] components, int count) {
        long size = this.component.size();
        MemorySegment buffer = ptr.reinterpret(size * count);

        for (int i = 0; i < count; i++) {
            if (components[i] != null) {
                MemorySegment componentSegment = buffer.asSlice(i * size, size);
                this.component.write(componentSegment, components[i]);
            }
        }
    }

    public void install(MemorySegment worldHandle, long componentId) {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment hooks = ecs_type_hooks_t.allocate(tempArena);

            if (this.ctorCallback != null) {
                MemorySegment ctorStub = this.createXtorStub(this.ctorCallback);
                ecs_type_hooks_t.ctor(hooks, ctorStub);
            }

            if (this.dtorCallback != null) {
                MemorySegment dtorStub = this.createXtorStub(this.dtorCallback);
                ecs_type_hooks_t.dtor(hooks, dtorStub);
            }

            if (this.copyCallback != null) {
                MemorySegment copyStub = this.createCopyMoveStub(this.copyCallback);
                ecs_type_hooks_t.copy(hooks, copyStub);
            }

            if (this.moveCallback != null) {
                MemorySegment moveStub = this.createCopyMoveStub(this.moveCallback);
                ecs_type_hooks_t.move(hooks, moveStub);
            }

            if (this.copyCtorCallback != null) {
                MemorySegment copyCtorStub = this.createCopyMoveStub(this.copyCtorCallback);
                ecs_type_hooks_t.copy_ctor(hooks, copyCtorStub);
            }

            if (this.moveCtorCallback != null) {
                MemorySegment moveCtorStub = this.createCopyMoveStub(this.moveCtorCallback);
                ecs_type_hooks_t.move_ctor(hooks, moveCtorStub);
            }

            if (this.onAddCallback != null) {
                MemorySegment onAddStub = this.createIterHookStub(this.onAddCallback);
                ecs_type_hooks_t.on_add(hooks, onAddStub);
            }

            if (this.onSetCallback != null) {
                MemorySegment onSetStub = this.createIterHookStub(this.onSetCallback);
                ecs_type_hooks_t.on_set(hooks, onSetStub);
            }

            if (this.onRemoveCallback != null) {
                MemorySegment onRemoveStub = this.createIterHookStub(this.onRemoveCallback);
                ecs_type_hooks_t.on_remove(hooks, onRemoveStub);
            }

            if (this.onReplaceCallback != null) {
                MemorySegment onReplaceStub = this.createReplaceHookStub(this.onReplaceCallback);
                ecs_type_hooks_t.on_replace(hooks, onReplaceStub);
            }

            flecs_h.ecs_set_hooks_id(worldHandle, componentId, hooks);
        }
    }
}