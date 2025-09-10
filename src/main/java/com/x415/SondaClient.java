package com.x415;

import java.util.List;

import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

@Client("https://zn4.m2mcontrol.com.br")
public interface SondaClient {
    @Get("/api/forecast/lines/load/allLines/281")
    @Cacheable("linhas-cache")
    List<Linha> getTodasLinhas();
}
