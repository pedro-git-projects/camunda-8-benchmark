package main

import (
	"context"
	"flag"
	"fmt"
	"log"
	"os"
	"sync"
	"time"

	"github.com/camunda/zeebe/clients/go/v8/pkg/zbc"
)

const (
	defaultZeebeGatewayAddress = "localhost:26500"
	defaultProcessID           = "your-process-id"
	defaultNumInstances        = 100
)

func main() {
	zeebeGatewayAddress := flag.String("gateway", defaultZeebeGatewayAddress, "Zeebe gateway address (host:port) [default: localhost:26500]")
	processID := flag.String("process", defaultProcessID, "BPMN process ID to start [required]")
	numInstances := flag.Int("instances", defaultNumInstances, "Number of workflow instances to start [default: 100]")

	flag.Usage = func() {
		fmt.Fprintf(os.Stderr, "Usage of %s:\n", os.Args[0])
		fmt.Fprintf(os.Stderr, "  -gateway string\n")
		fmt.Fprintf(os.Stderr, "    \tZeebe gateway address (host:port) [default: %s]\n", defaultZeebeGatewayAddress)
		fmt.Fprintf(os.Stderr, "  -process string\n")
		fmt.Fprintf(os.Stderr, "    \tBPMN process ID to start [required]\n")
		fmt.Fprintf(os.Stderr, "  -instances int\n")
		fmt.Fprintf(os.Stderr, "    \tNumber of workflow instances to start [default: %d]\n", defaultNumInstances)
		fmt.Fprintf(os.Stderr, "\nExample: %s -gateway localhost:26500 -process my-process -instances 500\n", os.Args[0])
	}

	flag.Parse()

	if *processID == "your-process-id" || *processID == "" {
		fmt.Fprintln(os.Stderr, "Error: BPMN process ID must be specified using the -process flag.")
		flag.Usage()
		os.Exit(1)
	}

	client, err := zbc.NewClient(&zbc.ClientConfig{
		GatewayAddress:         *zeebeGatewayAddress,
		UsePlaintextConnection: true,
	})
	if err != nil {
		log.Fatalf("Failed to create Zeebe client: %v", err)
	}
	defer client.Close()

	fmt.Printf("Connected to Zeebe at %s\n", *zeebeGatewayAddress)
	fmt.Printf("Starting %d instances of process %s\n", *numInstances, *processID)

	startTime := time.Now()

	var wg sync.WaitGroup

	for i := 0; i < *numInstances; i++ {
		wg.Add(1)

		go func(instanceID int) {
			defer wg.Done()

			request, err := client.NewCreateInstanceCommand().
				BPMNProcessId(*processID).
				LatestVersion().
				Send(context.Background())
			if err != nil {
				log.Printf("Failed to start instance %d: %v", instanceID, err)
				return
			}

			fmt.Printf("Started workflow instance %d with key %d\n", instanceID, request.GetProcessInstanceKey())
		}(i)
	}

	wg.Wait()

	elapsedTime := time.Since(startTime)
	fmt.Printf("Started %d workflow instances in %s\n", *numInstances, elapsedTime)
}
