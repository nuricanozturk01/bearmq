package com.bearmq.shared.vhost.dto;

public record VirtualHostInfo(
    String id, String name, String username, String password, String domain, String url) {}
