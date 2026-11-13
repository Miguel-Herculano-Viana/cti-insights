package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /** GET /api/dashboard          -> último envio
     *  GET /api/dashboard?lote=3   -> envio específico */
    @GetMapping
    public Map<String, Object> estatisticas(@RequestParam(required = false) Long lote) {
        return service.estatisticas(lote);
    }

    @GetMapping("/lotes")
    public List<Map<String, Object>> lotes() {
        return service.listarLotes();
    }
}
