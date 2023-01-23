package packet

import (
	"github.com/google/gopacket/layers"

	"log"

	"github.com/google/gopacket"
	"github.com/google/gopacket/pcap"
)


type Packet struct {
	gopacket.Packet
}

func GetIpPorts(p *Packet, ip *layers.IPv4) (string, string, string, string){

	srcIP := ip.SrcIP.String()
	dstIP := ip.DstIP.String()

	tcpLayer := p.Layer(layers.LayerTypeTCP)
	udpLayer := p.Layer(layers.LayerTypeUDP)

	srcPort:=""
	dstPort:=""

	if tcpLayer != nil {
		tcp, _ := tcpLayer.(*layers.TCP)
		srcPort=tcp.SrcPort.String()
		dstPort=tcp.DstPort.String()
	} else if udpLayer != nil {
		udp, _ := udpLayer.(*layers.UDP)
		srcPort=udp.SrcPort.String()
		dstPort=udp.DstPort.String()
	}


	return srcIP, dstIP, srcPort, dstPort

}




func CapturePackets(iface string, matchedPackets chan<- *Packet) {
	handle, err := pcap.OpenLive(iface, 65536, true, pcap.BlockForever)
	if err != nil {
		log.Fatalf("Failed to open interface %s: %s", iface, err)
	}
	defer handle.Close()

	err = handle.SetBPFFilter("tcp or udp")
	if err != nil {
		log.Printf("Failed to set BPF filter on interface %s: %s", iface, err)
	}

	packetSource := gopacket.NewPacketSource(handle, handle.LinkType())
	for packet := range packetSource.Packets() {
		p := &Packet{Packet: packet}

		matchedPackets <- p
	}
}

