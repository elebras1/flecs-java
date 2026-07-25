package io.github.elebras1.flecs.util;

import io.github.elebras1.flecs.flecs_h;

public final class Flecs {

    private Flecs() {
        // Prevent instantiation
    }

    public static final long Flecs = flecs_h.EcsFlecs();
    public static final long FlecsCore = flecs_h.EcsFlecsCore();
    public static final long World = flecs_h.EcsWorld();

    public static final long Wildcard = flecs_h.EcsWildcard();
    public static final long Any = flecs_h.EcsAny();
    public static final long This = flecs_h.EcsThis();
    public static final long Variable = flecs_h.EcsVariable();
    public static final long Transitive = flecs_h.EcsTransitive();
    public static final long Reflexive = flecs_h.EcsReflexive();
    public static final long Final = flecs_h.EcsFinal();
    public static final long Inheritable = flecs_h.EcsInheritable();
    public static final long Exclusive = flecs_h.EcsExclusive();
    public static final long Acyclic = flecs_h.EcsAcyclic();
    public static final long Traversable = flecs_h.EcsTraversable();
    public static final long Symmetric = flecs_h.EcsSymmetric();
    public static final long With = flecs_h.EcsWith();
    public static final long OneOf = flecs_h.EcsOneOf();
    public static final long Cascade = flecs_h.EcsCascade();
    public static final long Trait = flecs_h.EcsTrait();
    public static final long Relationship = flecs_h.EcsRelationship();
    public static final long Target = flecs_h.EcsTarget();
    public static final long PairIsTag = flecs_h.EcsPairIsTag();

    public static final long CanToggle = flecs_h.EcsCanToggle();
    public static final long DontFragment = flecs_h.EcsDontFragment();
    public static final long Sparse = flecs_h.EcsSparse();

    public static final long OnInstantiate = flecs_h.EcsOnInstantiate();
    public static final long Override = flecs_h.EcsOverride();
    public static final long Inherit = flecs_h.EcsInherit();
    public static final long DontInherit = flecs_h.EcsDontInherit();

    public static final long ChildOf = flecs_h.EcsChildOf();
    public static final long IsA = flecs_h.EcsIsA();
    public static final long DependsOn = flecs_h.EcsDependsOn();
    public static final long SlotOf = flecs_h.EcsSlotOf();
    public static final long OrderedChildren = flecs_h.EcsOrderedChildren();
    public static final long Module = flecs_h.EcsModule();

    public static final long Prefab = flecs_h.EcsPrefab();
    public static final long Disabled = flecs_h.EcsDisabled();
    public static final long NotQueryable = flecs_h.EcsNotQueryable();
    public static final long Singleton = flecs_h.EcsSingleton();

    public static final long OnDelete = flecs_h.EcsOnDelete();
    public static final long OnDeleteTarget = flecs_h.EcsOnDeleteTarget();
    public static final long Remove = flecs_h.EcsRemove();
    public static final long Delete = flecs_h.EcsDelete();
    public static final long Panic = flecs_h.EcsPanic();

    public static final long Name = flecs_h.EcsName();
    public static final long Symbol = flecs_h.EcsSymbol();
    public static final long Alias = flecs_h.EcsAlias();

    public static final long OnAdd = flecs_h.EcsOnAdd();
    public static final long OnRemove = flecs_h.EcsOnRemove();
    public static final long OnSet = flecs_h.EcsOnSet();
    public static final long Monitor = flecs_h.EcsMonitor();
    public static final long OnTableCreate = flecs_h.EcsOnTableCreate();
    public static final long OnTableDelete = flecs_h.EcsOnTableDelete();

    public static final long Phase = flecs_h.EcsPhase();
    public static final long OnStart = flecs_h.EcsOnStart();
    public static final long PreFrame = flecs_h.EcsPreFrame();
    public static final long OnLoad = flecs_h.EcsOnLoad();
    public static final long PostLoad = flecs_h.EcsPostLoad();
    public static final long PreUpdate = flecs_h.EcsPreUpdate();
    public static final long OnUpdate = flecs_h.EcsOnUpdate();
    public static final long OnValidate = flecs_h.EcsOnValidate();
    public static final long PostUpdate = flecs_h.EcsPostUpdate();
    public static final long PreStore = flecs_h.EcsPreStore();
    public static final long OnStore = flecs_h.EcsOnStore();
    public static final long PostFrame = flecs_h.EcsPostFrame();

    public static final int QueryCacheAuto = flecs_h.EcsQueryCacheAuto();
    public static final int QueryMatchEmptyTables = flecs_h.EcsQueryMatchEmptyTables();
    public static final int In = flecs_h.EcsIn();
    public static final int Out = flecs_h.EcsOut();
    public static final int InOut = flecs_h.EcsInOut();
    public static final int And = flecs_h.EcsAnd();
    public static final int Or = flecs_h.EcsOr();
    public static final int Not = flecs_h.EcsNot();
    public static final int Optional = flecs_h.EcsOptional();
    public static final int AndFrom = flecs_h.EcsAndFrom();
    public static final int OrFrom = flecs_h.EcsOrFrom();
    public static final int NotFrom = flecs_h.EcsNotFrom();

    public static final int ObserverYieldOnCreate = flecs_h.EcsObserverYieldOnCreate();
    public static final int ObserverYieldOnDelete = flecs_h.EcsObserverYieldOnDelete();

    public static final int HttpGet = flecs_h.EcsHttpGet();
    public static final int HttpPost = flecs_h.EcsHttpPost();
    public static final int HttpPut = flecs_h.EcsHttpPut();
    public static final int HttpDelete = flecs_h.EcsHttpDelete();
    public static final int HttpOptions = flecs_h.EcsHttpOptions();
    public static final int HttpMethodUnsupported = flecs_h.EcsHttpMethodUnsupported();

    public static final long Period1s = flecs_h.EcsPeriod1s();
    public static final long Period1m = flecs_h.EcsPeriod1m();
    public static final long Period1h = flecs_h.EcsPeriod1h();
    public static final long Period1d = flecs_h.EcsPeriod1d();
}