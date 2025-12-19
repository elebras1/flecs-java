package com.github.elebras1.flecs.util;

import com.github.elebras1.flecs.flecs_h;

public final class FlecsConstants {

    private FlecsConstants() {
        // Prevent instantiation
    }

    public static final long EcsFlecs = flecs_h.EcsFlecs();
    public static final long EcsFlecsCore = flecs_h.EcsFlecsCore();
    public static final long EcsWorld = flecs_h.EcsWorld();

    public static final long EcsPrivate = flecs_h.EcsPrivate();
    public static final long EcsPrefab = flecs_h.EcsPrefab();
    public static final long EcsSlotOf = flecs_h.EcsSlotOf();
    public static final long EcsDisabled = flecs_h.EcsDisabled();
    public static final long EcsNotQueryable = flecs_h.EcsNotQueryable();

    public static final long EcsChildOf = flecs_h.EcsChildOf();
    public static final long EcsIsA = flecs_h.EcsIsA();
    public static final long EcsDependsOn = flecs_h.EcsDependsOn();
    public static final long EcsOnDelete = flecs_h.EcsOnDelete();
    public static final long EcsOnDeleteTarget = flecs_h.EcsOnDeleteTarget();
    public static final long EcsCascade = flecs_h.EcsCascade();

    public static final long EcsRemove = flecs_h.EcsRemove();
    public static final long EcsDelete = flecs_h.EcsDelete();
    public static final long EcsPanic = flecs_h.EcsPanic();

    public static final long EcsName = flecs_h.EcsName();
    public static final long EcsSymbol = flecs_h.EcsSymbol();
    public static final long EcsAlias = flecs_h.EcsAlias();

    public static final long EcsOnAdd = flecs_h.EcsOnAdd();
    public static final long EcsOnRemove = flecs_h.EcsOnRemove();
    public static final long EcsOnSet = flecs_h.EcsOnSet();
    public static final long EcsMonitor = flecs_h.EcsMonitor();
    public static final long EcsOnTableCreate = flecs_h.EcsOnTableCreate();
    public static final long EcsOnTableDelete = flecs_h.EcsOnTableDelete();

    public static final long EcsPhase = flecs_h.EcsPhase();
    public static final long EcsOnStart = flecs_h.EcsOnStart();
    public static final long EcsPreFrame = flecs_h.EcsPreFrame();
    public static final long EcsOnLoad = flecs_h.EcsOnLoad();
    public static final long EcsPostLoad = flecs_h.EcsPostLoad();
    public static final long EcsPreUpdate = flecs_h.EcsPreUpdate();
    public static final long EcsOnUpdate = flecs_h.EcsOnUpdate();
    public static final long EcsOnValidate = flecs_h.EcsOnValidate();
    public static final long EcsPostUpdate = flecs_h.EcsPostUpdate();
    public static final long EcsPreStore = flecs_h.EcsPreStore();
    public static final long EcsOnStore = flecs_h.EcsOnStore();
    public static final long EcsPostFrame = flecs_h.EcsPostFrame();

    public static final long EcsWildcard = flecs_h.EcsWildcard();
    public static final long EcsAny = flecs_h.EcsAny();
    public static final long EcsThis = flecs_h.EcsThis();
    public static final long EcsVariable = flecs_h.EcsVariable();
    public static final long EcsTransitive = flecs_h.EcsTransitive();
    public static final long EcsReflexive = flecs_h.EcsReflexive();
    public static final long EcsFinal = flecs_h.EcsFinal();
    public static final long EcsDontInherit = flecs_h.EcsDontInherit();
    public static final long EcsExclusive = flecs_h.EcsExclusive();
    public static final long EcsAcyclic = flecs_h.EcsAcyclic();
    public static final long EcsTraversable = flecs_h.EcsTraversable();
    public static final long EcsSymmetric = flecs_h.EcsSymmetric();
    public static final long EcsWith = flecs_h.EcsWith();
    public static final long EcsOneOf = flecs_h.EcsOneOf();
    public static final long EcsCanToggle = flecs_h.EcsCanToggle();

    public static final int EcsQueryCacheAuto = flecs_h.EcsQueryCacheAuto();
    public static final int EcsQueryMatchEmptyTables = flecs_h.EcsQueryMatchEmptyTables();
    public static final int EcsIn = flecs_h.EcsIn();
    public static final int EcsOut = flecs_h.EcsOut();
    public static final int EcsInOut = flecs_h.EcsInOut();
    public static final int EcsAnd = flecs_h.EcsAnd();
    public static final int EcsOr = flecs_h.EcsOr();
    public static final int EcsNot = flecs_h.EcsNot();
    public static final int EcsOptional = flecs_h.EcsOptional();
    public static final int EcsAndFrom = flecs_h.EcsAndFrom();
    public static final int EcsOrFrom = flecs_h.EcsOrFrom();
    public static final int EcsNotFrom = flecs_h.EcsNotFrom();

    public static final int EcsObserverYieldOnCreate = flecs_h.EcsObserverYieldOnCreate();
    public static final int EcsObserverYieldOnDelete = flecs_h.EcsObserverYieldOnDelete();

    public static final int EcsHttpGet = flecs_h.EcsHttpGet();
    public static final int EcsHttpPost = flecs_h.EcsHttpPost();
    public static final int EcsHttpPut = flecs_h.EcsHttpPut();
    public static final int EcsHttpDelete = flecs_h.EcsHttpDelete();
    public static final int EcsHttpOptions = flecs_h.EcsHttpOptions();
    public static final int EcsHttpMethodUnsupported = flecs_h.EcsHttpMethodUnsupported();

    public static final long EcsPeriod1s = flecs_h.EcsPeriod1s();
    public static final long EcsPeriod1m = flecs_h.EcsPeriod1m();
    public static final long EcsPeriod1h = flecs_h.EcsPeriod1h();
    public static final long EcsPeriod1d = flecs_h.EcsPeriod1d();

}

