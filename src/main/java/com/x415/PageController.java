package com.x415;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x415.Linha.Empresa;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;

@Controller
public class PageController {
    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    private SondaClient sondaClient;

    public PageController(SondaClient sondaClient) {
        this.sondaClient = sondaClient;
    }

    @Get("/")
    @Produces(MediaType.TEXT_HTML)
    @View("layout")
    public HttpResponse<Map<String, Object>> index(HttpRequest<?> request) {
        Map<String, Object> model = new HashMap<>();
        model.put("uri", "");
        model.put("main", "inicio");
        return HttpResponse.ok(model);
    }

    String getLinhasInfo() {
        List<Linha> todasLinhas = sondaClient.getTodasLinhas();
        Collections.sort(todasLinhas, Comparator.comparingInt(l -> Integer.parseInt(l.getNumero())));

        StringBuilder sb = new StringBuilder();
        for (Linha linha : todasLinhas) {
            String nomeEmpresaTemp = linha.getEmpresas().get(0).getNome().toLowerCase();
            String nomeLinhaTemp = linha.getName().toLowerCase();
            String numeroLinha = linha.getNumero();

            if (nomeEmpresaTemp.contains("sindionibus")
                    || nomeEmpresaTemp.contains("top bus")
                    || nomeEmpresaTemp.contains("topbus")
                    || nomeLinhaTemp.contains("teste")
                    || numeroLinha.contains("0000")) {
                continue;
            }

            sb.append("<b>").append(linha.getName())
              .append("</b>").append("\nEmpresas operando:\n");

            for (Empresa empresa : linha.getEmpresas()) {
                if (empresa.getNome().toLowerCase().contains("sonda")) {
                    continue;
                }

                sb.append("- ").append(empresa.getNome()).append('\n');
            }

            sb.append('\n');
        }

        return sb.toString();
    }

    @Get("/linhas")
    @Produces(MediaType.TEXT_HTML)
    @View("layout")
    @ExecuteOn(TaskExecutors.BLOCKING)
    public HttpResponse<Map<String, Object>> linhas(HttpRequest<?> request) {
        Map<String, Object> model = new HashMap<>();
        model.put("uri", request.getPath());
        model.put("main", "linhas");

        try {
            String linhasInfo = getLinhasInfo();
            model.put("linhas", linhasInfo);
            return HttpResponse.ok(model);
        } catch (Exception e) {
            log.error("Erro ao buscar linhas: {}", e.getMessage(), e);
            model.put("linhas", "Ocorreu um erro ao buscar as linhas...");
            return HttpResponse.serverError(model);
        }
    }

    @Get("/empresas")
    @Produces(MediaType.TEXT_HTML)
    @View("layout")
    public HttpResponse<Map<String, Object>> empresas(HttpRequest<?> request) {
        Map<String, Object> model = new HashMap<>();
        model.put("uri", request.getPath());
        model.put("main", "empresas");
        return HttpResponse.ok(model);
    }

    @Get("/tarifa")
    @Produces(MediaType.TEXT_HTML)
    @View("layout")
    public HttpResponse<Map<String, Object>> tarifa(HttpRequest<?> request) {
        Map<String, Object> model = new HashMap<>();
        model.put("uri", request.getPath());
        model.put("main", "tarifa");
        return HttpResponse.ok(model);
    }
}
