package rules

import (
	"aaad-main/packet"
	"fmt"
	"strings"

	"github.com/google/gopacket/layers"
)


func MatchExtraRules(packet *packet.Packet, rule Rule) bool{
	payloadLayer:=packet.ApplicationLayer()
	ipLayer := packet.Layer(layers.LayerTypeIPv4)
	ip,_:=ipLayer.(*layers.IPv4)

	if payloadLayer != nil && rule.Content != "" {
		payload := string(payloadLayer.Payload())
		if strings.Contains(payload, rule.Content) {
			return true
		}
	}

	if rule.TTL!=0{
		if ip.TTL < uint8(rule.TTL){
			return true
		}
	}


	return false
}

func PacketMatchesRule(packet *packet.Packet, rule Rule) bool {
	ethLayer := packet.Layer(layers.LayerTypeEthernet)
	ipLayer := packet.Layer(layers.LayerTypeIPv4)
	tcpLayer := packet.Layer(layers.LayerTypeTCP)
	udpLayer := packet.Layer(layers.LayerTypeUDP)

	if ethLayer == nil || ipLayer == nil {
		return false
	}

	ip, _ := ipLayer.(*layers.IPv4)
	srcIP := ip.SrcIP.String()
	dstIP := ip.DstIP.String()
	

	protocol := ""
	if tcpLayer != nil {
		protocol = "tcp"
	} else if udpLayer != nil {
		protocol = "udp"
	}

	if (rule.SourceIP == "any" || srcIP == rule.SourceIP) &&
		(rule.DestIP == "any" || dstIP == rule.DestIP) &&
		rule.Protocol == protocol {
		if tcpLayer != nil {
			tcp, _ := tcpLayer.(*layers.TCP)
			srcPort := tcp.SrcPort.String()
			dstPort := tcp.DstPort.String()
			if strings.Contains(srcPort, "("){
				srcPort=srcPort[:strings.IndexByte(srcPort,'(')]
			}
			if strings.Contains(dstPort,"("){
				dstPort=dstPort[:strings.IndexByte(dstPort,'(')]
			}
			if (rule.SourcePort == "any" || rule.SourcePort == srcPort) &&
				(rule.DestPort == "any" || rule.DestPort == dstPort) {
				if rule.ExtraRules{
					return MatchExtraRules(packet,rule)
				}else{
					fmt.Println(srcIP)
				}
				return true
				
			}
		} else if udpLayer != nil {
			udp, _ := udpLayer.(*layers.UDP)
			srcPort := udp.SrcPort.String()
			dstPort := udp.DstPort.String()
			if strings.Contains(srcPort, "("){
				srcPort=srcPort[:strings.IndexByte(srcPort,'(')]
			}
			if strings.Contains(dstPort,"("){
				dstPort=dstPort[:strings.IndexByte(dstPort,'(')]
			}
			if (rule.SourcePort == "any" || rule.SourcePort == srcPort) &&
				(rule.DestPort == "any" || rule.DestPort == dstPort) {
				if rule.ExtraRules{
					return MatchExtraRules(packet,rule)
				}
				return true
			}
		}

	}

	return false
}

