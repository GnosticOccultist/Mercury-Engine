package fr.mercury.nucleus.asset.loader;

import java.util.Collection;
import java.util.function.Supplier;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ReadOnlyArray;

public class AssetLoaderDescriptor<A> implements Comparable<AssetLoaderDescriptor<?>> {

    private final Supplier<A> loaderSupplier;

    private final ReadOnlyArray<String> extensions;

    private final int priority;

    public AssetLoaderDescriptor(Supplier<A> loaderSupplier, String... extensions) {
        this(loaderSupplier, 1, extensions);
    }

    public AssetLoaderDescriptor(Supplier<A> loaderSupplier, int priority, String... extensions) {
        this.loaderSupplier = loaderSupplier;
        this.extensions = Array.of(extensions);
        this.priority = priority;
    }

    public AssetLoaderDescriptor(Supplier<A> loaderSupplier, Collection<String> extensions) {
        this(loaderSupplier, 1, extensions);
    }

    public AssetLoaderDescriptor(Supplier<A> loaderSupplier, int priority, Collection<String> extensions) {
        this.loaderSupplier = loaderSupplier;
        this.extensions = Array.of(extensions);
        this.priority = priority;
    }

    public A supply() throws Exception {
        return loaderSupplier.get();
    }

    public ReadOnlyArray<String> getExtensions() {
        return extensions;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(AssetLoaderDescriptor<?> other) {
        return other.getPriority() - getPriority();
    }

    @Override
    public String toString() {
        return extensions + " priority= " + priority;
    }
}
