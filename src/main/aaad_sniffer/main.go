package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"runtime"
	"sync"
	"syscall"

	"aaad-main/packet"
	"aaad-main/rules"

	"github.com/google/gopacket/layers"
	"github.com/google/gopacket/pcap"
)


const aaadRulesFile = "aaad.rules"


func ProcessPackets(matchedPackets <-chan *packet.Packet, rules_set []rules.Rule) {
	var wg sync.WaitGroup
	for pack := range matchedPackets {
		wg.Add(1)
		go func(p *packet.Packet) {
			defer wg.Done()

			ipLayer := p.Layer(layers.LayerTypeIPv4)
			ip,s:=ipLayer.(*layers.IPv4)
			if !s {
				return
			}
			srcIP, dstIP, srcPort, dstPort := packet.GetIpPorts(p, ip)


			for _, rule := range rules_set {
				if rules.PacketMatchesRule(p, rule) {
					fmt.Printf("[%s][from: %s:%s, to: %s:%s] Packet matches AAAD rule: %s\n", p.Metadata().Timestamp, srcIP, srcPort,dstIP,dstPort, rule.Msg)
				}
			}
		}(pack)
	}
	wg.Wait()
}



func main() {
	rulesFile, err := os.Open(aaadRulesFile)
	if err != nil {
		log.Fatalf("Failed to open AAAD rules file: %s", err)
	}
	defer rulesFile.Close()


	ifaces, err := pcap.FindAllDevs()
	if err !=nil{
		log.Fatalf("Failed to get interfaces")
	}


	rules_set, err := rules.ParseSnortRules(rulesFile)
	if err != nil {
		log.Fatalf("Failed to parse AAAD rules: %s", err)
	}

	//for _, r := range rules_set {
	//	r.Show()
	//}

	matchedPackets := make(chan *packet.Packet)


	for _,iface:=range ifaces{
		go packet.CapturePackets(iface.Name, matchedPackets)
	}

	for i := 0; i < runtime.NumCPU(); i++ {
		go ProcessPackets(matchedPackets, rules_set)
	}

	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)
	<-signalChan

	fmt.Println("Packet sniffer stopped.")
}
