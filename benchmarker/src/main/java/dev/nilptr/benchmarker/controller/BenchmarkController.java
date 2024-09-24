package dev.nilptr.benchmarker.controller;

import dev.nilptr.benchmarker.services.ZeebeWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
public class BenchmarkController {
    private final ZeebeWorkflowService zeebeWorkflowService;

    @GetMapping("/start-benchmark")
    public CompletableFuture<String> startBenchmark(@RequestParam String processId, @RequestParam int instances) {
        return zeebeWorkflowService.startWorkflowInstances(processId, instances)
                .thenApply(result -> "Benchmark start for process: " + processId + " with " + instances + " instances");
    }
}
