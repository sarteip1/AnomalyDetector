package rules

import (
	"bufio"
	"fmt"
	"os"
	"regexp"
	"strconv"
	"strings"
)

type Rule struct {
	Action     string
	Protocol   string
	SourceIP   string
	SourcePort string
	DestIP     string
	DestPort   string
	Msg        string
	Content    string
	Header     string
	TTL        int
	RST_flag   bool
	ExtraRules bool
}


func (r *Rule) Show() {
	fmt.Println("Action: " + r.Action)
	fmt.Println("Protocol: " + r.Protocol)
	fmt.Println("SourceIP: " + r.SourceIP)
	fmt.Println("SourcePort: " + r.SourcePort)
	fmt.Println("DestIP: " + r.DestIP)
	fmt.Println("DestPort: " + r.DestPort)
	fmt.Println("Msg: " + r.Msg)
	fmt.Println("Content: " + r.Content)
	fmt.Println("Header: " + r.Header)
	fmt.Println("TTL: " + fmt.Sprint(r.TTL))
	fmt.Println("RST_flag: " + strconv.FormatBool(r.RST_flag))
	fmt.Println("-----------------------")
}

func ParseRule(ruleStr string) (Rule, error) {
	components := strings.Fields(ruleStr)
	if len(components) < 6 {
		return Rule{}, fmt.Errorf("invalid Snort rule: %s", ruleStr)
	}

	rule := Rule{
		Action:     components[0],
		Protocol:   components[1],
		SourceIP:   components[2],
		SourcePort: components[3],
		DestIP:     components[5],
		DestPort:   components[6],
		Msg:        "",
		Content:    "",
		Header:		"",
		TTL:		0,
		RST_flag:	false,
	}

	var err error

	regex := *regexp.MustCompile(`msg:"([^"]*)";`)
	matches := regex.FindStringSubmatch(ruleStr)
	if len(matches) > 0 {
		rule.Msg = matches[1]
		rule.ExtraRules=true
	}

	regex = *regexp.MustCompile(`content:"([^"]*)";`)
	matches = regex.FindStringSubmatch(ruleStr)
	if len(matches) > 0 {
		rule.Content = matches[1]
		rule.ExtraRules=true
	}

	regex = *regexp.MustCompile(`header:"([^"]*)";`)
	matches = regex.FindStringSubmatch(ruleStr)
	if len(matches) > 0 {
		rule.Header = matches[1]
		rule.ExtraRules=true
	}


	regex = *regexp.MustCompile(`ttl_less:([1-9][0-9]*);`)
	matches = regex.FindStringSubmatch(ruleStr)
	if len(matches) > 0 {
		rule.TTL, err = strconv.Atoi(matches[1])
		if err!=nil{
			return Rule{}, fmt.Errorf("Cannot parse parameter ttl_less")
		}
		rule.ExtraRules=true
	}

	regex = *regexp.MustCompile(`rst_flag;`)
	matches = regex.FindStringSubmatch(ruleStr)
	if len(matches) > 0 {
		rule.RST_flag = true
		rule.ExtraRules=true
	}

	return rule, nil
}

func ParseSnortRules(file *os.File) ([]Rule, error) {
	var rules []Rule

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()

		if len(line) == 0 || line[0] == '#' {
			continue
		}

		rule, err := ParseRule(line)
		if err != nil {
			return nil, err
		}

		rules = append(rules, rule)
	}

	if err := scanner.Err(); err != nil {
		return nil, fmt.Errorf("failed to read AAAD rules: %s", err)
	}

	return rules, nil
}

