package com.sendbird.uikit.log;

enum Tag {
    DEFAULT("SBUIKIT");

    private final String tag;

    private Tag(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return tag;
    }
}
