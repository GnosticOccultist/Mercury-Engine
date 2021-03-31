package fr.mercury.nucleus.asset.loader.data;

import java.io.InputStream;

public abstract class AssetData {
    
    public abstract InputStream openStream();

    public abstract String getName();
}
