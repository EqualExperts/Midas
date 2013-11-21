package com.ee.midas.transform;

public enum TransformType {
    EXPANSION {
        @Override
        public String versionFieldName() {
            return "_expansionVersion";
        }
    }, CONTRACTION {
        @Override
        public String versionFieldName() {
            return "_contractionVersion";
        }
    };

    public abstract String versionFieldName();
}
